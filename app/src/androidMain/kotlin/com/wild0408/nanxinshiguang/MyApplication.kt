package com.wild0408.nanxinshiguang

import android.app.Application
import androidx.work.Configuration
import com.wild0408.nanxinshiguang.data.di.DatabaseModule
import com.wild0408.nanxinshiguang.data.di.DataStoreModule
import com.wild0408.nanxinshiguang.data.di.SharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module
import org.koin.plugin.module.dsl.startKoin

@Module(includes = [
    SharedModule::class,
    DatabaseModule::class,
    DataStoreModule::class
])
@ComponentScan("com.wild0408.nanxinshiguang")
class AppModule

@KoinApplication(modules = [AppModule::class])
class ScheduleAppConfig

class MyApplication : Application(), Configuration.Provider {

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().build()

    override fun onCreate() {
        super.onCreate()

        startKoin<ScheduleAppConfig> {
            androidLogger()
            androidContext(this@MyApplication)
            workManagerFactory()
        }
    }
}
