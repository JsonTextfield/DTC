package com.jsontextfield.dtc.core.network

import com.google.transit.realtime.FeedMessage
import com.google.transit.realtime.TripDescriptor
import com.google.transit.realtime.TripUpdate

data class LiveArrival(
    val tripId: String,
    val routeId: String?,
    val predictedEpochSeconds: Long,
    val delaySeconds: Int?,
)

fun FeedMessage.arrivalsAtStop(stopId: String): List<LiveArrival> =
    entity
        .mapNotNull { it.trip_update }
        .filter { it.trip.schedule_relationship != TripDescriptor.ScheduleRelationship.CANCELED }
        .flatMap { update ->
            update.stop_time_update
                .filter { stopId.split(",").any { stopId -> stopId == it.stop_id }}
                .filter { it.schedule_relationship != TripUpdate.StopTimeUpdate.ScheduleRelationship.SKIPPED }
                .mapNotNull { stu ->
                    val time = stu.arrival?.time ?: stu.departure?.time
                    if (time == null || time == 0L) return@mapNotNull null
                    LiveArrival(
                        tripId = update.trip.trip_id.orEmpty(),
                        routeId = update.trip.route_id,
                        predictedEpochSeconds = time,
                        delaySeconds = stu.arrival?.delay ?: stu.departure?.delay,
                    )
                }
        }
