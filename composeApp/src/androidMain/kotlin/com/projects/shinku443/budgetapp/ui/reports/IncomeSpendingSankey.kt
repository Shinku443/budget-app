package com.projects.shinku443.budgetapp.ui.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.projects.shinku443.budgetapp.model.CategoryType
import com.projects.shinku443.budgetapp.viewmodel.CategoryViewModel
import com.projects.shinku443.budgetapp.viewmodel.TransactionViewModel
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
                        // Node hit test
                        nodes.forEach { node ->
                            val pos = nodePositions[node.id] ?: return@forEach
                            val rect = Rect(
                                offset = Offset(pos.x - 36f, pos.y - 18f),
                                size = Size(72f, 36f)
                            )
                            if (rect.contains(offset)) {
                                tooltipText = "${node.label}\nValue: ${"%.2f".format(node.value)}"
                                tooltipPosition = offset
                                return@detectTapGestures
                            }
                        }
                        // Link hit test (approximate by midpoint distance)
                        links.forEach { link ->
                            val from = nodePositions[link.from] ?: return@forEach
                            val to = nodePositions[link.to] ?: return@forEach
                            val mid = Offset((from.x + to.x) / 2, (from.y + to.y) / 2)
                            if ((offset - mid).getDistance() < 24f) {
                                tooltipText = "Flow: ${"%.2f".format(link.value)}"
                                tooltipPosition = offset
                                return@detectTapGestures
                            }
                        }
                        tooltipText = null
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            // Group by level and sort
            val grouped = nodes.groupBy { it.level }
            val levels = grouped.keys.sorted()
            val levelSpacing = width / (levels.size + 1)

            nodePositions.clear()

            // Draw nodes with dynamic vertical spacing
            grouped.forEach { (level, levelNodes) ->
                val ySpacing = height / (levelNodes.size + 1)
                levelNodes.forEachIndexed { i, node ->
                    val pos = Offset(levelSpacing * (level + 1), ySpacing * (i + 1))
                    nodePositions[node.id] = pos

                    // Node rectangle
                    drawRoundRect(
                        color = when (node.level) {
                            0 -> Color(0xFF4CAF50).copy(alpha = 0.30f) // income
                            1 -> Color(0xFF9E9E9E).copy(alpha = 0.30f) // expenses aggregate
                            else -> Color(0xFF2196F3).copy(alpha = 0.30f) // categories
                        },
                        topLeft = Offset(pos.x - 36f, pos.y - 18f),
                        size = Size(72f, 36f),
                        cornerRadius = CornerRadius(8f, 8f)
                    )

                    // Label
                    drawContext.canvas.nativeCanvas.drawText(
                        node.label,
                        pos.x,
                        pos.y - 26f,
                        android.graphics.Paint().apply {
                            textAlign = android.graphics.Paint.Align.CENTER
                            textSize = 26f
                            color = android.graphics.Color.DKGRAY
                            isAntiAlias = true
                        }
                    )
                }
            }

            // Draw links as curves; cap stroke width
            links.forEach { link ->
                val fromPos = nodePositions[link.from] ?: return@forEach
                val toPos = nodePositions[link.to] ?: return@forEach

                val strokeWidth = min(link.value / 10f, 12f) // scale and cap
                val color = if (fromPos.x < toPos.x) Color(0xFF8BC34A) else Color(0xFFF44336)
                val alpha = 0.55f

                drawPath(
                    path = Path().apply {
                        moveTo(fromPos.x + 36f, fromPos.y) // start at right edge of source node
                        cubicTo(
                            (fromPos.x + toPos.x) / 2, fromPos.y,
                            (fromPos.x + toPos.x) / 2, toPos.y,
                            toPos.x - 36f, toPos.y // end at left edge of target node
                        )
                    },
                    color = color.copy(alpha = alpha),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        // Tooltip bubble
        tooltipText?.let {
            Box(
                modifier = Modifier
                    .offset { IntOffset(tooltipPosition.x.toInt(), tooltipPosition.y.toInt()) }
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun IncomeToExpensesToCategoriesSankey(
    transactionViewModel: TransactionViewModel = koinViewModel(),
    categoryViewModel: CategoryViewModel = koinViewModel()
) {
    val transactions by transactionViewModel.transactionsFiltered.collectAsState()
    val categories by categoryViewModel.categories.collectAsState()

    // Lookup for category names by id
    val categoryNames = remember(categories) {
        categories.associate { it.id to it.name }
    }

    // Income nodes (level 0), grouped by categoryId
    val incomeGroups = transactions
        .filter { it.type == CategoryType.INCOME }
        .groupBy { it.categoryId }

    val incomeNodes = incomeGroups.map { (catId, txs) ->
        val total = txs.sumOf { it.amount }.toFloat()
        SankeyNode(
            id = "income-$catId",
            label = categoryNames[catId]?.let { "Income: $it" } ?: "Income: $catId",
            value = total,
            level = 0
        )
    }

    // Aggregate expenses node (level 1)
    val totalExpenses = transactions
        .filter { it.type == CategoryType.EXPENSE }
        .sumOf { it.amount }
        .toFloat()

    val expensesNode = SankeyNode(
        id = "expenses-total",
        label = "Expenses",
        value = totalExpenses,
        level = 1
    )

    // Expense category nodes (level 2), grouped by categoryId
    val expenseGroups = transactions
        .filter { it.type == CategoryType.EXPENSE }
        .groupBy { it.categoryId }

    val expenseCategoryNodes = expenseGroups.map { (catId, txs) ->
        val total = txs.sumOf { it.amount }.toFloat()
        SankeyNode(
            id = "expense-cat-$catId",
            label = categoryNames[catId] ?: "Category: $catId",
            value = total,
            level = 2
        )
    }

    // Links: Income → Expenses (each income source contributes its full value to Expenses)
    val incomeToExpensesLinks = incomeNodes.map { inc ->
        SankeyLink(
            from = inc.id,
            to = expensesNode.id,
            value = inc.value
        )
    }

    // Links: Expenses → Expense Categories (fan out by category totals)
    val expensesToCategoryLinks = expenseCategoryNodes.map { cat ->
        SankeyLink(
            from = expensesNode.id,
            to = cat.id,
            value = cat.value
        )
    }

    SankeyChart(
        nodes = incomeNodes + expensesNode + expenseCategoryNodes,
        links = incomeToExpensesLinks + expensesToCategoryLinks,
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .padding(8.dp)
    )
}


@Composable
fun IncomeSpendingSankey(
    transactionViewModel: TransactionViewModel = koinViewModel(),
    categoryViewModel: CategoryViewModel = koinViewModel()
) {
    val transactions by transactionViewModel.transactionsFiltered.collectAsState()
    val categories by categoryViewModel.categories.collectAsState()

    // Income nodes (level 0)
    val incomeNodes = transactions.filter { it.type == CategoryType.INCOME }
        .groupBy { it.categoryId }
        .map { (catId, txs) ->
            SankeyNode(catId, "Income", txs.sumOf { it.amount }.toFloat(), 0)
        }

    // Category nodes (level 1)
    val categoryNodes = categories.map {
        SankeyNode(it.id, it.name, 0f, 1) // value optional, links carry flow
    }

    // Expense nodes (level 2)
    val expenseNodes = transactions.filter { it.type == CategoryType.EXPENSE }
        .groupBy { it.categoryId }
        .map { (catId, txs) ->
            SankeyNode(catId, "Expense", txs.sumOf { it.amount }.toFloat(), 2)
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
