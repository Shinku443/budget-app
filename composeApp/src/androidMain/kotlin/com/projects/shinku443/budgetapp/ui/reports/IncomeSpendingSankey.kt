package com.projects.shinku443.budgetapp.ui.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.projects.shinku443.budgetapp.model.CategoryType
import com.projects.shinku443.budgetapp.viewmodel.BudgetViewModel
import org.koin.androidx.compose.koinViewModel
import kotlin.math.min


data class SankeyNode(
    val id: String,
    val label: String,
    val value: Float,
    val level: Int // 0 = income, 1 = category, 2 = spending
)

data class SankeyLink(
    val from: String,
    val to: String,
    val value: Float
)

@Composable
fun SankeyChart(
    nodes: List<SankeyNode>,
    links: List<SankeyLink>,
    modifier: Modifier = Modifier
) {
    var tooltipText by remember { mutableStateOf<String?>(null) }
    var tooltipPosition by remember { mutableStateOf(Offset.Zero) }

    val nodePositions = remember { mutableStateMapOf<String, Offset>() }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(nodes, links) {
                    detectTapGestures { offset ->
                        // Check if tapped a node
                        nodes.forEach { node ->
                            val pos = nodePositions[node.id] ?: return@forEach
                            val rect = Rect(
                                offset = Offset(pos.x - 40f, pos.y - 20f),
                                size = Size(80f, 40f)
                            )
                            if (rect.contains(offset)) {
                                tooltipText = "${node.label}: ${node.value}"
                                tooltipPosition = offset
                            }
                        }
                        // Check if tapped a link (approximate)
                        links.forEach { link ->
                            val from = nodePositions[link.from] ?: return@forEach
                            val to = nodePositions[link.to] ?: return@forEach
                            val mid = Offset((from.x + to.x) / 2, (from.y + to.y) / 2)
                            if ((offset - mid).getDistance() < 30f) {
                                tooltipText = "Flow: ${link.value}"
                                tooltipPosition = offset
                            }
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            val grouped = nodes.groupBy { it.level }
            val levels = grouped.keys.sorted()
            val levelSpacing = width / (levels.size + 1)

            nodePositions.clear()
            grouped.forEach { (level, levelNodes) ->
                val ySpacing = height / (levelNodes.size + 1)
                levelNodes.forEachIndexed { i, node ->
                    val pos = Offset(levelSpacing * (level + 1), ySpacing * (i + 1))
                    nodePositions[node.id] = pos

                    drawRect(
                        color = Color.Blue.copy(alpha = 0.3f),
                        topLeft = Offset(pos.x - 40f, pos.y - 20f),
                        size = Size(80f, 40f)
                    )

                    drawContext.canvas.nativeCanvas.drawText(
                        node.label,
                        pos.x,
                        pos.y - 30f,
                        android.graphics.Paint().apply {
                            textAlign = android.graphics.Paint.Align.CENTER
                            textSize = 28f
                            color = android.graphics.Color.BLACK
                        }
                    )
                }
            }

            links.forEach { link ->
                val fromPos = nodePositions[link.from] ?: return@forEach
                val toPos = nodePositions[link.to] ?: return@forEach

                drawPath(
                    path = Path().apply {
                        moveTo(fromPos.x, fromPos.y)
                        cubicTo(
                            (fromPos.x + toPos.x) / 2, fromPos.y,
                            (fromPos.x + toPos.x) / 2, toPos.y,
                            toPos.x, toPos.y
                        )
                    },
                    color = Color.Green.copy(alpha = 0.5f),
                    style = Stroke(width = min(link.value, 12f))
                )
            }
        }

        // Tooltip
        tooltipText?.let {
            Box(
                modifier = Modifier
                    .offset { IntOffset(tooltipPosition.x.toInt(), tooltipPosition.y.toInt()) }
                    .background(Color.Black.copy(alpha = 0.8f), shape = RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                Text(it, color = Color.White)
            }
        }
    }
}

@Composable
fun IncomeSpendingSankey(viewModel: BudgetViewModel = koinViewModel()) {
    val transactions by viewModel.transactions.collectAsState()
    val categories by viewModel.categories.collectAsState()

    // Income nodes (level 0)
    val incomeNodes = transactions.filter { it.type == CategoryType.INCOME }
        .groupBy { it.categoryId }
        .map { (catId, txs) ->
            SankeyNode(catId, "Income ${catId}", txs.sumOf { it.amount }.toFloat(), 0)
        }

    // Category nodes (level 1)
    val categoryNodes = categories.map {
        SankeyNode(it.id, it.name, 0f, 1) // value optional, links carry flow
    }

    // Expense nodes (level 2)
    val expenseNodes = transactions.filter { it.type == CategoryType.EXPENSE }
        .groupBy { it.categoryId }
        .map { (catId, txs) ->
            SankeyNode(catId, "Expense ${catId}", txs.sumOf { it.amount }.toFloat(), 2)
        }

    // Links: Income → Category
    val incomeLinks = incomeNodes.map { inc ->
        // Simplify: connect each income to all categories for now
        categoryNodes.map { cat ->
            SankeyLink(inc.id, cat.id, inc.value / categoryNodes.size)
        }
    }.flatten()

    // Links: Category → Expense
    val expenseLinks = categoryNodes.map { cat ->
        expenseNodes.map { exp ->
            SankeyLink(cat.id, exp.id, exp.value / categoryNodes.size)
        }
    }.flatten()

    SankeyChart(
        nodes = incomeNodes + categoryNodes + expenseNodes,
        links = incomeLinks + expenseLinks,
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp) // ✅ smaller height
            .padding(8.dp)
    )
}
