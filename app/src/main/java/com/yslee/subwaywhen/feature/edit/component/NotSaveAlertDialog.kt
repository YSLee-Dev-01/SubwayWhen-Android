package com.yslee.subwaywhen.feature.edit.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

@Composable
fun NotSaveAlertDialog(
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(text = "수정된 지하철역이 저장되지 않았어요.\n저장하지 않을 경우 변경된 내용은\n적용되지 않아요.")
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text(text = "저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDiscard) {
                Text(text = "저장하지 않음")
            }
        },
        text = {
            TextButton(onClick = onCancel) {
                Text(text = "취소")
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun NotSaveAlertDialogPreview() {
    SubwayWhenTheme {
        NotSaveAlertDialog(
            onSave = {},
            onDiscard = {},
            onCancel = {},
        )
    }
}
