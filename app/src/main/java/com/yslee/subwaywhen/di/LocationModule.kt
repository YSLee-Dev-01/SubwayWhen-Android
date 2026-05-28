package com.yslee.subwaywhen.di

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.yslee.subwaywhen.core.location.LocationManager
import com.yslee.subwaywhen.core.location.LocationManagerImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationModule {

    @Singleton
    @Binds
    abstract fun bindLocationManager(impl: LocationManagerImpl): LocationManager

    companion object {
        @Singleton
        @Provides
        fun provideFusedLocationProviderClient(
            @ApplicationContext context: Context
        ): FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    }
}
