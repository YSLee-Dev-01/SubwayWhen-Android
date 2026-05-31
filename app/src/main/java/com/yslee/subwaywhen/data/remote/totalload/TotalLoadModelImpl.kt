package com.yslee.subwaywhen.data.remote.totalload

import com.yslee.subwaywhen.data.network.NetworkResult
import com.yslee.subwaywhen.data.remote.dto.liveArrival.RealtimeStationArrival
import com.yslee.subwaywhen.data.remote.dto.stationSearch.SearchStationInfo
import com.yslee.subwaywhen.data.remote.dto.vicinityStation.VicinityTransformData
import com.yslee.subwaywhen.data.remote.loadmodel.LoadModel
import javax.inject.Inject

class TotalLoadModelImpl @Inject constructor(
    private val loadModel: LoadModel
) : TotalLoadModel {

    override suspend fun stationSearch(query: String): List<SearchStationInfo> {
        return when (val result = loadModel.stationSearch(query)) {
            is NetworkResult.Success -> result.data.SearchInfoBySubwayNameService.row
            is NetworkResult.Failure -> emptyList()
        }
    }

    override suspend fun vicinityStations(x: Double, y: Double): List<VicinityTransformData> {
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

    override suspend fun liveArrivalSplit(
        stationName: String,
        line: String
    ): Pair<List<RealtimeStationArrival>, List<RealtimeStationArrival>> {
        val arrivals = when (val result = loadModel.stationArrivalRequest(stationName)) {
            is NetworkResult.Success -> result.data.realtimeArrivalList
            is NetworkResult.Failure -> emptyList()
        }
        val isNinthLine = line.contains("9호선")
        val up = arrivals.filter { if (isNinthLine) it.upDown == "하행" else it.upDown == "상행" }
        val down = arrivals.filter { if (isNinthLine) it.upDown == "상행" else it.upDown == "하행" }
        return Pair(up, down)
    }

    // ── 주변역 데이터 변환 ────────────────────────────────────────────────────

    /** "강남역 2호선" → "강남" */
    private fun parseStationName(placeName: String): String {
        val index = placeName.lastIndexOf('역')
        if (index < 0) return "정보없음"
        return placeName.substring(0, index)
    }

    /** "강남역 2호선" → "2호선" */
    private fun parseLineName(placeName: String): String {
        val index = placeName.lastIndexOf('역')
        if (index < 0) return "정보없음"
        return placeName.substring(index + 1).replace(" ", "")
    }

    /** "350" → "0.4km" */
    private fun formatDistance(distance: String): String {
        val meters = distance.toDoubleOrNull() ?: 0.0
        return "%.1fkm".format(meters / 1000.0)
    }
}
