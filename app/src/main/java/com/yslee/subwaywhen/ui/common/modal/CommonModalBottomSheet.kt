package com.yslee.subwaywhen.ui.common.modal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

/**
 * iOS ModalVCCustom 대응.
 * Material3 ModalBottomSheet 래퍼.
 * 드래그·백드롭 탭 dismiss, 상단 둥근 모서리는 ModalBottomSheet 표준 동작으로 처리.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonModalBottomSheet(
    mainTitle: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    subTitle: String? = null,
    confirmButton: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp),
        modifier = modifier,
    ) {
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
                confirmButton()
            }

            Spacer(modifier = Modifier.height(Dimens.paddingLR))
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
            confirmButton = {
                ModalSubButton(
                    text = "확인",
                    bgColor = Color(0xFF2196F3),
                    textColor = Color.White,
                    onClick = {},
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
