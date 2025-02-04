package com.example.personalalertdevice

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.material3.Button
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import coil.compose.rememberImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ProfileScreenMain(
    navController: NavController,
    userId: String,
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val context = LocalContext.current

    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    val photoFile = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "profile_photo.jpg")
    val tempUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", photoFile)

    // Image picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { profileImageUri = it }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) profileImageUri = tempUri
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val cameraGranted = permissions[android.Manifest.permission.CAMERA] ?: false
        val storageGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[android.Manifest.permission.READ_MEDIA_IMAGES] ?: false
        } else {
            permissions[android.Manifest.permission.READ_EXTERNAL_STORAGE] ?: false
        }

        if (cameraGranted) cameraLauncher.launch(tempUri)
        if (storageGranted) galleryLauncher.launch("image/*")
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Return Button
        Button(
            onClick = { navController.navigate("MainScreen") },
            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .align(Alignment.Start)
                .padding(top = 40.dp, bottom = 16.dp, start = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black,
                modifier = Modifier.size(45.dp)
            )
            Text(
                text = "RETURN",
                fontSize = 30.sp,
                color = Color.Black,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Profile Picture
        Box(
            modifier = Modifier
                .size(250.dp)
                .padding(top = 30.dp)
                .clickable { showDialog = true }
                .clip(RoundedCornerShape(16.dp))
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            if (profileImageUri != null) {
                Image(
                    painter = rememberImagePainter(profileImageUri),
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Upload Profile Picture",
                    modifier = Modifier.size(80.dp),
                    tint = Color.DarkGray
                )
            }
        }
    }

    // image picker
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select Option") },
            text = {
                Column {
                    Button(
                        onClick = {
                            permissionLauncher.launch(arrayOf(android.Manifest.permission.CAMERA))
                            showDialog = false
                        },
                        modifier = Modifier.fillMaxWidth().padding(4.dp)
                    ) {
                        Text("Take Photo")
                    }
                    Button(
                        onClick = {
                            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                android.Manifest.permission.READ_MEDIA_IMAGES
                            } else {
                                android.Manifest.permission.READ_EXTERNAL_STORAGE
                            }
                            permissionLauncher.launch(arrayOf(permission))
                            showDialog = false
                        },
                        modifier = Modifier.fillMaxWidth().padding(4.dp)
                    ) {
                        Text("Choose from Gallery")
                    }
                    Button(
                        onClick = { showDialog = false },
                        modifier = Modifier.fillMaxWidth().padding(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text("Cancel")
                    }
                }
            },
            confirmButton = {}
        )
    }
}
