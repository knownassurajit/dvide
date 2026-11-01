package com.knownassurajit.dvide_finance.app.ui.settings

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.knownassurajit.dvide_finance.app.util.CropMath
import com.knownassurajit.dvide_finance.app.util.CropRegion
import kotlin.math.roundToInt

/**
 * "Move and zoom" cropper for the profile photo. The image is panned/pinched inside a square viewport shown
 * through a circular mask (the avatar is circular). A zoom slider offers the same control without gestures, for
 * screen readers and switch access. Geometry lives in [CropMath] (unit tested).
 */
@Composable
fun ProfileCropDialog(
    source: Bitmap,
    onConfirm: (CropRegion) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Move and zoom", style = MaterialTheme.typography.titleLarge)

                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val side = minOf(maxWidth, 320.dp)
                    val viewportPx = with(LocalDensity.current) { side.toPx() }
                    var transform by remember(source, viewportPx) {
                        mutableStateOf(CropMath.initial(source.width, source.height, viewportPx))
                    }
                    val minScale = remember(source, viewportPx) {
                        CropMath.minScale(source.width, source.height, viewportPx)
                    }
                    val image = remember(source) { source.asImageBitmap() }
                    val scrim = Color.Black.copy(alpha = 0.55f)

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Canvas(
                            modifier = Modifier
                                .size(side)
                                .testTag("crop_canvas")
                                .semantics {
                                    contentDescription = "Photo preview. Drag to move, pinch or use the zoom slider to zoom."
                                }
                                .pointerInput(source, viewportPx) {
                                    detectTransformGestures { centroid, pan, zoom, _ ->
                                        transform = CropMath.transform(
                                            current = transform,
                                            imageWidth = source.width,
                                            imageHeight = source.height,
                                            viewport = viewportPx,
                                            zoom = zoom,
                                            panX = pan.x,
                                            panY = pan.y,
                                            focalX = centroid.x,
                                            focalY = centroid.y,
                                        )
                                    }
                                },
                        ) {
                            drawImage(
                                image = image,
                                dstOffset = IntOffset(transform.offsetX.roundToInt(), transform.offsetY.roundToInt()),
                                dstSize = IntSize(
                                    (source.width * transform.scale).roundToInt(),
                                    (source.height * transform.scale).roundToInt(),
                                ),
                            )
                            // Dim everything outside the circle that will become the avatar.
                            val outside = Path().apply {
                                fillType = PathFillType.EvenOdd
                                addRect(Rect(Offset.Zero, size))
                                addOval(Rect(Offset.Zero, size))
                            }
                            drawPath(outside, scrim)
                            drawCircle(
                                color = Color.White.copy(alpha = 0.9f),
                                radius = size.minDimension / 2f,
                                style = Stroke(width = 2.dp.toPx()),
                            )
                        }

                        // Mirrors the pinch zoom (1x..MAX_ZOOMx), zooming about the viewport centre.
                        val zoomLevel = (transform.scale / minScale).coerceIn(1f, CropMath.MAX_ZOOM)
                        Slider(
                            value = zoomLevel,
                            onValueChange = { target ->
                                transform = CropMath.transform(
                                    current = transform,
                                    imageWidth = source.width,
                                    imageHeight = source.height,
                                    viewport = viewportPx,
                                    zoom = (target * minScale) / transform.scale,
                                    panX = 0f,
                                    panY = 0f,
                                    focalX = viewportPx / 2f,
                                    focalY = viewportPx / 2f,
                                )
                            },
                            valueRange = 1f..CropMath.MAX_ZOOM,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("crop_zoom")
                                .semantics {
                                    contentDescription = "Zoom"
                                    stateDescription = "${(zoomLevel * 100).roundToInt()} percent"
                                },
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        ) {
                            TextButton(onClick = onDismiss) { Text("Cancel") }
                            Button(
                                onClick = {
                                    onConfirm(CropMath.regionFor(source.width, source.height, viewportPx, transform))
                                },
                                modifier = Modifier.testTag("crop_confirm"),
                            ) { Text("Use photo") }
                        }
                    }
                }
            }
        }
    }
}
