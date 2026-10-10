package com.jsontextfield.dtc.wearapp

import android.app.Application
import com.jsontextfield.dtc.wearapp.di.initKoin
import org.koin.android.ext.koin.androidContext

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@MainApplication)
        }
    }
}