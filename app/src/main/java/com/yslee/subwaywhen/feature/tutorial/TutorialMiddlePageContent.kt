package com.yslee.subwaywhen.feature.tutorial

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.ui.common.MainBgCard
import com.yslee.subwaywhen.ui.common.PrimaryButton
import com.yslee.subwaywhen.ui.theme.AppIconColor
import com.yslee.subwaywhen.ui.theme.Dimens

@Composable
fun TutorialMiddlePageContent(
    page: TutorialPageType.Middle,
    buttonLabelRes: Int,
    onNextClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(page.titleRes),
            fontSize = Dimens.fontSizeLarge,
            modifier = Modifier.padding(horizontal = Dimens.paddingLR, vertical = 16.dp),
        )

        MainBgCard(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimens.paddingLR),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(page.imageRes),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
                )

                PrimaryButton(
                    text = stringResource(buttonLabelRes),
                    containerColor = AppIconColor,
                    onClick = onNextClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.paddingLR, vertical = 16.dp),
                )
            }
        }
    }
}
