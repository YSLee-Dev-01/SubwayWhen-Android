package com.yslee.subwaywhen.di

import android.app.Application
import androidx.room.Room
import com.yslee.subwaywhen.data.local.room.AppDatabase
import com.yslee.subwaywhen.data.local.room.ShinbundangScheduleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(app: Application): AppDatabase =
        Room.databaseBuilder(app, AppDatabase::class.java, "subwaywhen.db").build()

    @Provides
    @Singleton
    fun provideShinbundangScheduleDao(db: AppDatabase): ShinbundangScheduleDao =
        db.shinbundangScheduleDao()
}
