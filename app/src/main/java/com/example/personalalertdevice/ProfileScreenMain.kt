package com.example.personalalertdevice

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.*



@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ProfileScreenMain(
    navController: NavController,
    userId: String,
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    val context = LocalContext.current
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    val factory = ProfilePictureViewModelFactory(firestore)
    val viewModel: ProfilePictureViewModel = viewModel(factory = factory)

    LaunchedEffect(userId) {
        viewModel.loadProfileImage(userId)
    }

    val profileImageUrl by remember { viewModel.profileImageUrl }

    if (!profileImageUrl.isNullOrEmpty()) {
        profileImageUri = Uri.parse(profileImageUrl)
    }

    val photoFile = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "profile_photo.jpg")
    val tempUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", photoFile)

    // Crop launcher
    val cropLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.let { data ->
                val outputUri = UCrop.getOutput(data)
                if (outputUri != null) {
                    profileImageUri = outputUri
                    // Save the profile image URL to Firestore
                    viewModel.saveProfileImage(userId, outputUri.toString())
                } else {
                    Toast.makeText(context, "Crop error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Image picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            startCrop(context, it, cropLauncher)
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) startCrop(context, tempUri, cropLauncher)
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val cameraGranted = permissions[android.Manifest.permission.CAMERA] ?: false
        val storageGranted = permissions[android.Manifest.permission.READ_MEDIA_IMAGES]
            ?: permissions[android.Manifest.permission.READ_EXTERNAL_STORAGE]
            ?: false

        when {
            cameraGranted -> cameraLauncher.launch(tempUri)
            storageGranted -> galleryLauncher.launch("image/*")
            else -> Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
        }
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
                fontWeight = FontWeight.Bold,
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
                .clip(RoundedCornerShape(100.dp))
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

    // Image Picker Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Picture of Yourself", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }
            },
            text = {
                Column {
                    Button(
                        onClick = {
                            permissionLauncher.launch(arrayOf(android.Manifest.permission.CAMERA))
                            showDialog = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xff518752)),
                        shape = RoundedCornerShape(5.dp)
                    ) {
                        Text("Take Photo", fontSize = 22.sp)
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xff518752)),
                        shape = RoundedCornerShape(5.dp)
                    ) {
                        Text("Choose from Gallery", fontSize = 22.sp)
                    }

                    Button(
                        onClick = { showDialog = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        shape = RoundedCornerShape(5.dp)
                    ) {
                        Text("Cancel", fontSize = 22.sp)
                    }
                }
            },
            confirmButton = {}
        )
    }

}



fun startCrop(context: Context, sourceUri: Uri, cropLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
    val destinationUri = Uri.fromFile(File(context.cacheDir, "cropped_${UUID.randomUUID()}.jpg"))
    val options = UCrop.Options().apply {
        setCompressionQuality(80)
        setFreeStyleCropEnabled(true)
        setCircleDimmedLayer(true)
    }

    // temp perms
    context.grantUriPermission(context.packageName, sourceUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

    val cropIntent = UCrop.of(sourceUri, destinationUri)
        .withAspectRatio(1f, 1f)
        .withMaxResultSize(500, 500)
        .withOptions(options)
        .getIntent(context)

    cropLauncher.launch(cropIntent)
}

