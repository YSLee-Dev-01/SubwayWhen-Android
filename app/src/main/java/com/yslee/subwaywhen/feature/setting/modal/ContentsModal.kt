package com.yslee.subwaywhen.feature.setting.modal

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.yslee.subwaywhen.ui.common.PrimaryButton
import com.yslee.subwaywhen.ui.common.modal.CommonModalBottomSheet
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.MainColorDark
import com.yslee.subwaywhen.ui.theme.MainColorLight
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

@Composable
fun ContentsModal(
    contents: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
) {
    CommonModalBottomSheet(
        mainTitle = "기타",
        onDismiss = onDismiss,
        confirmButton = { animatedDismiss ->
            val isDark = isSystemInDarkTheme()
            PrimaryButton(
                text = "확인",
                containerColor = if (isDark) MainColorDark else MainColorLight,
                contentColor = MaterialTheme.colorScheme.onSurface,
                onClick = animatedDismiss,
                modifier = Modifier.fillMaxWidth().height(Dimens.modalButtonHeight),
            )
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = Dimens.modalContentMaxHeight)
                .verticalScroll(rememberScrollState()),
        ) {
            if (!isLoading && contents.isNotEmpty()) {
                Text(
                    text = contents,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = Dimens.fontSizeSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                )
            }
        }
    }
}

@Preview
@Composable
private fun ContentsModalPreview() {
    SubwayWhenTheme {
        ContentsModal(contents = "앱 문의: example@email.com", isLoading = false, onDismiss = {})
    }
}
