package com.yslee.subwaywhen.data.repository

import com.yslee.subwaywhen.data.remote.dto.liveArrival.RealtimeStationArrival
import com.yslee.subwaywhen.data.remote.dto.vicinityStation.VicinityTransformData

interface VicinityRepository {
    suspend fun loadVicinityStations(x: Double, y: Double): List<VicinityTransformData>
    suspend fun loadLiveArrival(stationName: String): List<RealtimeStationArrival>
}
