package com.jsontextfield.dtc.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.jsontextfield.dtc.core.data.DataStorePreferencesRepository
import com.jsontextfield.dtc.core.data.FAVOURITE_STOPS_KEY
import com.jsontextfield.dtc.core.data.HIDDEN_TRAINS_KEY
import com.jsontextfield.dtc.core.data.IPreferencesRepository
import com.jsontextfield.dtc.core.data.ITransitRepository
import com.jsontextfield.dtc.core.data.SELECTED_STOP_KEY
import com.jsontextfield.dtc.core.data.SORT_MODE_KEY
import com.jsontextfield.dtc.core.domain.GetSelectedStopUseCase
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual fun preferencesModule(): Module {
    return module {
        single<IPreferencesRepository> {
            val appGroupId = "group.com.jsontextfield.godepartures"
            DataStorePreferencesRepository(
                dataStore = createDataStore(),
                onSetStop = { stopCode: String ->
                    val userDefaults = NSUserDefaults(suiteName = appGroupId)
                    userDefaults.setObject(stopCode, forKey = SELECTED_STOP_KEY)
                },
                onSetSortMode = { sortMode ->
                    val userDefaults = NSUserDefaults(suiteName = appGroupId)
                    userDefaults.setInteger(sortMode.ordinal.toLong(), forKey = SORT_MODE_KEY)
                },
                onSetVisibleTrains = { visibleTrains ->
                    val userDefaults = NSUserDefaults(suiteName = appGroupId)
                    userDefaults.setObject(visibleTrains.joinToString(","), forKey = HIDDEN_TRAINS_KEY)
                },
                onSetFavouriteStops = { favouriteStops ->
                    val userDefaults = NSUserDefaults(suiteName = appGroupId)
                    userDefaults.setObject(favouriteStops.joinToString(","), forKey = FAVOURITE_STOPS_KEY)
                }
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
fun createDataStore(): DataStore<Preferences> = createDataStore(
    producePath = {
        val appGroupId = "group.com.jsontextfield.godepartures"

        val sharedContainer: NSURL? =
            NSFileManager.defaultManager.containerURLForSecurityApplicationGroupIdentifier(appGroupId)

        val baseURL = sharedContainer ?: NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        requireNotNull(baseURL).path + "/$dataStoreFileName"
    }
)


class WidgetHelper : KoinComponent {
    val preferencesRepository: IPreferencesRepository by inject()
    val transitRepository: ITransitRepository by inject()
    val getSelectedStopUseCase = GetSelectedStopUseCase(
        goTrainDataSource = transitRepository,
        preferencesRepository = preferencesRepository,
    )
}

actual fun widgetModule(): Module {
    return module {  }
}

actual fun databaseModule(): Module {
    return module {
        single<app.cash.sqldelight.db.SqlDriver> { com.jsontextfield.dtc.core.data.createSqlDriver() }
        single { com.jsontextfield.dtc.core.db.GtfsDatabase(get()) }
    }
}