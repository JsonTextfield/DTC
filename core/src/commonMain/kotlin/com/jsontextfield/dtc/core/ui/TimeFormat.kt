package com.jsontextfield.dtc.core.ui

import dtc.core.generated.resources.Res
import dtc.core.generated.resources.relative
import dtc.core.generated.resources.twelve_hour
import dtc.core.generated.resources.twenty_four_hour
import org.jetbrains.compose.resources.StringResource

enum class TimeFormat(val key: StringResource) {
    TWELVE_HOUR(Res.string.twelve_hour),
    TWENTY_FOUR_HOUR(Res.string.twenty_four_hour),
    RELATIVE(Res.string.relative),
}