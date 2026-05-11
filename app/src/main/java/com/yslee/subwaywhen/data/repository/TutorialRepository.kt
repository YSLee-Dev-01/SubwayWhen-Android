package com.yslee.subwaywhen.data.repository

import kotlinx.coroutines.flow.Flow

interface TutorialRepository {
    fun isTutorialSeen(): Flow<Boolean>
    suspend fun markTutorialSeen()
}
