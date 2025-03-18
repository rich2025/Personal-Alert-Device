package com.example.personalalertdevice.Profile

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.TextField
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import com.example.personalalertdevice.R
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

    val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(firestore))

    LaunchedEffect(userId) {
        profileViewModel.loadProfileData(userId)
    }

    val profileData = profileViewModel.profileData.value

    val name = profileData?.get("full name") ?: ""
    val age = profileData?.get("age") ?: ""
    val gender = profileData?.get("gender") ?: ""
    val weight = profileData?.get("weight") ?: ""
    val height = profileData?.get("height") ?: ""
    val address = profileData?.get("address") ?: ""


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
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(
                    text = "RETURN",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Main Screen",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        // Profile Picture
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(top = 20.dp)
                .size(250.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
                .clickable { showDialog = true }
        ) {
            if (profileImageUri != null) {
                Image(
                    painter = rememberImagePainter(profileImageUri),
                    contentDescription = "Picture of Yourself",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else if (profileImageUri == null){
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Upload a Picture of Yourself",
                    modifier = Modifier.size(80.dp),
                    tint = Color.DarkGray
                )
            }
        }

        // profile data display
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 15.dp)
                .heightIn(min = 180.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
                    .background(Color(0xffebeced), RoundedCornerShape(8.dp))
            ) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 16.dp)
                ) {
                    ProfileLabelValue(label = "Name", value = name)
                    ProfileLabelValue(label = "Age", value = age)
                    ProfileLabelValue(label = "Gender", value = gender)
                    ProfileLabelValue(label = "Weight", value = weight)
                    ProfileLabelValue(label = "Height", value = height)
                    ProfileLabelValue(label = "Address", value = address)
                }
            }

            Button(
                onClick = { navController.navigate("ProfileScreen") },
                modifier = Modifier
                    .height(203.dp)
                    .width(50.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF558f4f)),
                contentPadding = PaddingValues(0.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .size(100.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.pencil),
                        contentDescription = "Edit",
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Edit",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                }
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

@Composable
fun ProfileLabelValue(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 5.dp, end = 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.Black
        )
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.DarkGray
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

