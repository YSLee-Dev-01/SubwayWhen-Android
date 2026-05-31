package com.yslee.subwaywhen.data.repository

import com.yslee.subwaywhen.data.remote.dto.liveArrival.RealtimeStationArrival
import com.yslee.subwaywhen.data.remote.dto.vicinityStation.VicinityTransformData

interface VicinityRepository {
    suspend fun loadVicinityStations(x: Double, y: Double): List<VicinityTransformData>

    /**
     * 실시간 도착 정보를 상행/하행으로 분리하여 반환.
     * first = 상행(내선), second = 하행(외선)
     */
    suspend fun loadLiveArrival(
        stationName: String,
        line: String
    ): Pair<List<RealtimeStationArrival>, List<RealtimeStationArrival>>
}
