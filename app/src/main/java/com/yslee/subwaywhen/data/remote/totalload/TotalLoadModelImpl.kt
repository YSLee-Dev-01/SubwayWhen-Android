package com.yslee.subwaywhen.data.remote.totalload

import android.content.Context
import com.yslee.subwaywhen.data.model.SaveStation
import com.yslee.subwaywhen.data.network.NetworkResult
import com.yslee.subwaywhen.data.remote.dto.liveArrival.LiveStationModel
import com.yslee.subwaywhen.data.remote.dto.liveArrival.RealtimeStationArrival
import com.yslee.subwaywhen.data.local.room.ShinbundangScheduleDao
import com.yslee.subwaywhen.data.local.room.ShinbundangScheduleEntity
import com.yslee.subwaywhen.data.remote.dto.scheduleArrival.korail.ProcessedKorailSchedule
import com.yslee.subwaywhen.data.remote.dto.scheduleArrival.seoul.ScheduleStationModel
import com.yslee.subwaywhen.data.remote.dto.scheduleArrival.shinbundang.ProcessedShinbundangSchedule
import com.yslee.subwaywhen.data.remote.dto.scheduleArrival.shinbundang.ShinbundangSchedule
import com.yslee.subwaywhen.data.remote.dto.stationSearch.SearchStationInfo
import com.yslee.subwaywhen.data.remote.dto.vicinityStation.VicinityTransformData
import com.yslee.subwaywhen.data.remote.firebase.FirebaseDataSource
import com.yslee.subwaywhen.data.remote.loadmodel.LoadModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject

@Serializable
private data class StationIdEntry(
    @SerialName("lineId")     val lineId: String,
    @SerialName("stationId")  val stationId: String,
    @SerialName("stationName") val stationName: String,
)

