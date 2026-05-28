package com.yslee.subwaywhen.data.repository

import com.yslee.subwaywhen.data.network.NetworkResult
import com.yslee.subwaywhen.data.remote.dto.liveArrival.RealtimeStationArrival
import com.yslee.subwaywhen.data.remote.dto.vicinityStation.VicinityTransformData
import com.yslee.subwaywhen.data.remote.loadmodel.LoadModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VicinityRepositoryImpl @Inject constructor(
    private val loadModel: LoadModel
) : VicinityRepository {

    override suspend fun loadVicinityStations(x: Double, y: Double): List<VicinityTransformData> {
        return when (val result = loadModel.vicinityStationsLoad(x.toString(), y.toString())) {
            is NetworkResult.Success -> {
                result.data.documents
                    .filter { it.category == "SW8" }
                    .sortedBy { it.distance.toIntOrNull() ?: 0 }
                    .map { doc ->
                        VicinityTransformData(
                            id = doc.distance + doc.name,
                            name = parseStationName(doc.name),
                            line = parseLineName(doc.name),
                            distance = formatDistance(doc.distance)
                        )
                    }
            }
            is NetworkResult.Failure -> emptyList()
        }
    }

    override suspend fun loadLiveArrival(stationName: String): List<RealtimeStationArrival> {
        return when (val result = loadModel.stationArrivalRequest(stationName)) {
            is NetworkResult.Success -> result.data.realtimeArrivalList
            is NetworkResult.Failure -> emptyList()
        }
    }

    private fun parseStationName(placeName: String): String {
        val index = placeName.lastIndexOf('역')
        if (index < 0) return "정보없음"
        return placeName.substring(0, index)
    }

    private fun parseLineName(placeName: String): String {
        val index = placeName.lastIndexOf('역')
        if (index < 0) return "정보없음"
        return placeName.substring(index + 1).replace(" ", "")
    }

    private fun formatDistance(distance: String): String {
        val meters = distance.toDoubleOrNull() ?: 0.0
        val km = meters / 1000.0
        return "%.1fkm".format(km)
    }
}
