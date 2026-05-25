package com.yslee.subwaywhen.data.remote.firebase

import com.google.firebase.database.FirebaseDatabase
import com.yslee.subwaywhen.core.logger.AppLogger
import com.yslee.subwaywhen.core.logger.LogLevel
import com.yslee.subwaywhen.data.remote.dto.stationSearch.SearchQueryRecommendData
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseDataSourceImpl @Inject constructor(
    private val database: FirebaseDatabase
) : FirebaseDataSource {

    override suspend fun getSearchDefaultList(): List<String>? = try {
        val snapshot = database.reference.child("SubwayWhen/SearchDefaultList").get().await()
        @Suppress("UNCHECKED_CAST")
        snapshot.getValue(Any::class.java)
            ?.let { it as? List<*> }
            ?.filterIsInstance<String>()
    } catch (e: Exception) {
        AppLogger.Network.log(LogLevel.ERROR, "getSearchDefaultList failed: ${e.message}")
        null
    }

    override suspend fun getSearchQueryRecommendList(): List<SearchQueryRecommendData>? = try {
        val snapshot = database.reference.child("SubwayWhen/SearchQueryRecommendList/value").get().await()
        snapshot.children.mapNotNull { child ->
            val queryName = child.child("queryName").getValue(String::class.java) ?: return@mapNotNull null
            val stationName = child.child("stationName").getValue(String::class.java) ?: return@mapNotNull null
            val line = child.child("line").getValue(String::class.java) ?: return@mapNotNull null
            SearchQueryRecommendData(queryName = queryName, stationName = stationName, line = line)
        }
    } catch (e: Exception) {
        AppLogger.Network.log(LogLevel.ERROR, "getSearchQueryRecommendList failed: ${e.message}")
        null
    }

}
