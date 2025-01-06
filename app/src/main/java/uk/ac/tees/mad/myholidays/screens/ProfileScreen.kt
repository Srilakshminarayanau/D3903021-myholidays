package uk.ac.tees.mad.myholidays.screens

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import uk.ac.tees.mad.myholidays.viewmodels.ProfileViewModel
import uk.ac.tees.mad.myholidays.viewmodels.UserProfile
import uk.ac.tees.mad.myholidays.viewmodels.UserState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    navController: NavController,
) {
    val userState by viewModel.userState.collectAsState()
    val profileImageUri by viewModel.profileImageUri.collectAsState()
    val context = LocalContext.current

    var showEditDialog by remember { mutableStateOf(false) }
    var showImagePicker by remember { mutableStateOf(false) }

    val cameraPermission = rememberPermissionState(
        android.Manifest.permission.CAMERA
    ) { granted ->
        if (granted) {
            showImagePicker = true
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            // Convert bitmap to URI and save
            val uri = saveImageToInternalStorage(context, bitmap)

            uri?.let { viewModel.updateProfileImage(it) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Logout")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.logout()
                            navController.navigate("auth") {
                                popUpTo("profile")
                            }
                        }
                    ) {
                        Icon(Icons.Default.ExitToApp, "Logout")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image
            Card(
                modifier = Modifier
                    .size(120.dp)
                    .clickable {
                        if (cameraPermission.status.isGranted) {
                            showImagePicker = true
                        } else {
                            cameraPermission.launchPermissionRequest()
                        }
                    },
                shape = CircleShape,
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (profileImageUri != null) {
                        AsyncImage(
                            model = profileImageUri,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (val state = userState) {
                is UserState.Success -> {
                    ProfileContent(
                        profile = state.profile,
                        onEditClick = { showEditDialog = true }
                    )
                }

                is UserState.Loading -> {
                    CircularProgressIndicator()
                }

                is UserState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                else -> Unit
            }
        }

        // Edit Profile Dialog
        if (showEditDialog) {
            val currentProfile = (userState as? UserState.Success)?.profile
            EditProfileDialog(
                currentProfile = currentProfile,
                onDismiss = { showEditDialog = false },
                onSave = { name, email ->
                    viewModel.updateUserProfile(name, email)
                    showEditDialog = false
                }
            )
        }

        // Image Picker Dialog
        if (showImagePicker) {
            ImagePickerDialog(
                onDismiss = { showImagePicker = false },
                onCameraSelected = {
                    launcher.launch(null)
                    showImagePicker = false
                },
                onGallerySelected = {
                    // Handle gallery selection
                    showImagePicker = false
                }
            )
        }
    }
}

@Composable
fun ProfileContent(
    profile: UserProfile,
    onEditClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Profile Details",
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, "Edit Profile")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ProfileField(
                icon = Icons.Default.Person,
                label = "Name",
                value = profile.name
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileField(
                icon = Icons.Default.Email,
                label = "Email",
                value = profile.email
            )
        }
    }
}

@Composable
fun ProfileField(
    icon: ImageVector,
    label: String,
    value: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun EditProfileDialog(
    currentProfile: UserProfile?,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
) {
    var name by remember { mutableStateOf(currentProfile?.name ?: "") }
    var email by remember { mutableStateOf(currentProfile?.email ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, email) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ImagePickerDialog(
    onDismiss: () -> Unit,
    onCameraSelected: () -> Unit,
    onGallerySelected: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Image Source") },
        text = {
            Column {
                TextButton(
                    onClick = onCameraSelected,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Take Photo")
                }
                TextButton(
                    onClick = onGallerySelected,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Choose from Gallery")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Utility function to save image
private fun saveImageToInternalStorage(context: Context, bitmap: Bitmap): Uri? {
    return try {
        val filename = "profile_${System.currentTimeMillis()}.jpg"
        context.openFileOutput(filename, Context.MODE_PRIVATE).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        }
        Uri.parse("file://${context.filesDir}/$filename")
    } catch (e: Exception) {
        null
    }
}