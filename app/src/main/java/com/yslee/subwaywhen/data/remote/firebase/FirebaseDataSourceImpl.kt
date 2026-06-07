package com.yslee.subwaywhen.data.remote.firebase

import com.google.firebase.database.FirebaseDatabase
import com.yslee.subwaywhen.core.logger.AppLogger
import com.yslee.subwaywhen.core.logger.LogLevel
import com.yslee.subwaywhen.data.remote.dto.scheduleArrival.korail.KorailTrainNumber
import com.yslee.subwaywhen.data.remote.dto.stationSearch.SearchQueryRecommendData
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseDataSourceImpl @Inject constructor(
    private val database: FirebaseDatabase
) : FirebaseDataSource {

    @Volatile private var cachedKorailTrainNumbers: List<KorailTrainNumber>? = null

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

    override suspend fun getKorailTrainNumberList(): List<KorailTrainNumber>? {
        cachedKorailTrainNumbers?.let { return it }
        return try {
            val snapshot = database.reference.child("SubwayWhen/KorailTrainData/value").get().await()
            val result = snapshot.children.mapNotNull { child ->
                val endStation = child.child("endStation").getValue(String::class.java) ?: return@mapNotNull null
                val isFast = child.child("isFast").getValue(String::class.java) ?: return@mapNotNull null
                val line = child.child("line").getValue(String::class.java) ?: return@mapNotNull null
                val startStation = child.child("startStation").getValue(String::class.java) ?: return@mapNotNull null
                val trainNumber = child.child("trainNumber").getValue(String::class.java) ?: return@mapNotNull null
                val week = child.child("week").getValue(String::class.java) ?: return@mapNotNull null
                KorailTrainNumber(
                    endStation = endStation,
                    isFast = isFast,
                    line = line,
                    startStation = startStation,
                    trainNumber = trainNumber,
                    week = week,
                )
            }
            cachedKorailTrainNumbers = result
            result
        } catch (e: Exception) {
            AppLogger.Network.log(LogLevel.ERROR, "getKorailTrainNumberList failed: ${e.message}")
            null
        }
    }

}
