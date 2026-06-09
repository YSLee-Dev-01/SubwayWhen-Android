package com.yslee.subwaywhen.feature.setting.modal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.ui.common.PrimaryButton
import com.yslee.subwaywhen.ui.common.modal.CommonModalBottomSheet
import com.yslee.subwaywhen.ui.theme.AppIconColor
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

private val trainIcons = listOf("🚃", "🚂", "🚈", "🚅", "🚋", "🚗", "🚙", "🏎️")

@Composable
fun TrainIconModal(
    currentIcon: String,
    onIconSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var selected by remember(currentIcon) { mutableStateOf(currentIcon) }

    CommonModalBottomSheet(
        mainTitle = "열차 아이콘",
        onDismiss = onDismiss,
        confirmButton = { animatedDismiss ->
            PrimaryButton(
                text = "확인",
                containerColor = AppIconColor,
                onClick = { onIconSelected(selected); animatedDismiss() },
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
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
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = if (isSelected) AppIconColor.copy(alpha = 0.1f)
                                    else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(Dimens.cornerRadius),
                        )
                        .then(
                            if (isSelected) Modifier.border(
                                width = 2.dp,
                                color = AppIconColor,
                                shape = RoundedCornerShape(Dimens.cornerRadius),
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
