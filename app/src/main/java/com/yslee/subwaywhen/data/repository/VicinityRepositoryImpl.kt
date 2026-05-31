package com.yslee.subwaywhen.data.repository

import com.yslee.subwaywhen.data.remote.dto.liveArrival.RealtimeStationArrival
import com.yslee.subwaywhen.data.remote.dto.vicinityStation.VicinityTransformData
import com.yslee.subwaywhen.data.remote.totalload.TotalLoadModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VicinityRepositoryImpl @Inject constructor(
    private val totalLoadModel: TotalLoadModel
) : VicinityRepository {

    override suspend fun loadVicinityStations(x: Double, y: Double): List<VicinityTransformData> =
        totalLoadModel.vicinityStations(x, y)

    override suspend fun loadLiveArrival(
        stationName: String,
        line: String
    ): Pair<List<RealtimeStationArrival>, List<RealtimeStationArrival>> =
        totalLoadModel.liveArrivalSplit(stationName, line)
}
