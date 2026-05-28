package com.yslee.subwaywhen.di

import com.yslee.subwaywhen.data.repository.LocalDataRepository
import com.yslee.subwaywhen.data.repository.LocalDataRepositoryImpl
import com.yslee.subwaywhen.data.repository.SearchRepository
import com.yslee.subwaywhen.data.repository.SearchRepositoryImpl
import com.yslee.subwaywhen.data.repository.TutorialRepository
import com.yslee.subwaywhen.data.repository.TutorialRepositoryImpl
import com.yslee.subwaywhen.data.repository.VicinityRepository
import com.yslee.subwaywhen.data.repository.VicinityRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Singleton
    @Binds
    abstract fun bindTutorialRepository(
        impl: TutorialRepositoryImpl
    ): TutorialRepository

    @Singleton
    @Binds
    abstract fun bindLocalDataRepository(
        impl: LocalDataRepositoryImpl
    ): LocalDataRepository

    @Singleton
    @Binds
    abstract fun bindSearchRepository(impl: SearchRepositoryImpl): SearchRepository

    @Singleton
    @Binds
    abstract fun bindVicinityRepository(impl: VicinityRepositoryImpl): VicinityRepository
}
