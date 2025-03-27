package com.example.personalalertdevice.Contacts

import android.Manifest
import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import androidx.compose.foundation.background
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
import com.google.accompanist.permissions.rememberPermissionState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale

// Holds details associated with each contact
data class Contact(val id: String, val name: String, val phoneNumber: String)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestContactPermission(navController: NavController, onPermissionGranted: () -> Unit) {
    val contactPermissionState = rememberPermissionState(permission = Manifest.permission.READ_CONTACTS)
    val updatedOnPermissionGranted by rememberUpdatedState(onPermissionGranted)

    if (contactPermissionState.status.isGranted) {
        updatedOnPermissionGranted()
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Display UI for requesting permission
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(30.dp)
                ) {
                    val textToShow = if (contactPermissionState.status.shouldShowRationale) {
                        "The app needs access to your contacts. Please request and allow permission."
                    } else {
                        "You have denied contact permission. Please enable it from Settings."
                    }

                    Text(
                        text = textToShow,
                        fontSize = 33.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 90.dp)
                    )

                    if (contactPermissionState.status.shouldShowRationale) {
                        Button(
                            onClick = { contactPermissionState.launchPermissionRequest() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF32a852)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .padding(top = 0.dp, bottom = 150.dp)
                                .size(width = 300.dp, height = 120.dp)
                        ) {
                            Text(
                                text = "Request Permission",
                                fontSize = 27.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
            // Sort alphabetically by name
            contactsList.add(Contact(id, name, phoneNumber))
        }
    }
    return contactsList.sortedBy { it.name }
}

// Group contacts by the first letter of their name
fun groupContactsByLetter(contacts: List<Contact>): Map<Char, List<Contact>> {
    return contacts.groupBy { it.name.firstOrNull() ?: '#' }
}

@Composable
fun ContactsListScreen(navController: NavController, viewModel: ContactsViewModel = viewModel()) {
    val context = LocalContext.current
    var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
    var groupedContacts by remember { mutableStateOf<Map<Char, List<Contact>>>(emptyMap()) }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Back button
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
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(
                    text = "RETURN",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Contacts Screen",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        // Request permission and load contacts
        RequestContactPermission(navController = navController, onPermissionGranted = {
            contacts = getContacts(context)
            groupedContacts = groupContactsByLetter(contacts)
        })

        // Contact list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Make LazyColumn take up remaining space
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

                items(contactsList) { contact ->
                    ContactItem(
                        contact = contact,
                        isSelected = viewModel.designatedContacts.any { it.name == contact.name }, // Missing comma fixed
                        onClick = { selectedContact ->
                            if (viewModel.designatedContacts.any { it.id == selectedContact.id }) {
                                viewModel.removeContact(selectedContact)
                            } else {
                                viewModel.addContact(selectedContact)
                            }
                            Log.d("ContactsListScreen", "Designated Contacts List: ${viewModel.designatedContacts}")
                        }
                    )
                }
            }
        }
    }
}

// Composable for each contact
@Composable
fun ContactItem(contact: Contact, isSelected: Boolean, onClick: (Contact) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick(contact) }
            .background(if (isSelected) Color(0xFF32a852) else Color.Transparent)
    ) {
        Text(
            text = contact.name,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else Color.Black,
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        )
        Text(
            text = contact.phoneNumber,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else Color.DarkGray,
            modifier = Modifier.padding(8.dp)
        )
    }
}
