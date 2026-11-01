package com.knownassurajit.dvide_finance.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.knownassurajit.dvide_finance.app.ui.theme.MIN_VISIBLE_CONTRAST
import com.knownassurajit.dvide_finance.app.ui.theme.argbToHex
import com.knownassurajit.dvide_finance.app.ui.theme.argbToHsl
import com.knownassurajit.dvide_finance.app.ui.theme.contrastAgainst
import com.knownassurajit.dvide_finance.app.ui.theme.hslToColor
import com.knownassurajit.dvide_finance.app.ui.theme.parseHexArgb

/**
 * Free color choice for an entry. Hue / saturation / lightness sliders and a hex field stay in sync; the exact
 * value typed in the hex field is what gets saved (it is not round-tripped through HSL). The result is an
 * opaque ARGB color, stored in the existing `Transaction.color` column, so older data is unaffected.
 */
@Composable
fun ColorPickerDialog(
    initial: Color?,
    onConfirm: (Color) -> Unit,
    onDismiss: () -> Unit,
) {
    val start = remember(initial) { initial ?: Color(0xFF4285F4) }
    val startHsl = remember(start) { argbToHsl(start.toArgb()) }

    var picked by remember { mutableStateOf(start) }
    var hue by remember { mutableFloatStateOf(startHsl.hue) }
    var saturation by remember { mutableFloatStateOf(startHsl.saturation) }
    var lightness by remember { mutableFloatStateOf(startHsl.lightness) }
    var hexText by remember { mutableStateOf(argbToHex(start.toArgb())) }

    fun onSlidersChanged() {
        picked = hslToColor(hue, saturation, lightness)
        hexText = argbToHex(picked.toArgb())
    }

    val hexValid = parseHexArgb(hexText) != null
    val surface = MaterialTheme.colorScheme.surfaceContainerHigh
    val lowContrast = picked.contrastAgainst(surface) < MIN_VISIBLE_CONTRAST

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom color") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(picked)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            .semantics { contentDescription = "Preview of ${argbToHex(picked.toArgb())}" },
                    )
                    Column {
                        Text(argbToHex(picked.toArgb()), style = MaterialTheme.typography.titleMedium)
                        if (lowContrast) {
                            Text(
                                text = "Hard to see on this theme. Try a lighter or darker shade.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }

                LabeledSlider("Hue", hue, 0f..359f, picked) { hue = it; onSlidersChanged() }
                LabeledSlider("Saturation", saturation, 0f..1f, picked) { saturation = it; onSlidersChanged() }
                LabeledSlider("Lightness", lightness, 0f..1f, picked) { lightness = it; onSlidersChanged() }

                OutlinedTextField(
                    value = hexText,
                    onValueChange = { input ->
                        hexText = input.take(7)
                        parseHexArgb(hexText)?.let { argb ->
                            picked = Color(argb)
                            val hsl = argbToHsl(argb)
                            hue = hsl.hue; saturation = hsl.saturation; lightness = hsl.lightness
                        }
                    },
                    label = { Text("Hex code") },
                    placeholder = { Text("#RRGGBB") },
                    isError = !hexValid,
                    supportingText = { if (!hexValid) Text("Enter 3 or 6 hex digits, for example #34A853") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("color_hex_field"),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(picked) },
                enabled = hexValid,
                modifier = Modifier.testTag("color_confirm"),
            ) { Text("Use color") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun LabeledSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    accent: Color,
    onChange: (Float) -> Unit,
) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onChange,
            valueRange = range,
            colors = SliderDefaults.colors(thumbColor = accent, activeTrackColor = accent),
            modifier = Modifier.semantics { contentDescription = label },
        )
    }
}
