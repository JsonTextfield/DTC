package com.jsontextfield.dtc.core.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.jsontextfield.dtc.core.db.GtfsDatabase

actual fun createSqlDriver(): SqlDriver {
    return NativeSqliteDriver(GtfsDatabase.Schema, "gtfs.db")
}