class TotalLoadModelImpl @Inject constructor(
    private val loadModel: LoadModel,
    private val firebaseDataSource: FirebaseDataSource,
    private val shinbundangScheduleDao: ShinbundangScheduleDao,
    @ApplicationContext private val context: Context,
) : TotalLoadModel {

    /** stationId → stationName 맵. assets/station_id_list.json lazy load. */
    private val stationIdMap: Map<String, String> by lazy {
        val json = context.assets.open("station_id_list.json").bufferedReader().readText()
        Json.decodeFromString<List<StationIdEntry>>(json).associate { it.stationId to it.stationName }
    }

    /**
     * iOS TotalLoadModel.nextAndBackStationSearch() + removingSubName() 대응.
     * 역명의 부역명(괄호 부분) 제거 → e.g. "쌍용(나사렛대)" → "쌍용".
     */
    private fun lookupStationName(id: String): String =
        stationIdMap[id]?.replace(Regex("\\(.*?\\)"), "")?.trim() ?: ""

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
        // 환승역에서 선택한 노선의 데이터만 표시 (iOS: requestModel.line.lineCode == x.subWayId)
        val lineCode = lineNameToCode(line)
        val filtered = if (lineCode.isNotEmpty()) arrivals.filter { it.subWayId == lineCode } else arrivals
        // 2호선은 "내선"/"외선", 9호선은 방향 반전, 그 외는 "상행"/"하행"
        // iOS: line.upDownText(isUp: true/false) 대응
        // iOS: 공항철도(1065)는 nextAndBackStationSearch에서 back/next 반전
        val isAirport = lineCode == "1065"
        val up   = filtered.filter { it.upDown == upDirectionText(line) }
            .map { it.copy(backStationName = if (isAirport) lookupStationName(it.nextStationId) else lookupStationName(it.backStationId)) }
        val down = filtered.filter { it.upDown == downDirectionText(line) }
            .map { it.copy(backStationName = if (isAirport) lookupStationName(it.nextStationId) else lookupStationName(it.backStationId)) }
        return Pair(up, down)
    }

    override fun arrivalDataLoad(stations: List<SaveStation>): Flow<IndexedValue<NetworkResult<LiveStationModel>>> = channelFlow {
        stations.forEachIndexed { index, station ->
            launch {
                val result = loadModel.stationArrivalRequest(station.stationName)
                send(IndexedValue(index, result))
            }
        }
    }

    override suspend fun seoulScheduleLoad(station: SaveStation, weekDay: String): NetworkResult<ScheduleStationModel> {
        return loadModel.seoulStationScheduleLoad(
            stationCode = station.stationCode,
            weekDay = weekDay,
            upDown = station.updnLine,
            stationLine = station.lineCode
        )
    }

    override suspend fun korailScheduleLoad(station: SaveStation, weekDay: String): NetworkResult<List<ProcessedKorailSchedule>> {
        // iOS 패턴: 첫 시도 실패 시 소문자 stationCode로 재시도
        // lnCd = korailCode (K1/K2/K4), stinCd = stationCode (K154 등 실제 역코드)
        val rawResult = loadModel.korailScheduleLoad(
            stationCode = station.stationCode,
            weekDay = weekDay,
            korailLineCode = station.korailCode
        )
        val body = when (rawResult) {
            is NetworkResult.Success -> rawResult.data.body
            is NetworkResult.Failure -> {
                val retry = loadModel.korailScheduleLoad(
                    stationCode = station.stationCode.lowercase(),
                    weekDay = weekDay,
                    korailLineCode = station.korailCode
                )
                when (retry) {
                    is NetworkResult.Success -> retry.data.body
                    is NetworkResult.Failure -> return NetworkResult.Failure(retry.error)
                }
            }
        }

        val trainNumbers = firebaseDataSource.getKorailTrainNumberList() ?: emptyList()
        val isWeekday = weekDay == "weekday"
        val filteredNumbers = trainNumbers.filter { if (isWeekday) it.week == "평일" else it.week == "주말" }

        // 짝수 trainCode 마지막 자리 = 상행, 홀수 = 하행
        val upDownFiltered = body.filter { schedule ->
            val time = schedule.time?.takeIf { it.isNotEmpty() } ?: return@filter false
            val lastDigit = schedule.trainCode.lastOrNull()?.digitToIntOrNull() ?: return@filter false
            val isUp = lastDigit % 2 == 0
            if (isUp) station.updnLine == "상행" else station.updnLine == "하행"
        }

        // 트레인 넘버 조인으로 lastStation / startStation / 급행 주입
        val processed = upDownFiltered.mapNotNull { schedule ->
            val time = schedule.time ?: return@mapNotNull null
            val tn = filteredNumbers.firstOrNull { it.trainNumber == schedule.trainCode }
            ProcessedKorailSchedule(
                time = time,
                lastStation = tn?.endStation ?: "",
                startStation = tn?.startStation ?: "",
                isFast = if (tn?.isFast == "급행") "급행" else "",
            )
        }

        // exceptionLastStation 필터 후 시간 오름차순 정렬
        val sorted = processed
            .filter { station.exceptionLastStation.isEmpty() || !station.exceptionLastStation.contains(it.lastStation) }
            .sortedBy { it.time.toIntOrNull() ?: 0 }

        return NetworkResult.Success(sorted)
    }

    override suspend fun shinbundangScheduleLoad(
        station: SaveStation,
        weekDay: String,
        isDisposable: Boolean,
    ): NetworkResult<List<ProcessedShinbundangSchedule>> {
        // 1. Firebase에서 버전 조회
        val firebaseVersion = loadModel.shinbundangScheduleVersionRequest() ?: 0.0

        // 2. Room에서 로컬 캐시 조회
        val cached = try { shinbundangScheduleDao.load(station.stationName) } catch (e: Exception) { null }
        val cachedVersion = cached?.scheduleVersion?.toDoubleOrNull() ?: 0.0

        // 3. 버전 비교: 로컬이 최신이면 캐시 사용, 아니면 Firebase 조회
        val rawSchedules: List<ShinbundangSchedule> = if (cachedVersion >= firebaseVersion && cached != null) {
            try {
                Json.decodeFromString(kotlinx.serialization.builtins.ListSerializer(ShinbundangSchedule.serializer()), cached.scheduleData)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            val fetched = loadModel.shinbundangScheduleRequest(station.stationName) ?: emptyList()

            // 4. isDisposable = false이면 Room에 저장
            if (!isDisposable && fetched.isNotEmpty()) {
                try {
                    shinbundangScheduleDao.insert(
                        ShinbundangScheduleEntity(
                            stationName = station.stationName,
                            scheduleData = Json.encodeToString(kotlinx.serialization.builtins.ListSerializer(ShinbundangSchedule.serializer()), fetched),
                            scheduleVersion = firebaseVersion.toString(),
                        )
                    )
                } catch (e: Exception) { /* 저장 실패 시 무시 */ }
            }
            fetched
        }

        // 5. 방향 / 요일 / exceptionLastStation 필터
        val requestWeek = if (weekDay == "weekday") "평일" else "주말"
        val filtered = rawSchedules.filter { schedule ->
            schedule.updown == station.updnLine &&
            schedule.week == requestWeek &&
            !station.exceptionLastStation.contains(schedule.endStation)
        }

        // 6. startTime 오름차순 정렬 후 ProcessedShinbundangSchedule 매핑
        val result = filtered
            .sortedBy { it.startTime.split(":").joinToString("").toIntOrNull() ?: 0 }
            .map { ProcessedShinbundangSchedule(it.startTime, it.startStation, it.endStation) }

        return NetworkResult.Success(result)
    }

    override suspend fun getLicenses(): List<String> = loadModel.getLicenses() ?: emptyList()

    override suspend fun getContents(): String = loadModel.getContents() ?: ""

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

    /**
     * iOS SubwayLineData.upDownText(isUp: true) 대응.
     * 2호선: "내선", 9호선 반전: "하행", 그 외: "상행"
     */
    private fun upDirectionText(line: String) = when {
        line == "2호선"          -> "내선"
        line.contains("9호선") -> "하행"
        else                    -> "상행"
    }

    /**
     * iOS SubwayLineData.upDownText(isUp: false) 대응.
     * 2호선: "외선", 9호선 반전: "상행", 그 외: "하행"
     */
    private fun downDirectionText(line: String) = when {
        line == "2호선"          -> "외선"
        line.contains("9호선") -> "상행"
        else                    -> "하행"
    }

    /**
     * VicinityTransformData.line → Seoul Metro API subWayId.
     * 데이터 레이어가 ui.common.SubwayLineMapper를 import하지 않도록 독립 구현.
     * 미지원 노선은 "" 반환 → 필터 미적용.
     */
    private fun lineNameToCode(line: String): String {
        // 숫자 호선: "2호선" → "1002", "9호선" → "1009"
        if (line.firstOrNull()?.isDigit() == true) {
            val number = line.filter { it.isDigit() }
            return "100$number"
        }
        return when (line) {
            "경의중앙선" -> "1063"
            "공항철도"   -> "1065"
            "경춘선"     -> "1067"
            "수인분당선" -> "1075"
            "신분당선"   -> "1077"
            "우이신설선" -> "1092"
            "서해선"     -> "1093"
            "신림선"     -> "1094"
            "경강선"     -> "1081"
            "GTX-A"      -> "1032"
            else -> ""
        }
    }
}
