package com.yslee.subwaywhen.di

import com.yslee.subwaywhen.data.repository.TutorialRepository
import com.yslee.subwaywhen.data.repository.TutorialRepositoryImpl
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
}
