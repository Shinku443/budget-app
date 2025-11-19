package com.projects.shinku443.budgetapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.koalaplot.core.pie.PieChart
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun CategoryPieChart(
    data: Map<String, Float>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Text("No data yet")
        return
    }


    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Category Breakdown", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        PieChart(
            modifier = Modifier
                .height(300.dp)
                .fillMaxWidth(),
            values = data.values.toList(),
            label = { index ->
                Text(data.keys.toList()[index])
            }
        )
    }
}
