package com.jsontextfield.dtc.core.ui.navigation

import com.jsontextfield.dtc.core.entities.Trip

data class NavigationActions(
    val onShowAlerts: () -> Unit = {},
    val onShowStops: () -> Unit = {},
    val onShowTripDetails: (Trip) -> Unit = {},
    val onShowSettings: () -> Unit = {},
)