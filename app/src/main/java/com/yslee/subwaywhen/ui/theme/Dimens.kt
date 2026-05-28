package com.yslee.subwaywhen.ui.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object Dimens {
    val cornerRadius = 15.dp
    val paddingLR = 20.dp
    val paddingTB = 7.5.dp
    val paddingInner = 16.dp

    const val animationDurationMs = 250
    const val animationScale = 0.94f

    val titleOffsetY = 7.5.dp
    val searchSectionGap = 15.dp
    val stationLineCircleSize = 70.dp
    val vicinityStationCircleSize = 45.dp
    val vicinityStationCircleSizeLarge = 65.dp
    val tabBarBottomPadding = 80.dp
    val disposableButtonWidth = 60.dp
    val disposableButtonHeight = 30.dp
    val modalButtonHeight = 50.dp
    val modalHorizontalMargin = 10.dp
    val disposableViewGap = 10.dp
    val vicinityListButtonHeight = 40.dp   // 목록으로 확인하기 버튼 (iOS AnimationButtonInSUI 자연 높이 대응)
    val vicinityMiniBarWidth = 45.dp       // mini-row 가로 바 너비 (iOS minWidth: 45)
    val vicinityMiniBarHeight = 7.5.dp     // mini-row 가로 바 높이 (iOS height: 7.5)
    val vicinityActionIconSize = 18.dp     // 상세 카드 버튼 아이콘 (22dp의 ~80%)

    val fontSizeSuperSmall = 9.sp
    val fontSizeMediumSmall = 11.sp
    val fontSizeSmall = 13.sp
    val fontSizeMedium = 15.sp
    val fontSizeLarge = 17.sp
    val fontSizeMainTitleMedium = 21.sp
    val fontSizeMainTitle = 23.sp
    val fontSizeBigTitle = 27.sp
}
