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

class TtcRealtimeApi {
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
            level = LogLevel.ALL
        }
    }

    suspend fun tripUpdates(): FeedMessage = fetch(TRIP_UPDATES_URL)
    suspend fun vehiclePositions(): FeedMessage = fetch(VEHICLE_POSITIONS_URL)
    suspend fun alerts(): FeedMessage = fetch(ALERTS_URL)

    private suspend fun fetch(url: String): FeedMessage {
        try {
            Logger.withTag("TtcRealtimeApi").d("Fetching GTFS-RT from $url")
            val bytes = client.get(url).bodyAsBytes()
            Logger.withTag("TtcRealtimeApi").d("Fetched ${bytes.size} bytes from $url")
            return FeedMessage.ADAPTER.decode(bytes)
        } catch (e: Exception) {
            Logger.withTag("TtcRealtimeApi").e(e) { "Failed to fetch or decode GTFS-RT from $url" }
            throw e
        }
    }

    companion object {
        const val TRIP_UPDATES_URL = "https://gtfsrt.ttc.ca/trips/update?format=binary"
        const val VEHICLE_POSITIONS_URL = "https://gtfsrt.ttc.ca/vehicles/position?format=binary"
        const val ALERTS_URL = "https://gtfsrt.ttc.ca/alerts/all?format=binary"
    }
}
