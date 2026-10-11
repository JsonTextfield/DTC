package com.jsontextfield.dtc.core.data

import com.jsontextfield.dtc.core.db.SelectTripById
import com.jsontextfield.dtc.core.db.Stops
import com.jsontextfield.dtc.core.db.Trips
import com.jsontextfield.dtc.core.entities.Alert
import com.jsontextfield.dtc.core.entities.Schedule
import com.jsontextfield.dtc.core.entities.Stop
import com.jsontextfield.dtc.core.entities.Trip
import com.jsontextfield.dtc.core.entities.TripDetails
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlinx.coroutines.flow.Flow

interface ITransitRepository {
    suspend fun getTrips(stopCode: String): List<Trip>

    suspend fun getTripDetails(tripId: String, stopCode: String): TripDetails?
    suspend fun getTripInfo(tripId: String): SelectTripById?

    suspend fun getMoreTrips(tripId: String, stopCode: String): Set<String>
    @NativeCoroutines
    fun getAllAlerts(language: String): Flow<List<Alert>>

    suspend fun getAllStops(): List<Stop>

    suspend fun getStopsByName(name: String): List<Stops>

    suspend fun getStopById(id: String): Stops?

    suspend fun getUPExpressTripSchedule(id: String): List<Schedule>
}