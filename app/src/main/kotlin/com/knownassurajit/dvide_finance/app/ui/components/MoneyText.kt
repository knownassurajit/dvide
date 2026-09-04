package com.knownassurajit.dvide_finance.app.ui.components

import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun MoneyText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    minSize: TextUnit = 14.sp,
    textAlign: TextAlign? = null,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        style = style,
        maxLines = 1,
        overflow = TextOverflow.Clip,
        softWrap = false,
        textAlign = textAlign,
        autoSize = TextAutoSize.StepBased(
            minFontSize = minSize,
            maxFontSize = style.fontSize,
            stepSize = 1.sp,
        ),
    )
}

@Composable
fun MoneyText(
    text: AnnotatedString,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    minSize: TextUnit = 22.sp,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        style = style,
        maxLines = 1,
        overflow = TextOverflow.Clip,
        softWrap = false,
        autoSize = TextAutoSize.StepBased(
            minFontSize = minSize,
            maxFontSize = style.fontSize,
            stepSize = 1.sp,
        ),
    )
}
