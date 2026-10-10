package com.jsontextfield.dtc.core.ui

import dtc.core.generated.resources.Res
import dtc.core.generated.resources.bus
import dtc.core.generated.resources.train
import org.jetbrains.compose.resources.StringResource

enum class StopType(
    val stringResId: StringResource,
    val typeString: String,
) {
    BUS(Res.string.bus, "Bus"),
    TRAIN(Res.string.train, "Train"),
    //PARK_AND_RIDE(Res.string.park_and_ride, "Park & Ride"),
}