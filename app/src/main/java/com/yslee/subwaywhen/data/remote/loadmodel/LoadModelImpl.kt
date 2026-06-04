package com.yslee.subwaywhen.data.remote.loadmodel

import com.yslee.subwaywhen.data.network.NetworkError
import com.yslee.subwaywhen.data.network.NetworkManager
import com.yslee.subwaywhen.data.network.NetworkResult
import com.yslee.subwaywhen.data.network.TokenKey
import com.yslee.subwaywhen.data.network.TokenProvider
import com.yslee.subwaywhen.data.network.requestData
import com.yslee.subwaywhen.data.remote.dto.liveArrival.LiveStationModel
import com.yslee.subwaywhen.data.remote.dto.realtimePosition.RealtimeTrainPositionResponse
import com.yslee.subwaywhen.data.remote.dto.scheduleArrival.korail.KorailHeader
import com.yslee.subwaywhen.data.remote.dto.scheduleArrival.seoul.ScheduleStationModel
import com.yslee.subwaywhen.data.remote.dto.stationSearch.SearchStation
import com.yslee.subwaywhen.data.remote.dto.subwayNotice.SubwayNoticeResponse
import com.yslee.subwaywhen.data.remote.dto.vicinityStation.VicinityStationsData
import javax.inject.Inject

class LoadModelImpl @Inject constructor(
    private val networkManager: NetworkManager,
    private val tokenProvider: TokenProvider
) : LoadModel {

    override suspend fun stationArrivalRequest(stationName: String): NetworkResult<LiveStationModel> {
        val name = arrivalStationNameCheck(stationName).replace(" ", "")
        val url = "http://swopenapi.seoul.go.kr/api/subway/${tokenProvider.token(TokenKey.LIVE)}/json/realtimeStationArrival/0/50/$name"
        return networkManager.requestData(url)
    }

    override suspend fun seoulStationScheduleLoad(
        stationCode: String,
        weekDay: String,
        upDown: String,
        stationLine: String
    ): NetworkResult<ScheduleStationModel> {
        if (stationCode.contains("K") || stationCode.contains("D")) {
            return NetworkResult.Failure(NetworkError.BadUrl)
        }

        // 9호선은 상하행이 반대 (iOS LoadModel.swift 동일 처리)
        // stationLine은 lineCode("1009")로 전달됨
        val inOut = if (stationLine == "1009") {
            if (upDown == "상행" || upDown == "내선") 2 else 1
        } else {
            if (upDown == "상행" || upDown == "내선") 1 else 2
        }

        val weekdayCode = when (weekDay) {
            "weekday" -> 1
            "saturday" -> 2
            else -> 3
        }

        val url = "http://openapi.seoul.go.kr:8088/${tokenProvider.token(TokenKey.SEOUL)}/json/SearchSTNTimeTableByFRCodeService/1/500/$stationCode/$weekdayCode/$inOut"
        return networkManager.requestData(url)
    }

    override suspend fun korailScheduleLoad(
        stationCode: String,
        weekDay: String,
        korailLineCode: String
    ): NetworkResult<KorailHeader> {
        val dayCd = if (weekDay == "weekday") 8 else 9
        val url = "https://openapi.kric.go.kr/openapi/trainUseInfo/subwayTimetable"
        return networkManager.requestData(
            url,
            headers = emptyMap(),
            query = mapOf(
                "serviceKey" to tokenProvider.token(TokenKey.KORAIL),
                "format" to "JSON",
                "railOprIsttCd" to "KR",
                "dayCd" to dayCd.toString(),
                "lnCd" to korailLineCode,
                "stinCd" to stationCode
            )
        )
    }

    override suspend fun stationSearch(stationName: String): NetworkResult<SearchStation> {
        val url = "http://openapi.seoul.go.kr:8088/${tokenProvider.token(TokenKey.SEOUL)}/json/SearchInfoBySubwayNameService/1/5/$stationName"
        return networkManager.requestData(url)
    }

    override suspend fun vicinityStationsLoad(x: String, y: String): NetworkResult<VicinityStationsData> {
        val url = "https://dapi.kakao.com/v2/local/search/category.json"
        return networkManager.requestData(
            url,
            headers = mapOf("Authorization" to "KakaoAK ${tokenProvider.token(TokenKey.KAKAO)}"),
            query = mapOf(
                "category_group_code" to "SW8",
                "radius" to "3000",
                "x" to x,
                "y" to y
            )
        )
    }

    override suspend fun subwayNoticeRequest(): NetworkResult<SubwayNoticeResponse> {
        val url = "http://openapi.seoul.go.kr:8088/${tokenProvider.token(TokenKey.SEOUL)}/json/getNtceList/1/5/"
        return networkManager.requestData(url)
    }

    override suspend fun realtimePositionRequest(subwayLine: String): NetworkResult<RealtimeTrainPositionResponse> {
        val url = "http://swopenapi.seoul.go.kr/api/subway/${tokenProvider.token(TokenKey.REALTIME)}/json/realtimePosition/0/100/$subwayLine"
        return networkManager.requestData(url)
    }

    private fun arrivalStationNameCheck(stationName: String): String {
        val nameMap = mapOf(
            "쌍용" to "쌍용(나사렛대)",
            "총신대입구" to "총신대입구(이수)",
            "신정" to "신정(은행정)",
            "오목교" to "오목교(목동운동장앞)",
            "군자" to "군자(능동)",
            "아차산" to "아차산(어린이대공원후문)",
            "광나루" to "광나루(장신대)",
            "천호" to "천호(풍납토성)",
            "굽은다리" to "굽은다리(강동구민회관앞)",
            "새절" to "새절(신사)",
            "증산" to "증산(명지대앞)",
            "월드컵경기장" to "월드컵경기장(성산)",
            "대흥" to "대흥(서강대앞)",
            "안암" to "안암(고대병원앞)",
            "월곡" to "월곡(동덕여대)",
            "상월곡" to "상월곡(한국과학기술연구원)",
            "화랑대" to "화랑대(서울여대입구)",
            "공릉" to "공릉(서울산업대입구)",
            "어린이대공원" to "어린이대공원(세종대)",
            "이수" to "총신대입구(이수)",
            "숭실대입구" to "숭실대입구(살피재)",
            "몽촌토성" to "몽촌토성(평화의문)",
            "남한산성입구" to "남한산성입구(성남법원, 검찰청)",
            "서울" to "서울역"
        )
        return nameMap.getOrDefault(stationName, stationName)
    }
}
