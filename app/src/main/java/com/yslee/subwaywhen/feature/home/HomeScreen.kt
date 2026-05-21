package com.yslee.subwaywhen.feature.home

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
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
) {
    HomeScreenContent()
}

@Composable
private fun HomeScreenContent() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(text = stringResource(R.string.home_placeholder_title))
    }
}

@Preview(name = "HomeScreen - Light", showBackground = true)
@Composable
private fun HomeScreenLightPreview() {
    HomeScreenContent()
}

@Preview(name = "HomeScreen - Dark", showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun HomeScreenDarkPreview() {
    HomeScreenContent()
}
