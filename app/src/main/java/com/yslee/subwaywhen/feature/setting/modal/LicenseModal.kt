package com.yslee.subwaywhen.feature.setting.modal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.ui.common.PrimaryButton
import com.yslee.subwaywhen.ui.common.modal.CommonModalBottomSheet
import com.yslee.subwaywhen.ui.theme.AppIconColor
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

@Composable
fun LicenseModal(
    licenses: List<String>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
) {
    CommonModalBottomSheet(
        mainTitle = "오픈 라이선스",
        onDismiss = onDismiss,
        confirmButton = { animatedDismiss ->
            PrimaryButton(
                text = "확인",
                containerColor = AppIconColor,
                onClick = animatedDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            if (!isLoading && licenses.isNotEmpty()) {
                licenses.forEachIndexed { index, license ->
                    Text(
                        text = license,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = Dimens.fontSizeSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                    )
                    if (index < licenses.lastIndex) {
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun LicenseModalPreview() {
    SubwayWhenTheme {
        LicenseModal(
            licenses = listOf("라이선스 A", "라이선스 B"),
            isLoading = false,
            onDismiss = {},
        )
    }
}
