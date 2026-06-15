package com.yslee.subwaywhen.data.remote.firebase

import com.yslee.subwaywhen.data.model.HolidayData
import com.yslee.subwaywhen.data.remote.dto.scheduleArrival.korail.KorailTrainNumber
import com.yslee.subwaywhen.data.remote.dto.stationSearch.SearchQueryRecommendData

interface FirebaseDataSource {
    // Returns null on failure
    suspend fun getSearchDefaultList(): List<String>?
    suspend fun getSearchQueryRecommendList(): List<SearchQueryRecommendData>?
    suspend fun getKorailTrainNumberList(): List<KorailTrainNumber>?
    suspend fun getHolidayList(): HolidayData?
    // Returns (title, contents) or null if no announcement (title == "Nil" or failure)
    suspend fun getImportantData(): Pair<String, String>?
}
