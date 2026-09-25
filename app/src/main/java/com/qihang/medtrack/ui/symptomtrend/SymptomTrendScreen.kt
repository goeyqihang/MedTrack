package com.qihang.medtrack.ui.symptomtrend

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SymptomTrendScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    viewModel: SymptomTrendViewModel = viewModel(
        factory = SymptomTrendViewModel.Factory
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(text = "Symptom Trends", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))

        val error = state.error
        when {
            state.isLoading -> {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            error != null -> {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
            }

            else -> TrendContent(
                state = state,
                onCategorySelected = { viewModel.onCategorySelected(it) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Back")
        }
    }
}

@Composable
private fun TrendContent(
    state: SymptomTrendUiState,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // ── category filter chips ────────────────────────────────────
        if (state.categories.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.selectedCategory == null,
                    onClick = { onCategorySelected(null) },
                    label = { Text("All") }
                )
                state.categories.forEach { category ->
                    FilterChip(
                        selected = state.selectedCategory == category,
                        onClick = { onCategorySelected(category) },
                        label = { Text(category) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── summary ──────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryCard(
                value = state.entryCount.toString(),
                label = "Entries",
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                value = "%.1f".format(state.averageSeverity),
                label = "Avg Severity",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── chart ────────────────────────────────────────────────────
        when {
            state.points.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No symptom data to chart.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            else -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    SeverityLineChart(
                        points = state.points,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Severity (0–10) over time",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Hand-drawn line chart: severity (Y, 0–10) against chronological entries (X).
 */
@Composable
private fun SeverityLineChart(
    points: List<TrendPoint>,
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val axisColor = MaterialTheme.colorScheme.outline
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val labelStyle = MaterialTheme.typography.labelSmall
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier) {
        val leftPad = 32.dp.toPx()
        val bottomPad = 28.dp.toPx()
        val topPad = 8.dp.toPx()
        val rightPad = 12.dp.toPx()

        val chartW = size.width - leftPad - rightPad
        val chartH = size.height - topPad - bottomPad
        val maxSeverity = 10f

        fun yFor(severity: Float): Float =
            topPad + chartH - (severity / maxSeverity) * chartH

        fun xFor(index: Int): Float =
            if (points.size == 1) {
                leftPad + chartW / 2f
            } else {
                leftPad + (index.toFloat() / (points.size - 1)) * chartW
            }

        // horizontal gridlines + Y labels at 0, 5, 10
        listOf(0, 5, 10).forEach { severity ->
            val y = yFor(severity.toFloat())
            drawLine(
                color = gridColor,
                start = Offset(leftPad, y),
                end = Offset(leftPad + chartW, y),
                strokeWidth = 1.dp.toPx()
            )
            val layout = textMeasurer.measure(severity.toString(), labelStyle)
            drawText(
                textLayoutResult = layout,
                color = labelColor,
                topLeft = Offset(
                    leftPad - layout.size.width - 6.dp.toPx(),
                    y - layout.size.height / 2f
                )
            )
        }

        // Y and X axis lines
        drawLine(
            axisColor,
            Offset(leftPad, topPad),
            Offset(leftPad, topPad + chartH),
            2.dp.toPx()
        )
        drawLine(
            axisColor,
            Offset(leftPad, topPad + chartH),
            Offset(leftPad + chartW, topPad + chartH),
            2.dp.toPx()
        )

        // connecting line
        if (points.size >= 2) {
            val path = Path()
            points.forEachIndexed { index, point ->
                val x = xFor(index)
                val y = yFor(point.severity.toFloat())
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, lineColor, style = Stroke(width = 3.dp.toPx()))
        }

        // data point dots
        points.forEachIndexed { index, point ->
            drawCircle(
                color = lineColor,
                radius = 4.dp.toPx(),
                center = Offset(xFor(index), yFor(point.severity.toFloat()))
            )
        }

        // X labels — first, middle, last (avoids clutter)
        val labelIndices = when {
            points.size <= 1 -> listOf(0)
            points.size == 2 -> listOf(0, 1)
            else -> listOf(0, points.size / 2, points.size - 1)
        }
        labelIndices.forEach { index ->
            val layout = textMeasurer.measure(points[index].label, labelStyle)
            val x = (xFor(index) - layout.size.width / 2f)
                .coerceIn(0f, size.width - layout.size.width)
            drawText(
                textLayoutResult = layout,
                color = labelColor,
                topLeft = Offset(x, topPad + chartH + 6.dp.toPx())
            )
        }
    }
}
