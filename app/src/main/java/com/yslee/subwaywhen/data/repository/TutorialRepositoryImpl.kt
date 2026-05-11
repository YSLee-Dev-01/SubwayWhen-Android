package com.yslee.subwaywhen.data.repository

import com.yslee.subwaywhen.data.local.SettingLocalDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TutorialRepositoryImpl @Inject constructor(
    private val dataSource: SettingLocalDataSource
) : TutorialRepository {

    override fun isTutorialSeen(): Flow<Boolean> =
        dataSource.getSaveSetting().map { it.tutorialSuccess }

    override suspend fun markTutorialSeen() {
        dataSource.updateTutorialSeen(true)
    }
}
