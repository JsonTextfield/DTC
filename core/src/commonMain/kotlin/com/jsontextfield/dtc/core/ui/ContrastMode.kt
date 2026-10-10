package com.jsontextfield.dtc.core.ui


import dtc.core.generated.resources.Res
import dtc.core.generated.resources.high
import dtc.core.generated.resources.medium
import dtc.core.generated.resources.normal
import org.jetbrains.compose.resources.StringResource

enum class ContrastMode(val key: StringResource) {
    NORMAL(Res.string.normal),
    MEDIUM(Res.string.medium),
    HIGH(Res.string.high),
}