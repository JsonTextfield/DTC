package com.jsontextfield.dtc.core.ui

import dtc.core.generated.resources.Res
import dtc.core.generated.resources.dark_mode
import dtc.core.generated.resources.light_mode
import dtc.core.generated.resources.system_default
import org.jetbrains.compose.resources.StringResource

enum class ThemeMode(val key: StringResource) {
    LIGHT(Res.string.light_mode),
    DARK(Res.string.dark_mode),
    DEFAULT(Res.string.system_default),
}