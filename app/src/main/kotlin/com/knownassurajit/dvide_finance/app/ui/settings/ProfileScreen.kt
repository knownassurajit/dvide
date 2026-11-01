package com.knownassurajit.dvide_finance.app.ui.settings

import android.graphics.Bitmap
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.knownassurajit.dvide_finance.app.data.profile.ProfileImages
import com.knownassurajit.dvide_finance.app.data.repository.AppSettings
import com.knownassurajit.dvide_finance.app.domain.model.EntryLimits
import com.knownassurajit.dvide_finance.app.ui.components.CwIcons
import com.knownassurajit.dvide_finance.app.ui.components.ProfileAvatar
import com.knownassurajit.dvide_finance.app.ui.theme.DvideDimens
import com.knownassurajit.dvide_finance.app.ui.theme.ShapeCommitBtn
import kotlinx.coroutines.launch

/**
 * Local profile. Everything here is stored on this device only: the app has no online account, so nothing on this
 * screen is "verified" and the screen says so. The photo is copied into private storage after cropping; only a
 * file-name reference is kept (never the picker's temporary content URI).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    settings: AppSettings,
    onSave: (name: String, email: String, profilePhotoRef: String?) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(settings.userName) }
    var email by remember { mutableStateOf(settings.userEmail) }
    var photoRef by remember { mutableStateOf(settings.profilePictureUri) }
    var cropSource by remember { mutableStateOf<Bitmap?>(null) }
    var photoError by remember { mutableStateOf<String?>(null) }
    var photoBusy by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // null = the user backed out of the picker; leave everything as it was.
        if (uri != null) scope.launch {
            photoBusy = true
            photoError = null
            ProfileImages.loadForCrop(context, uri).fold(
                onSuccess = { cropSource = it },
                onFailure = { photoError = "That image couldn't be opened. Try a different photo." },
            )
            photoBusy = false
        }
    }
    fun pickPhoto() = photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

    val emailValid = email.isBlank() || Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val isValid = name.trim().isNotEmpty() && email.trim().isNotEmpty() && emailValid

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Personal details",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(CwIcons.Back, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets.safeContent.only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
        ),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = DvideDimens.screen)
                .padding(bottom = DvideDimens.section),
            verticalArrangement = Arrangement.spacedBy(DvideDimens.item),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = DvideDimens.item),
            ) {
                ProfileAvatar(
                    userName = name,
                    imageUri = photoRef,
                    size = 96.dp,
                    onClick = ::pickPhoto,
                    onClickLabel = "Choose profile photo",
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = ::pickPhoto, enabled = !photoBusy) {
                        Text(if (photoRef != null) "Change photo" else "Add photo")
                    }
                    if (photoRef != null) {
                        TextButton(onClick = { photoRef = null; photoError = null }) {
                            Text("Remove", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                if (photoBusy) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                }
                photoError?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(DvideDimens.hairline),
            ) {
                Text(
                    text = "Name",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = EntryLimits.clampInput(it, EntryLimits.PROFILE_NAME_MAX) },
                    placeholder = { Text("Enter your name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_name_field"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(DvideDimens.hairline),
            ) {
                Text(
                    text = "Email",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = EntryLimits.clampInput(it, EntryLimits.PROFILE_EMAIL_MAX) },
                    placeholder = { Text("Enter your email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_email_field"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                    singleLine = true,
                    isError = !emailValid,
                    supportingText = { if (!emailValid) Text("Enter a valid email address, for example name@example.com") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        CwIcons.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                    Column {
                        Text("Local profile", style = MaterialTheme.typography.labelLarge)
                        Text(
                            text = "Not signed in. These details are stored only on this device and are not linked to or verified by any online account.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(DvideDimens.item))

            Button(
                onClick = {
                    if (isValid) {
                        onSave(name.trim(), email.trim(), photoRef)
                        onClose()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(DvideDimens.commit)
                    .testTag("profile_save_button"),
                shape = ShapeCommitBtn,
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                Icon(imageVector = CwIcons.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(DvideDimens.tight))
                Text(
                    text = "Save profile",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }

    cropSource?.let { bitmap ->
        ProfileCropDialog(
            source = bitmap,
            onDismiss = { cropSource = null },
            onConfirm = { region ->
                cropSource = null
                scope.launch {
                    photoBusy = true
                    ProfileImages.saveAvatar(context, bitmap, region).fold(
                        onSuccess = { photoRef = it; photoError = null },
                        onFailure = { photoError = "The photo couldn't be saved. Check your free storage and try again." },
                    )
                    photoBusy = false
                }
            },
        )
    }
}

// Compatibility overload for 2-parameter onSave calls
@Composable
fun ProfileScreen(
    settings: AppSettings,
    onSave: (name: String, email: String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ProfileScreen(
        settings = settings,
        onSave = { name, email, _ -> onSave(name, email) },
        onClose = onClose,
        modifier = modifier,
    )
}
