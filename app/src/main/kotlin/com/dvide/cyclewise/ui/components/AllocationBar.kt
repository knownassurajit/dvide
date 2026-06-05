package com.dvide.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dvide.app.data.model.Category
import com.dvide.app.domain.model.Metrics
import com.dvide.app.ui.theme.cycleColors

private data class AllocSeg(val fraction: Float, val color: Color)

@Composable
fun AllocationBar(
    metrics: Metrics,
    height: Dp = 16.dp,
    modifier: Modifier = Modifier,
) {
    val cc    = MaterialTheme.cycleColors
    val inc   = metrics.income
    val used  = metrics.allocated + metrics.spent
    val over  = used > inc

    val asides   = metrics.byCategory.keys.filter {
        Category.kindOf(it, metrics.transactions.firstOrNull { tx -> tx.category == it }?.kind) == Category.Kind.ASIDE
    }
    val expenses = metrics.byCategory.keys.filter {
        Category.kindOf(it, metrics.transactions.firstOrNull { tx -> tx.category == it }?.kind) == Category.Kind.EXPENSE
    }

    val segs = (asides + expenses).mapNotNull { cat ->
        val amt  = metrics.byCategory[cat] ?: return@mapNotNull null
        if (amt <= 0) return@mapNotNull null
        val frac = if (over) (amt / used).toFloat() else (amt / inc).toFloat()
        AllocSeg(frac.coerceIn(0f, 1f), cc.categoryColor(cat))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        segs.forEach { seg ->
            val animFrac by animateFloatAsState(
                targetValue  = seg.fraction,
                animationSpec = spring(stiffness = 200f, dampingRatio = 0.8f),
                label        = "allocFrac",
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(animFrac.coerceAtLeast(0.001f))
                    .background(seg.color)
            )
        }
        // Remaining unallocated space
        if (!over) {
            val remaining = (1f - segs.sumOf { it.fraction.toDouble() }.toFloat()).coerceAtLeast(0f)
            if (remaining > 0.001f) {
                Box(modifier = Modifier.weight(remaining).fillMaxHeight())
            }
        }
    }
}
