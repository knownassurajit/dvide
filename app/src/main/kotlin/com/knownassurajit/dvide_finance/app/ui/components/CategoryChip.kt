package com.knownassurajit.dvide_finance.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.knownassurajit.dvide_finance.app.data.model.Category
import com.knownassurajit.dvide_finance.app.ui.theme.dvideColors

@Composable
fun CategoryChip(
    categoryKey: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cc      = MaterialTheme.dvideColors
    val color   = cc.categoryColor(categoryKey)
    val soft    = cc.categorySoft(categoryKey)
    val label   = Category.labelOf(categoryKey)

    val elevation by animateDpAsState(
        targetValue   = if (selected) 4.dp else 0.dp,
        animationSpec = spring(stiffness = 400f),
        label         = "chipElevation",
    )

    OutlinedButton(
        onClick  = onClick,
        modifier = modifier.height(48.dp),
        shape    = RoundedCornerShape(16.dp),
        border   = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) color else MaterialTheme.colorScheme.outlineVariant,
        ),
        colors   = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) soft else Color.Transparent,
            contentColor   = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = elevation),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (selected)
                    androidx.compose.ui.text.font.FontWeight(720)
                else
                    androidx.compose.ui.text.font.FontWeight(600),
            ),
        )
    }
}
