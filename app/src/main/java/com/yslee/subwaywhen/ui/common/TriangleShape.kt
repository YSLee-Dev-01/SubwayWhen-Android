package com.yslee.subwaywhen.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.yslee.subwaywhen.ui.theme.SubwayWhenTheme

/**
 * iOS Triangle 대응.
 * 위쪽 꼭짓점 → 좌하 → 우하 삼각형 Shape.
 */
object TriangleShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val path = Path().apply {
            moveTo(size.width / 2f, 0f)
            lineTo(0f, size.height)
            lineTo(size.width, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}

@Preview(name = "TriangleShape - Light", showBackground = true)
@Composable
private fun TriangleShapeLightPreview() {
    SubwayWhenTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .size(width = 60.dp, height = 40.dp)
                .background(color = MaterialTheme.colorScheme.onBackground, shape = TriangleShape)
        )
    }
}

@Preview(name = "TriangleShape - Dark", showBackground = true, backgroundColor = 0xFF222222)
@Composable
private fun TriangleShapeDarkPreview() {
    SubwayWhenTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .size(width = 60.dp, height = 40.dp)
                .background(color = MaterialTheme.colorScheme.onBackground, shape = TriangleShape)
        )
    }
}
