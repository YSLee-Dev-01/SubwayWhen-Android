package com.yslee.subwaywhen.feature.home.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.R
import com.yslee.subwaywhen.data.model.SaveStationGroup
import com.yslee.subwaywhen.ui.theme.AppIconColor
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.MainColorDark
import com.yslee.subwaywhen.ui.theme.MainColorLight
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

/**
 * iOS MainTableHeaderGroupView 대응.
 * 출근(ONE) / 퇴근(TWO) 그룹 탭.
 * 선택된 탭: 70%, 미선택: 30%. animateFloatAsState로 너비 전환.
 */
@Composable
fun HomeGroupTabBar(
    currentGroup: SaveStationGroup,
    onGroupTap: (SaveStationGroup) -> Unit,
    modifier: Modifier = Modifier,
) {
    val oneFraction by animateFloatAsState(
        targetValue = if (currentGroup == SaveStationGroup.ONE) 0.7f else 0.3f,
        animationSpec = tween(durationMillis = Dimens.animationDurationMs),
        label = "groupTabOneFraction",
    )
    val mainBg = if (isSystemInDarkTheme()) MainColorDark else MainColorLight

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(color = mainBg, shape = RoundedCornerShape(Dimens.cornerRadius)),
    ) {
        // ONE(출근) 탭
        GroupTabButton(
            label = stringResource(R.string.home_group_one),
            isSelected = currentGroup == SaveStationGroup.ONE,
            onTap = { onGroupTap(SaveStationGroup.ONE) },
            modifier = Modifier.weight(oneFraction),
        )

        // TWO(퇴근) 탭
        GroupTabButton(
            label = stringResource(R.string.home_group_two),
            isSelected = currentGroup == SaveStationGroup.TWO,
            onTap = { onGroupTap(SaveStationGroup.TWO) },
            modifier = Modifier.weight(1f - oneFraction),
        )
    }
}

@Composable
private fun GroupTabButton(
    label: String,
    isSelected: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(Dimens.cornerRadius),
        color = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.background.copy(alpha = 0f),
        border = if (isSelected) BorderStroke(1.2.dp, AppIconColor) else null,
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onTap),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = if (isSelected) Dimens.fontSizeLarge else Dimens.fontSizeMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onBackground
                else
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            )
        }
    }
}

@Preview(name = "HomeGroupTabBar - ONE 선택 - Light", showBackground = true)
@Composable
private fun HomeGroupTabBarOneLightPreview() {
    SubwayWhenTheme(darkTheme = false) {
        HomeGroupTabBar(
            currentGroup = SaveStationGroup.ONE,
            onGroupTap = {},
            modifier = Modifier.padding(horizontal = Dimens.paddingLR, vertical = Dimens.paddingTB),
        )
    }
}

@Preview(name = "HomeGroupTabBar - TWO 선택 - Light", showBackground = true)
@Composable
private fun HomeGroupTabBarTwoLightPreview() {
    SubwayWhenTheme(darkTheme = false) {
        HomeGroupTabBar(
            currentGroup = SaveStationGroup.TWO,
            onGroupTap = {},
            modifier = Modifier.padding(horizontal = Dimens.paddingLR, vertical = Dimens.paddingTB),
        )
    }
}

@Preview(name = "HomeGroupTabBar - ONE 선택 - Dark", showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun HomeGroupTabBarOneDarkPreview() {
    SubwayWhenTheme(darkTheme = true) {
        HomeGroupTabBar(
            currentGroup = SaveStationGroup.ONE,
            onGroupTap = {},
            modifier = Modifier.padding(horizontal = Dimens.paddingLR, vertical = Dimens.paddingTB),
        )
    }
}

@Preview(name = "HomeGroupTabBar - TWO 선택 - Dark", showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun HomeGroupTabBarTwoDarkPreview() {
    SubwayWhenTheme(darkTheme = true) {
        HomeGroupTabBar(
            currentGroup = SaveStationGroup.TWO,
            onGroupTap = {},
            modifier = Modifier.padding(horizontal = Dimens.paddingLR, vertical = Dimens.paddingTB),
        )
    }
}
