package com.knownassurajit.dvide_finance.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.knownassurajit.dvide_finance.app.data.profile.ProfileImages

/**
 * Circular profile picture, or the user's initial when there is no (readable) photo.
 *
 * [imageUri] is the stored photo reference (a private file name; see [ProfileImages]). Decoding happens off the
 * main thread at the displayed size, so a large photo can never block or exhaust memory during composition.
 */
@Composable
fun ProfileAvatar(
    userName: String,
    imageUri: String?,
    size: Dp = 42.dp,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    onClickLabel: String? = null,
) {
    val context = LocalContext.current
    val targetPx = with(LocalDensity.current) { (size * 2).roundToPx() }

    val bitmap by produceState<ImageBitmap?>(
        initialValue = imageUri?.takeIf { it.isNotBlank() }?.let { ProfileImages.peek(it, targetPx)?.asImageBitmap() },
        imageUri,
        targetPx,
    ) {
        value = if (imageUri.isNullOrBlank()) null
                else ProfileImages.decodeAvatar(context, imageUri, targetPx)?.asImageBitmap()
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .then(
                if (onClick != null) Modifier.clickable(onClickLabel = onClickLabel, role = Role.Button, onClick = onClick)
                else Modifier
            ),
        contentAlignment = Alignment.Center,
    ) {
        val photo = bitmap
        if (photo != null) {
            Image(
                bitmap = photo,
                contentDescription = "Profile photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            val trimmed = userName.trim()
            // First code point, so an emoji or a character outside the BMP is never cut in half.
            val initial = if (trimmed.isEmpty()) "Y"
                          else String(Character.toChars(trimmed.codePointAt(0))).uppercase()
            Text(
                text = initial,
                style = if (size > 60.dp) MaterialTheme.typography.headlineMedium
                        else MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}
