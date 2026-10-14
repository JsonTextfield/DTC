package com.jsontextfield.dtc.core.network

import co.touchlab.kermit.Logger
import com.google.transit.realtime.FeedMessage
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger as KtorLogger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes

class TtcGtfsRealtimeApi {
    private val client: HttpClient = HttpClient {
        expectSuccess = true
        install(HttpTimeout) {
            connectTimeoutMillis = 10_000
            requestTimeoutMillis = 15_000
            socketTimeoutMillis = 15_000
        }
        install(Logging) {
            logger = object : KtorLogger {
                override fun log(message: String) {
                    Logger.withTag("TtcRealtimeApi").d(message)
                }
            }
            level = LogLevel.INFO
        }
    }

    suspend fun tripUpdates(): FeedMessage = fetch(TRIP_UPDATES_URL)
    suspend fun subwayUpdates(): FeedMessage = fetch(SUBWAY_UPDATES_URL)
    suspend fun vehiclePositions(): FeedMessage = fetch(VEHICLE_POSITIONS_URL)
    suspend fun alerts(): FeedMessage = fetch(ALERTS_URL)

    private suspend fun fetch(url: String): FeedMessage {
        try {
            val bytes = client.get(url).bodyAsBytes()
            return FeedMessage.ADAPTER.decode(bytes)
        } catch (e: Exception) {
            Logger.withTag("TtcRealtimeApi").e(e) { "Failed to fetch or decode GTFS-RT from $url" }
            throw e
        }
    }

    companion object {
        const val TRIP_UPDATES_URL = "https://gtfsrt.ttc.ca/trips/update?format=binary"
        const val SUBWAY_UPDATES_URL = "https://gtfsrt.ttc.ca/trips/subway?format=binary"
        const val VEHICLE_POSITIONS_URL = "https://gtfsrt.ttc.ca/vehicles/position?format=binary"
        const val ALERTS_URL = "https://gtfsrt.ttc.ca/alerts/all?format=binary"
    }
}
