package com.yslee.subwaywhen.feature.home.modal.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.point
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerDimensions
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import com.yslee.subwaywhen.feature.home.modal.HourlyCongestion
import com.yslee.subwaywhen.ui.theme.AppIconColor

// vico 2.1.2: CartesianValueFormatter must never return an empty string.
// Use a custom ItemPlacer to control which x values get labels.
private val xAxisLabelHours = listOf(0, 3, 6, 9, 12, 15, 18, 21, 23)
    .map { it.toDouble() }

private object FixedHourItemPlacer : HorizontalAxis.ItemPlacer {
    override fun getLabelValues(
        context: CartesianDrawingContext,
        visibleXRange: ClosedFloatingPointRange<Double>,
        fullXRange: ClosedFloatingPointRange<Double>,
        maxLabelWidth: Float,
    ): List<Double> = xAxisLabelHours.filter {
        it >= visibleXRange.start && it <= visibleXRange.endInclusive
    }

    override fun getWidthMeasurementLabelValues(
        context: CartesianMeasuringContext,
        layerDimensions: CartesianLayerDimensions,
        fullXRange: ClosedFloatingPointRange<Double>,
    ): List<Double> = listOf(xAxisLabelHours.first())

    override fun getHeightMeasurementLabelValues(
        context: CartesianMeasuringContext,
        layerDimensions: CartesianLayerDimensions,
        fullXRange: ClosedFloatingPointRange<Double>,
        maxLabelWidth: Float,
    ): List<Double> = listOf(xAxisLabelHours.first())

    override fun getStartLayerMargin(
        context: CartesianMeasuringContext,
        layerDimensions: CartesianLayerDimensions,
        tickThickness: Float,
        maxLabelWidth: Float,
    ): Float = maxLabelWidth

    override fun getEndLayerMargin(
        context: CartesianMeasuringContext,
        layerDimensions: CartesianLayerDimensions,
        tickThickness: Float,
        maxLabelWidth: Float,
    ): Float = 0f
}

@Composable
fun CongestionChart(
    congestionData: List<HourlyCongestion>,
    nowHour: Int,
    selectedHour: Int?,
    onHourSelect: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val latestNowHour by rememberUpdatedState(nowHour)
    val latestOnHourSelect by rememberUpdatedState(onHourSelect)
    val latestCongestionData by rememberUpdatedState(congestionData)

    LaunchedEffect(congestionData) {
        if (congestionData.isEmpty()) return@LaunchedEffect
        modelProducer.runTransaction {
            lineSeries {
                series(
                    congestionData.map { it.hour },
                    congestionData.map { it.percent },
                )
            }
        }
    }

    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    val redDotComponent = rememberShapeComponent(fill = fill(Color.Red), shape = CorneredShape.Pill)
    val redPoint = LineCartesianLayer.point(component = redDotComponent, size = 8.dp)

    val lineSpec = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(fill(AppIconColor)),
        pointProvider = remember(nowHour, redPoint) {
            object : LineCartesianLayer.PointProvider {
                override fun getPoint(
                    entry: LineCartesianLayerModel.Entry,
                    seriesIndex: Int,
                    extraStore: ExtraStore,
                ): LineCartesianLayer.Point? =
                    if (entry.x.toInt() == nowHour) redPoint else null

                override fun getLargestPoint(extraStore: ExtraStore): LineCartesianLayer.Point = redPoint
            }
        },
    )

    val tapLabelText = rememberTextComponent(color = onSurfaceColor)
    val tapMarker = rememberDefaultCartesianMarker(
        label = tapLabelText,
        valueFormatter = DefaultCartesianMarker.ValueFormatter { _, targets ->
            val xHour = targets.firstOrNull()?.x?.toInt() ?: return@ValueFormatter ""
            val percent = latestCongestionData.firstOrNull { it.hour == xHour }?.percent
                ?: return@ValueFormatter ""
            "${xHour}시 · ${percent}%"
        },
    )

    val nowLabelText = rememberTextComponent(color = Color.Red)
    val nowMarker = rememberDefaultCartesianMarker(
        label = nowLabelText,
        valueFormatter = DefaultCartesianMarker.ValueFormatter { _, targets ->
            val xHour = targets.firstOrNull()?.x?.toInt() ?: return@ValueFormatter ""
            val percent = latestCongestionData.firstOrNull { it.hour == xHour }?.percent
                ?: return@ValueFormatter ""
            "${percent}%"
        },
    )

    val markerListener = remember {
        object : CartesianMarkerVisibilityListener {
            override fun onShown(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                val x = targets.firstOrNull()?.x?.toInt() ?: return
                latestOnHourSelect(if (x == latestNowHour) null else x)
            }

            override fun onUpdated(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                val x = targets.firstOrNull()?.x?.toInt() ?: return
                latestOnHourSelect(if (x == latestNowHour) null else x)
            }

            override fun onHidden(marker: CartesianMarker) {
                latestOnHourSelect(null)
            }
        }
    }

    val xFormatter = remember {
        CartesianValueFormatter { _, value, _ -> "${value.toInt()}시" }
    }

    val yFormatter = remember {
        CartesianValueFormatter { _, value, _ -> "${value.toInt()}%" }
    }

    val chart = rememberCartesianChart(
        rememberLineCartesianLayer(
            lineProvider = LineCartesianLayer.LineProvider.series(lineSpec),
        ),
        startAxis = VerticalAxis.rememberStart(
            valueFormatter = yFormatter,
            itemPlacer = VerticalAxis.ItemPlacer.step({ _ -> 30.0 }),
        ),
        bottomAxis = HorizontalAxis.rememberBottom(
            valueFormatter = xFormatter,
            itemPlacer = FixedHourItemPlacer,
        ),
        marker = tapMarker,
        markerVisibilityListener = markerListener,
        persistentMarkers = if (selectedHour == null) {
            { _ -> nowMarker at nowHour }
        } else null,
    )

    CartesianChartHost(
        chart = chart,
        modelProducer = modelProducer,
        scrollState = rememberVicoScrollState(scrollEnabled = false),
        modifier = modifier,
    )
}
