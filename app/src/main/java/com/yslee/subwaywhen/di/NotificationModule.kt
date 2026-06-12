package com.yslee.subwaywhen.di

import com.yslee.subwaywhen.core.notification.NotificationScheduler
import com.yslee.subwaywhen.core.notification.NotificationSchedulerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {
    @Binds
    @Singleton
    abstract fun bindNotificationScheduler(impl: NotificationSchedulerImpl): NotificationScheduler
}
