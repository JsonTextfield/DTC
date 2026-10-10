package com.jsontextfield.dtc.core.ui.theme

import androidx.compose.material3.ColorScheme

actual fun isDynamicThemeEnabled(): Boolean {
    return false
}

actual fun dynamicColorScheme(useDarkTheme: Boolean): ColorScheme? {
    return null
}