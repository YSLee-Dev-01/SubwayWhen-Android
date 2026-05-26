package com.yslee.subwaywhen.feature.search.modal.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.yslee.subwaywhen.R
import com.yslee.subwaywhen.ui.common.modal.CommonModalBottomSheet
import com.yslee.subwaywhen.ui.common.modal.ModalSubButton
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveCompletedModal(onConfirm: () -> Unit) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.tutorial_success))
    val progress by animateLottieCompositionAsState(composition, iterations = 1)

    CommonModalBottomSheet(
        mainTitle = "저장 완료",
        subTitle = "지하철 역이 저장되었어요.",
        onDismiss = onConfirm,
        confirmButton = {
            ModalSubButton(
                text = "확인",
                bgColor = MaterialTheme.colorScheme.primary,
                textColor = Color.White,
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.CenterHorizontally),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "SaveCompletedModal - Light", showBackground = true)
@Composable
private fun SaveCompletedModalLightPreview() {
    SubwayWhenTheme(darkTheme = false) {
        SaveCompletedModal(onConfirm = {})
    }
}
