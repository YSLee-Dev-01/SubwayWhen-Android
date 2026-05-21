package com.yslee.subwaywhen.feature.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.yslee.subwaywhen.R

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
) {
    SearchScreenContent()
}

@Composable
private fun SearchScreenContent() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(text = stringResource(R.string.search_placeholder_title))
    }
}

@Preview(name = "SearchScreen - Light", showBackground = true)
@Composable
private fun SearchScreenLightPreview() {
    SearchScreenContent()
}

@Preview(name = "SearchScreen - Dark", showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun SearchScreenDarkPreview() {
    SearchScreenContent()
}
