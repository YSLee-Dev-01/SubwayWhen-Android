package com.yslee.subwaywhen.ui.common.modal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

/**
 * iOS ModalVCCustom 대응.
 * - 좌우 10dp 여백, containerColor Transparent로 Modal 외부에 topDecoration 표시 지원.
 * - topDecoration: Modal 흰 배경 외부 상단에 표시할 컴포저블 (DisposableView 등).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonModalBottomSheet(
    mainTitle: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    subTitle: String? = null,
    topDecoration: (@Composable () -> Unit)? = null,
    sheetHeight: Dp? = null,
    // 외부에서 animated dismiss(슬라이드 다운) 액션을 받아 저장하기 위한 콜백
    onAnimatedDismissReady: ((suspend () -> Unit) -> Unit)? = null,
    // dismiss: animatedDismiss 콜백. sheetState.hide() 후 onDismiss() 순서로 호출해 슬라이드-다운 애니메이션을 보장한다.
    confirmButton: (@Composable (() -> Unit) -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val animatedDismissSuspend: suspend () -> Unit = { sheetState.hide(); onDismiss() }
    // 외부로 animated dismiss 액션 전달 (SideEffect로 composition 완료 후 실행)
    if (onAnimatedDismissReady != null) {
        androidx.compose.runtime.SideEffect { onAnimatedDismissReady(animatedDismissSuspend) }
    }
    val animatedDismiss: () -> Unit = { scope.launch { sheetState.hide(); onDismiss() } }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RectangleShape,
        containerColor = Color.Transparent,
        dragHandle = null,
        modifier = modifier.padding(horizontal = Dimens.modalHorizontalMargin),
    ) {
        Column(
            modifier = if (sheetHeight != null) Modifier.height(sheetHeight) else Modifier,
        ) {
            if (topDecoration != null) {
                topDecoration()
                Spacer(modifier = Modifier.height(Dimens.disposableViewGap))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (sheetHeight != null) Modifier.weight(1f) else Modifier)
                    .clip(RoundedCornerShape(25.dp))
                    .background(color = MaterialTheme.colorScheme.surface),
            ) {
                // Drag handle pill
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(5.dp)
                        .align(Alignment.CenterHorizontally)
                        .background(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(2.5.dp),
                        ),
                )
                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.paddingLR),
                ) {
                    Text(
                        text = mainTitle,
                        fontSize = Dimens.fontSizeMainTitleMedium,
                        fontWeight = FontWeight.ExtraBold,
                    )

                    if (subTitle != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = subTitle,
                            fontSize = Dimens.fontSizeSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Spacer(modifier = Modifier.height(Dimens.paddingLR))

                    content()

                    if (confirmButton != null) {
                        Spacer(modifier = Modifier.height(Dimens.paddingLR))
                        confirmButton(animatedDismiss)
                    }

                    Spacer(modifier = Modifier.height(Dimens.paddingLR))
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "CommonModalBottomSheet - Light", showBackground = true)
@Composable
private fun CommonModalBottomSheetLightPreview() {
    SubwayWhenTheme(darkTheme = false) {
        CommonModalBottomSheet(
            mainTitle = "알림 설정",
            subTitle = "도착 알림을 설정합니다.",
            onDismiss = {},
            confirmButton = { dismiss ->
                ModalSubButton(
                    text = "확인",
                    bgColor = Color(0xFF2196F3),
                    textColor = MaterialTheme.colorScheme.onPrimary,
                    onClick = dismiss,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
        ) {
            Text(text = "내용 영역")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "CommonModalBottomSheet - Dark", showBackground = true, backgroundColor = 0xFF1C1C1E)
@Composable
private fun CommonModalBottomSheetDarkPreview() {
    SubwayWhenTheme(darkTheme = true) {
        CommonModalBottomSheet(
            mainTitle = "알림 설정",
            subTitle = null,
            onDismiss = {},
            confirmButton = null,
        ) {
            Text(text = "내용 영역")
        }
    }
}
