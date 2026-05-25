package com.yslee.subwaywhen.feature.search.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.R
import com.yslee.subwaywhen.ui.common.MainBgCard
import com.yslee.subwaywhen.ui.theme.Dimens

/**
 * 검색 텍스트 필드 컴포저블.
 * - 비검색 모드: MainBgCard + placeholder Text, 탭 시 onEnterSearchMode() 호출
 * - 검색 모드: MainBgCard(weight(1f)) + BasicTextField + 우측 "닫기" Text
 */
@Composable
fun SearchTextField(
    isSearchMode: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    onEnterSearchMode: () -> Unit,
    onExitSearchMode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isSearchMode) {
        if (isSearchMode) focusRequester.requestFocus()
    }

    if (isSearchMode) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.fillMaxWidth(),
        ) {
            MainBgCard(modifier = Modifier.weight(1f)) {
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = Dimens.fontSizeMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.paddingInner)
                        .focusRequester(focusRequester),
                    decorationBox = { innerTextField ->
                        Box {
                            if (query.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.search_textfield_active_placeholder),
                                    fontSize = Dimens.fontSizeMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                )
                            }
                            innerTextField()
                        }
                    },
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.search_close),
                fontSize = Dimens.fontSizeMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .clip(RoundedCornerShape(Dimens.cornerRadius))
                    .clickable { onExitSearchMode() }
                    .padding(horizontal = 4.dp, vertical = 8.dp),
            )
        }
    } else {
        MainBgCard(
            modifier = modifier
                .fillMaxWidth()
                .clickable { onEnterSearchMode() }
                .clip(RoundedCornerShape(Dimens.cornerRadius)),
        ) {
            Text(
                text = stringResource(R.string.search_textfield_placeholder),
                fontSize = Dimens.fontSizeMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.padding(Dimens.paddingInner),
            )
        }
    }
}
