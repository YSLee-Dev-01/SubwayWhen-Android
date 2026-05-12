package com.yslee.subwaywhen.feature.tutorial

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
        MainBgCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(horizontal = Dimens.paddingLR),
        ) {
            Text(
                text = stringResource(page.titleRes),
                fontSize = Dimens.fontSizeLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp),
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        MainBgCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = Dimens.paddingLR),
        ) {
            Image(
                painter = painterResource(page.imageRes),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            )
        }

        PrimaryButton(
            text = stringResource(buttonLabelRes),
            containerColor = AppIconColor,
            onClick = onNextClick,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = Dimens.paddingLR, end = Dimens.paddingLR, bottom = 16.dp),
        )
    }
}
