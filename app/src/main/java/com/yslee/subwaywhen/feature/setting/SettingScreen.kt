package com.yslee.subwaywhen.feature.setting

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
fun SettingScreen(
    viewModel: SettingViewModel = hiltViewModel(),
) {
    SettingScreenContent()
}

@Composable
private fun SettingScreenContent() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(text = stringResource(R.string.setting_placeholder_title))
    }
}

@Preview(name = "SettingScreen - Light", showBackground = true)
@Composable
private fun SettingScreenLightPreview() {
    SettingScreenContent()
}

@Preview(name = "SettingScreen - Dark", showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun SettingScreenDarkPreview() {
    SettingScreenContent()
}
