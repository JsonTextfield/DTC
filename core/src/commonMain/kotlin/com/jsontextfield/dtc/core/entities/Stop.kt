package com.jsontextfield.dtc.core.entities

import androidx.compose.runtime.Immutable
import com.jsontextfield.dtc.core.ui.StopType

@Immutable
data class Stop(
    val name: String,
    val code: String,
    val types: Set<StopType> = emptySet(),
    val isEnabled: Boolean = true,
    val isFavourite: Boolean = false,
)
