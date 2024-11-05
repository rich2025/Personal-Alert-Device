package com.example.personalalertdevice

import android.Manifest
import android.content.Context
import android.provider.ContactsContract
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.text.style.TextAlign

// Holds details associated with each contact
data class Contact(val id: String, val name: String, val phoneNumber: String)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestContactPermission(onPermissionGranted: () -> Unit) {
    val context = LocalContext.current
    // Permission state, enable read contacts
    val contactPermissionState = rememberPermissionState(permission = Manifest.permission.READ_CONTACTS)
    if (contactPermissionState.status.isGranted) {
        onPermissionGranted()
    } else {
        // If the permission is not granted, display permission request UI
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFf5f4e4)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val textToShow = if (contactPermissionState.status.shouldShowRationale) {
                    "The app needs access to your contacts to display them. Please grant permission by pressing the button below and then Allow."
                } else {
                    "You have denied contact permission. Please enable it from Settings > Security & Privacy > Privacy Control > Permission Manager > Contacts > Personal Alert Device."
                }
                Text(
                    text = textToShow,
                    fontSize = 30.sp,
                    color = Color(0xFF242424),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(horizontal = 50.dp, vertical = 16.dp)
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )

                // Hide the button if the contacts permission is denied
                if (contactPermissionState.status.shouldShowRationale) {
                    Button(
                        onClick = { contactPermissionState.launchPermissionRequest() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF32a852)),
                        modifier = Modifier
                            .padding(top = 24.dp)
                            .size(300.dp, 100.dp)
                    ) {
                        Text(
                            text = "Request Permission",
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.wrapContentWidth(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }
}

// Retrieve contacts from device contact list
fun getContacts(context: Context): List<Contact> {
    val contactsList = mutableListOf<Contact>()
    val contentResolver = context.contentResolver
    val cursor = contentResolver.query(
        ContactsContract.Contacts.CONTENT_URI,
        null,
        null,
        null,
        null
    )

    cursor?.use {
        // Retrieve contact ID and name
        while (it.moveToNext()) {
            val id = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
            val name = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)) ?: "Unknown"

            val phoneCursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                arrayOf(id),
                null
            )
            // Query for phone number using contact ID
            var phoneNumber = "No Phone Number"
            phoneCursor?.use { pc ->
                if (pc.moveToFirst()) {
                    phoneNumber = pc.getString(pc.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                }
            }
            contactsList.add(Contact(id, name, phoneNumber))
        }
    }
    // Sort alphabetically by name
    return contactsList.sortedBy { it.name }
}

// Group contacts by the first letter of their name
fun groupContactsByLetter(contacts: List<Contact>): Map<Char, List<Contact>> {
    return contacts.groupBy { it.name.firstOrNull() ?: '#' }
}

@Composable
fun ContactsListScreen(navController: NavController) {
    val context = LocalContext.current
    var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
    var groupedContacts by remember { mutableStateOf<Map<Char, List<Contact>>>(emptyMap()) }

    RequestContactPermission(onPermissionGranted = {
        contacts = getContacts(context)
        groupedContacts = groupContactsByLetter(contacts)
    })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFf5f4e4)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Return Button
        Button(
            onClick = { navController.popBackStack() },
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

        Spacer(modifier = Modifier.height(16.dp))

        // Contact list with headers for each letter
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            groupedContacts.forEach { (letter, contactsList) ->
                item {
                    Text(
                        text = letter.toString(),
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                            .background(Color(0xFFE0E0E0), RoundedCornerShape(4.dp))
                    )
                }
                // List contacts under each letter
                items(contactsList) { contact ->
                    ContactItem(contact)
                }
            }
        }
    }
}

// Composable for individual contact item
@Composable
fun ContactItem(contact: Contact) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = contact.name,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        )
        Text(
            text = contact.phoneNumber,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray,
            modifier = Modifier.padding(8.dp)
        )
    }
}
