package com.jsontextfield.dtc.core.data

import androidx.compose.ui.graphics.Color
import co.touchlab.kermit.Logger
import com.jsontextfield.dtc.core.db.GtfsDatabase
import com.jsontextfield.dtc.core.db.SelectTripById
import com.jsontextfield.dtc.core.db.Stops
import com.jsontextfield.dtc.core.entities.Alert
import com.jsontextfield.dtc.core.entities.Schedule
import com.jsontextfield.dtc.core.entities.Stop
import com.jsontextfield.dtc.core.entities.Trip
import com.jsontextfield.dtc.core.entities.TripDetails
import com.jsontextfield.dtc.core.network.TtcGtfsRealtimeApi
import com.jsontextfield.dtc.core.network.arrivalsAtStop
import com.jsontextfield.dtc.core.network.model.AlertsResponse
import com.jsontextfield.dtc.core.network.model.ExceptionsResponse
import com.jsontextfield.dtc.core.network.model.ServiceAtAGlanceTrainsResponse
import com.jsontextfield.dtc.core.ui.StopType
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.char
import kotlinx.datetime.toInstant
import kotlinx.io.IOException
import kotlin.time.Clock
import kotlin.time.Instant

class TransitRepository(
    private val ttcGtfsRealtimeApi: TtcGtfsRealtimeApi,
    private val gtfsDatabase: GtfsDatabase,
) : ITransitRepository {
    private val timeZone = TimeZone.of("America/Toronto")
    private var stopsByName: Map<String, List<Stop>> = emptyMap()
    private var stopsMap: Map<String, String> = emptyMap()
    private var serviceAlerts: List<Alert> = emptyList()
    private var lastUpdated: Instant = Instant.fromEpochMilliseconds(0)
    private var serviceAtAGlanceTrains: Map<String, ServiceAtAGlanceTrainsResponse.Trips.Trip>? =
        null
    private var exceptions: ExceptionsResponse? = null

    override suspend fun getTrips(stopCode: String): List<Trip> {
        val currentTime = Clock.System.now()
        return ttcGtfsRealtimeApi.tripUpdates().arrivalsAtStop(stopCode)
            .mapNotNull { arrival ->
                getTripInfo(arrival.tripId)
                    ?.let { tripInfo ->
                        Trip(
                            id = arrival.tripId,
                            code = arrival.routeId.orEmpty(),
                            destination = tripInfo.trip_headsign.orEmpty(),
                            color = tripInfo.route_color?.let(Color::fromHex)
                                ?: Color.Gray,
                            textColor = tripInfo.route_text_color?.let(Color::fromHex)
                                ?: Color.White,
                            lastUpdated = currentTime,
                            departureTime = Instant.fromEpochSeconds(arrival.predictedEpochSeconds),
                        )
                    }
            }
    }

    override suspend fun getMoreTrips(
        tripId: String,
        stopCode: String
    ): Set<String> {
        return emptySet()
    }

    override suspend fun getTripDetails(
        tripId: String,
        stopCode: String
    ): TripDetails? {
        return null
    }

    override suspend fun getTripInfo(tripId: String): SelectTripById? {
        return gtfsDatabase.tripQueries.selectTripById(trip_id = tripId)
            .executeAsOneOrNull()
    }


    override suspend fun getAllStops(): List<Stop> {
        if (stopsByName.isNotEmpty()) {
            return stopsByName.values.flatten()
        }
        return try {
            val dbStops =
                gtfsDatabase.stopQueries.selectAllStops().executeAsList()
            dbStops.map { stopRow ->
                val code = stopRow.stop_code ?: stopRow.stop_id
                val name = stopRow.stop_name
                val locationType = stopRow.location_type?.toInt() ?: 0
                val type = when {
                    locationType == 1 || locationType == 2 -> setOf(
                        StopType.TRAIN,
                        StopType.BUS
                    )

                    else -> setOf(StopType.BUS)
                }
                Stop(
                    name = name ?: "",
                    code = code,
                    types = type,
                )
            }
        } catch (e: Exception) {
            Logger.withTag(TransitRepository::class.simpleName.toString())
                .e(e.message.orEmpty())
            emptyList()
        }.also {
            stopsMap = it.associate { stop -> stop.code to stop.name }
            stopsByName = it.groupBy { stop -> stop.name }
        }
    }

    override suspend fun getStopsByName(name: String): List<Stops> {
        return gtfsDatabase.stopQueries.selectStopsByName(stop_name = name)
            .executeAsList()
    }

    override suspend fun getStopById(id: String): Stops? {
        return gtfsDatabase.stopQueries.selectStopById(stop_id = id)
            .executeAsOneOrNull()
    }

    private fun AlertsResponse.toAlerts(): List<Alert> {
        return try {
            messages?.message?.map { message ->
                val bodyEn = message.bodyEnglish.orEmpty().trim()
                val bodyFr = message.bodyFrench.orEmpty().trim()
                val date = try {
                    LocalDateTime
                        .parse(message.postedDateTime, inFormatter)
                        .toInstant(timeZone)
                } catch (e: Exception) {
                    Instant.fromEpochMilliseconds(0L)
                }
                Alert(
                    id = message.code,
                    date = date,
                    affectedLines = message.lines.map { if (it.code == "GT") "KI" else it.code },
                    affectedStops = stopsMap.mapNotNull {
                        if (message.stops.any { stop -> stop.code in it.key }) {
                            it.value
                        } else {
                            null
                        }
                    },
                    subjectEn = message.subjectEnglish.orEmpty().trim(),
                    subjectFr = message.subjectFrench.orEmpty().trim(),
                    bodyEn = bodyEn,
                    bodyFr = bodyFr,
                )
            }?.sortedByDescending { it.date } ?: emptyList()
        } catch (exception: IOException) {
            throw exception
        } catch (exception: Exception) {
            exception.printStackTrace()
            emptyList()
        }
    }

    @NativeCoroutines
    override fun getAllAlerts(language: String): Flow<List<Alert>> {
        return flow {
            emit(ttcGtfsRealtimeApi.alerts().entity.mapNotNull { entity ->
                entity.alert?.let { alert ->
                    Alert(
                        id = entity.id,
                        subjectEn = alert.header_text?.translation?.let { translations ->
                            translations.firstOrNull { it.language == language }?.text
                                ?: translations.firstOrNull()?.text
                        }.orEmpty(),
                        bodyEn = alert.description_text?.translation?.let { translations ->
                            translations.firstOrNull { it.language == language }?.text
                                ?: translations.firstOrNull()?.text
                        }.orEmpty(),
                    )
                }

            })
        }
    }

    companion object {
        @OptIn(FormatStringsInDatetimeFormats::class)
        val inFormatter = LocalDateTime.Format {
            year()
            char('-')
            monthNumber()
            char('-')
            day()
            char(' ')
            hour()
            char(':')
            minute()
            char(':')
            second()
        }

        @OptIn(FormatStringsInDatetimeFormats::class)
        val serviceUpdateDateFormatter = LocalDateTime.Format {
            monthNumber()
            char('/')
            day()
            char('/')
            year()
            char(' ')
            hour()
            char(':')
            minute()
            char(':')
            second()
        }
    }
}

fun Color.Companion.fromHex(hex: String): Color {
    return try {
        Color("FF$hex".toLong(16))
    } catch (e: Exception) {
        Color.Gray
    }
}