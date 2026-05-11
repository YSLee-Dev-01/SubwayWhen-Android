package com.yslee.subwaywhen.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.yslee.subwaywhen.ui.theme.Dimens
import com.yslee.subwaywhen.ui.theme.MainColorDark
import com.yslee.subwaywhen.ui.theme.MainColorLight

/**
 * iOS MainStyleViewInSUI 대응.
 * MainColor 배경 + cornerRadius(15.dp) RoundedCornerShape 컨테이너 컴포저블.
 */
@Composable
fun MainBgCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val bgColor = if (isSystemInDarkTheme()) MainColorDark else MainColorLight
    Box(
        modifier = modifier
            .background(
                color = bgColor,
                shape = RoundedCornerShape(Dimens.cornerRadius),
            )
    ) {
        content()
    }
}
