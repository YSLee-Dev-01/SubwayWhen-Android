package com.yslee.subwaywhen.feature.setting.modal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.ui.common.PrimaryButton
import com.yslee.subwaywhen.ui.common.modal.CommonModalBottomSheet
import com.yslee.subwaywhen.ui.theme.AppIconColor
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.MainColorDark
import com.yslee.subwaywhen.ui.theme.MainColorLight
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

private val trainIcons = listOf("🚃", "🚂", "🚈", "🚅", "🚋", "🚗", "🚙", "🏎️")

@Composable
fun TrainIconModal(
    currentIcon: String,
    onIconSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var selected by remember(currentIcon) { mutableStateOf(currentIcon) }
    val isDark = isSystemInDarkTheme()
    val mainColor = if (isDark) MainColorDark else MainColorLight

    CommonModalBottomSheet(
        mainTitle = "열차 아이콘",
        subTitle = "상세화면의 열차 아이콘을 변경하는 기능이에요.",
        onDismiss = onDismiss,
        confirmButton = { animatedDismiss ->
            PrimaryButton(
                text = "확인",
                containerColor = mainColor,
                contentColor = MaterialTheme.colorScheme.onSurface,
                onClick = { onIconSelected(selected); animatedDismiss() },
                modifier = Modifier.fillMaxWidth().height(Dimens.modalButtonHeight),
            )
        },
    ) {
        // 선택 아이콘 미리보기 (iOS SettingTrainIconModalView 대응)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(Dimens.cornerRadius))
                .background(mainColor),
        ) {
            // 선로 + 역 원 + 열차 아이콘 (레이어 순서로 z-order 보장)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
            ) {
                // 레이어 1: 선로
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .background(AppIconColor),
                )
                // 레이어 2: 역 원형들
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(15.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                            .border(1.dp, AppIconColor, CircleShape),
                    )
                    Box(
                        modifier = Modifier
                            .size(15.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                            .border(1.dp, AppIconColor, CircleShape),
                    )
                }
                // 레이어 3: 열차 아이콘 (항상 원형 위에 그려짐)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(Modifier.size(15.dp))
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(15.dp),
                    ) {
                        Text(
                            text = selected,
                            fontSize = (Dimens.fontSizeBigTitle.value * 2).sp,
                            modifier = Modifier.offset(y = (-13).dp),
                        )
                    }
                }
            }
            // 레이블 (하단)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "현재역",
                    fontSize = Dimens.fontSizeSmall,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "다음역",
                    fontSize = Dimens.fontSizeSmall,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(trainIcons) { icon ->
                val isSelected = icon == selected
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize(0.85f)
                            .background(
                                color = if (isSelected) AppIconColor.copy(alpha = 0.1f) else mainColor,
                                shape = CircleShape,
                            )
                            .then(
                                if (isSelected) Modifier.border(
                                    width = 2.dp,
                                    color = AppIconColor,
                                    shape = CircleShape,
                                ) else Modifier
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { selected = icon },
                            ),
                    ) {
                        Text(text = icon, fontSize = Dimens.fontSizeBigTitle)
                    }
                }
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Preview
@Composable
private fun TrainIconModalPreview() {
    SubwayWhenTheme {
        TrainIconModal(currentIcon = "🚃", onIconSelected = {}, onDismiss = {})
    }
}
