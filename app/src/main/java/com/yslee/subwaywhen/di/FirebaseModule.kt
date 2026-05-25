package com.yslee.subwaywhen.di

import com.google.firebase.database.FirebaseDatabase
import com.yslee.subwaywhen.data.remote.firebase.FirebaseDataSource
import com.yslee.subwaywhen.data.remote.firebase.FirebaseDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FirebaseModule {

    @Singleton
    @Binds
    abstract fun bindFirebaseDataSource(impl: FirebaseDataSourceImpl): FirebaseDataSource

    companion object {
        @Singleton
        @Provides
        fun provideFirebaseDatabase(): FirebaseDatabase = FirebaseDatabase.getInstance()
    }
}
