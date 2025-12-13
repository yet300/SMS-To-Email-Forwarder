package com.yet.forwarder

import android.app.Application
import com.yet.forwarder.di.AppModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module

class ForwarderApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@ForwarderApp)
            modules(AppModule().module)
        }
    }
}
