package com.jsontextfield.dtc.core.data

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.jsontextfield.dtc.core.db.GtfsDatabase

actual fun createSqlDriver(): SqlDriver {
    throw UnsupportedOperationException("Use Context-based driver on Android")
}

fun createAndroidSqlDriver(context: Context): SqlDriver {
    val dbFile = context.getDatabasePath("gtfs.db")
    if (!dbFile.exists()) {
        dbFile.parentFile?.mkdirs()
        try {
            context.assets.open("composeResources/com.jsontextfield.dtc.core.generated.resources/files/gtfs.db").use { input ->
                dbFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } catch (_: Exception) {
            try {
                context.assets.open("gtfs.db").use { input ->
                    dbFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (_: Exception) {}
        }
    }
    return AndroidSqliteDriver(GtfsDatabase.Schema, context, "gtfs.db")
}
