package com.yslee.subwaywhen.data.remote.totalload

import com.yslee.subwaywhen.data.model.SaveStation
import com.yslee.subwaywhen.data.network.NetworkResult
import com.yslee.subwaywhen.data.remote.dto.liveArrival.LiveStationModel
import com.yslee.subwaywhen.data.remote.dto.liveArrival.RealtimeStationArrival
import com.yslee.subwaywhen.data.remote.dto.scheduleArrival.korail.ProcessedKorailSchedule
import com.yslee.subwaywhen.data.remote.dto.scheduleArrival.seoul.ScheduleStationModel
import com.yslee.subwaywhen.data.remote.dto.stationSearch.SearchStationInfo
import com.yslee.subwaywhen.data.remote.dto.vicinityStation.VicinityTransformData
import kotlinx.coroutines.flow.Flow

interface TotalLoadModel {
    suspend fun stationSearch(query: String): List<SearchStationInfo>
    suspend fun vicinityStations(x: Double, y: Double): List<VicinityTransformData>

    /**
     * 역 실시간 도착 정보를 호선 기준으로 상행/하행으로 분리하여 반환.
     * first = 상행(내선), second = 하행(외선)
     * 9호선은 실제 방향이 반전되므로 내부에서 처리.
     */
    suspend fun liveArrivalSplit(
        stationName: String,
        line: String
    ): Pair<List<RealtimeStationArrival>, List<RealtimeStationArrival>>

    fun arrivalDataLoad(stations: List<SaveStation>): Flow<IndexedValue<NetworkResult<LiveStationModel>>>

    suspend fun seoulScheduleLoad(station: SaveStation, weekDay: String): NetworkResult<ScheduleStationModel>

    suspend fun korailScheduleLoad(station: SaveStation, weekDay: String): NetworkResult<List<ProcessedKorailSchedule>>
}
