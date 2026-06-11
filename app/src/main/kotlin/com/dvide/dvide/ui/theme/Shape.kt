package com.dvide.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val DvideShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small      = RoundedCornerShape(12.dp),
    medium     = RoundedCornerShape(16.dp),
    large      = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

// ─────────────────────────────────────────────────────────────
// M3 Expressive asymmetric shapes — organic silhouette cards.
// The bottom-start corner is pulled in to create directional tension.
// ─────────────────────────────────────────────────────────────

// Gauge card / Editorial hero — large asymmetric
val ShapeGaugeCard = RoundedCornerShape(
    topStart     = 40.dp,
    topEnd       = 40.dp,
    bottomEnd    = 40.dp,
    bottomStart  = 16.dp,
)

// Tight/sharp variant (when budget is stressed)
val ShapeGaugeCardSharp = RoundedCornerShape(16.dp)

// Cards hero — filled primary container
val ShapeCardsHero = RoundedCornerShape(
    topStart     = 36.dp,
    topEnd       = 36.dp,
    bottomEnd    = 36.dp,
    bottomStart  = 14.dp,
)
val ShapeCardsHeroSharp = RoundedCornerShape(14.dp)

// Bucket / step cards row
val ShapeBucketCard   = RoundedCornerShape(22.dp)

// Timeline transaction row
val ShapeTimelineRow  = RoundedCornerShape(20.dp)

// Settings group container
val ShapeSettingsGroup = RoundedCornerShape(26.dp)

// Bottom sheet / FAB expanded
val ShapeSheet = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)

// Segmented control pill
val ShapePill = RoundedCornerShape(999.dp)

// Keypad key
val ShapeKeypadKey = RoundedCornerShape(18.dp)

// Commit / primary action button
val ShapeCommitBtn = RoundedCornerShape(20.dp)
