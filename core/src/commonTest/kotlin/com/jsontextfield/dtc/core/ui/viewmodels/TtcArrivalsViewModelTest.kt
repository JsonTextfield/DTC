package com.jsontextfield.dtc.core.ui.viewmodels

import com.jsontextfield.dtc.core.ui.Status
import kotlin.test.Test
import kotlin.test.assertEquals

class TtcArrivalsViewModelTest {

    @Test
    fun testTtcArrivalsUIStateDefaults() {
        val state = TtcArrivalsUIState()
        assertEquals(Status.LOADING, state.status)
        assertEquals(emptyList(), state.arrivals)
    }

    @Test
    fun testTtcArrivalsUIStateCopy() {
        val arrivalItem = TtcArrivalItem(
            tripId = "trip-1",
            routeId = "501",
            minutes = 5,
            delaySeconds = 0,
        )
        val state = TtcArrivalsUIState(
            status = Status.LOADED,
            arrivals = listOf(arrivalItem),
        )
        assertEquals(Status.LOADED, state.status)
        assertEquals(1, state.arrivals.size)
        assertEquals("trip-1", state.arrivals.first().tripId)
    }
}
