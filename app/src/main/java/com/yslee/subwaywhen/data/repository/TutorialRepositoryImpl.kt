package com.yslee.subwaywhen.data.repository

import com.yslee.subwaywhen.core.FixInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TutorialRepositoryImpl @Inject constructor(
    private val fixInfo: FixInfo
) : TutorialRepository {

    override fun isTutorialSeen(): Flow<Boolean> =
        fixInfo.saveSetting.map { it.tutorialSuccess }

    override suspend fun markTutorialSeen() {
        fixInfo.updateSaveSetting(fixInfo.saveSetting.value.copy(tutorialSuccess = true))
    }
}
