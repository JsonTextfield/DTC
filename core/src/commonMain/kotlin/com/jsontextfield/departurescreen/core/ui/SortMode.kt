package com.jsontextfield.departurescreen.core.ui

import dtc.core.generated.resources.Res
import dtc.core.generated.resources.sort_by_line
import dtc.core.generated.resources.sort_by_time
import org.jetbrains.compose.resources.StringResource

enum class SortMode(val key: StringResource) {
    TIME(Res.string.sort_by_time),
    LINE(Res.string.sort_by_line),
}