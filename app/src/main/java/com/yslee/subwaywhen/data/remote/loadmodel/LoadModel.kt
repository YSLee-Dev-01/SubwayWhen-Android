package com.yslee.subwaywhen.data.remote.loadmodel

import com.yslee.subwaywhen.data.network.NetworkResult
import com.yslee.subwaywhen.data.remote.dto.liveArrival.LiveStationModel
import com.yslee.subwaywhen.data.remote.dto.realtimePosition.RealtimeTrainPositionResponse
import com.yslee.subwaywhen.data.remote.dto.scheduleArrival.korail.KorailHeader
import com.yslee.subwaywhen.data.remote.dto.scheduleArrival.seoul.ScheduleStationModel
import com.yslee.subwaywhen.data.remote.dto.stationSearch.SearchStation
import com.yslee.subwaywhen.data.remote.dto.subwayNotice.SubwayNoticeResponse
import com.yslee.subwaywhen.data.remote.dto.vicinityStation.VicinityStationsData

interface LoadModel {
    suspend fun stationArrivalRequest(stationName: String): NetworkResult<LiveStationModel>
    suspend fun seoulStationScheduleLoad(
        stationCode: String,
        weekDay: String,
        upDown: String,
        stationLine: String
    ): NetworkResult<ScheduleStationModel>
    suspend fun korailScheduleLoad(
        stationCode: String,
        weekDay: String,
        korailLineCode: String
    ): NetworkResult<KorailHeader>
    suspend fun stationSearch(stationName: String): NetworkResult<SearchStation>
    suspend fun vicinityStationsLoad(x: String, y: String): NetworkResult<VicinityStationsData>
    suspend fun subwayNoticeRequest(): NetworkResult<SubwayNoticeResponse>
    suspend fun realtimePositionRequest(subwayLine: String): NetworkResult<RealtimeTrainPositionResponse>
}
