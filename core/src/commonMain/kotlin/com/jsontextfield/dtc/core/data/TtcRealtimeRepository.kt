package com.jsontextfield.dtc.core.data

import com.jsontextfield.dtc.core.network.LiveArrival
import com.jsontextfield.dtc.core.network.TtcRealtimeApi
import com.jsontextfield.dtc.core.network.arrivalsAtStop

class TtcRealtimeRepository(
    private val api: TtcRealtimeApi,
) {
    suspend fun getArrivals(stopId: String): List<LiveArrival> {
        return api.tripUpdates().arrivalsAtStop(stopId)
    }
}
