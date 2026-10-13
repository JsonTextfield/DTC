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
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.char
import kotlin.time.Clock
import kotlin.time.Instant

class TransitRepository(
    private val ttcGtfsRealtimeApi: TtcGtfsRealtimeApi,
    private val gtfsDatabase: GtfsDatabase,
) : ITransitRepository {
    private val timeZone = TimeZone.of("America/Toronto")
    private var stopsByName: Map<String, List<Stop>> = emptyMap()
    private var stopsMap: Map<String, String> = emptyMap()
    private var lastUpdated: Instant = Instant.fromEpochMilliseconds(0)

    override suspend fun getTrips(stopCode: String): List<Trip> {
        val currentTime = Clock.System.now()
        return coroutineScope {
            val tripArrivalsDeferred = async {
                ttcGtfsRealtimeApi.tripUpdates().arrivalsAtStop(stopCode)
            }
            val subwayArrivalsDeferred = async {
                ttcGtfsRealtimeApi.subwayUpdates().arrivalsAtStop(stopCode)
            }

            val allArrivals =
                tripArrivalsDeferred.await() + subwayArrivalsDeferred.await()

            val stop = gtfsDatabase.stopQueries.selectStopById(
                stopCode.split(",").first()
            ).executeAsOneOrNull()
            allArrivals.mapNotNull { arrival ->
                getTripInfo(arrival.tripId)
                    ?.let { tripInfo ->
                        Trip(
                            id = arrival.tripId,
                            code = arrival.routeId.orEmpty(),
                            destination = tripInfo.trip_headsign.orEmpty()
                                .substringAfter("towards "),
                            color = tripInfo.route_color?.let(Color::fromHex)
                                ?: Color.Gray,
                            textColor = tripInfo.route_text_color?.let(Color::fromHex)
                                ?: Color.White,
                            lastUpdated = currentTime,
                            departureTime = Instant.fromEpochSeconds(arrival.predictedEpochSeconds),
                            stopCode = stopCode,
                            stopName = stop?.stop_name
                        )
                    }
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
        return coroutineScope {

            val lastUpdated = Clock.System.now()
            val tripArrivalsDeferred = async {
                ttcGtfsRealtimeApi.tripUpdates().entity
            }
            val subwayArrivalsDeferred = async {
                ttcGtfsRealtimeApi.subwayUpdates().entity
            }

            val allArrivals =
                tripArrivalsDeferred.await() + subwayArrivalsDeferred.await()
            allArrivals.firstOrNull {
                it.trip_update?.trip?.trip_id == tripId
            }?.let { tripUpdate ->
                TripDetails(
                    id = tripId,
                    stops = tripUpdate.trip_update?.stop_time_update?.mapNotNull { stopTimeUpdate ->
                        stopTimeUpdate.stop_id?.let { stopId ->
                            val stop =
                                gtfsDatabase.stopQueries.selectStopById(stopId)
                                    .executeAsOneOrNull()
                            Schedule(
                                name = stop?.stop_name.orEmpty(),
                                code = stopId,
                                time = Instant.fromEpochSeconds(
                                    stopTimeUpdate.arrival?.time
                                        ?: lastUpdated.epochSeconds
                                ),
                                lastUpdated = lastUpdated
                            )
                        }
                    }.orEmpty(),
                )
            }
        }
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
            dbStops.groupBy { stop ->
                stop.parent_station ?: stop.stop_id
            }.map { (id, stops) ->
                val code = stops.joinToString(",") { it.stop_id }
                val name = gtfsDatabase.stopQueries.selectStopById(id)
                    .executeAsOneOrNull()
                Stop(
                    name = name?.stop_name ?: id,
                    code = code,
                    types = emptySet(),
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
                        urlEn = alert.url?.translation?.let { translations ->
                            translations.firstOrNull { translation -> translation.language == language }?.text
                                ?: translations.firstOrNull()?.text
                        }
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
        Color("FF${hex.padStart(6, '0')}".toLong(16))
    } catch (e: Exception) {
        Color.Gray
    }
}