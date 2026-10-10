package com.jsontextfield.dtc.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.jsontextfield.dtc.core.data.DataStorePreferencesRepository
import com.jsontextfield.dtc.core.data.IPreferencesRepository
import com.jsontextfield.dtc.widget.config.WidgetConfigDataStore
import com.jsontextfield.dtc.widget.config.WidgetConfigViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

actual fun preferencesModule(): Module {
    return module {
        single<DataStore<Preferences>> {
            createDataStore(androidContext())
        }
        single<IPreferencesRepository> {
            DataStorePreferencesRepository(get<DataStore<Preferences>>())
        }
    }
}

actual fun widgetModule(): Module {
    return module {
        single<WidgetConfigDataStore> {
            WidgetConfigDataStore(androidContext())
        }
        viewModel<WidgetConfigViewModel> { params ->
            WidgetConfigViewModel(
                preferencesRepository = get<IPreferencesRepository>(),
                widgetConfigDataStore = get<WidgetConfigDataStore>(),
                widgetId = params.getOrNull(Int::class),
            )
        }
    }
}

actual fun databaseModule(): Module {
    return module {
        single<app.cash.sqldelight.db.SqlDriver> { com.jsontextfield.dtc.core.data.createAndroidSqlDriver(androidContext()) }
        single { com.jsontextfield.dtc.core.db.GtfsDatabase(get()) }
    }
}

fun createDataStore(context: Context): DataStore<Preferences> = createDataStore(
    producePath = { context.filesDir.resolve(dataStoreFileName).absolutePath }
)