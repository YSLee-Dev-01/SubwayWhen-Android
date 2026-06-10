package com.yslee.subwaywhen.feature.setting.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

@Composable
fun SettingTextFieldRow(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    onFocusLost: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(59.dp)
            .padding(horizontal = Dimens.paddingInner),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = Dimens.fontSizeMedium),
        )
        Spacer(Modifier.weight(1f))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                fontSize = Dimens.fontSizeMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End,
            ),
            singleLine = true,
            modifier = Modifier
                .width(40.dp)
                .onFocusChanged { if (!it.isFocused) onFocusLost() },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingTextFieldRowPreview() {
    SubwayWhenTheme {
        SettingTextFieldRow(
            title = "혼잡도 이모지",
            value = "☹️",
            onValueChange = {},
            onFocusLost = {},
        )
    }
}
