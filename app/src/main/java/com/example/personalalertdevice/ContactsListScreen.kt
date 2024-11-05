package com.example.personalalertdevice

import android.Manifest
import android.content.Context
import android.provider.ContactsContract
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
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale

// Holds details associated with each contact
data class Contact(val id: String, val name: String, val phoneNumber: String)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestContactPermission(onPermissionGranted: () -> Unit) {
    val contactPermissionState = rememberPermissionState(permission = Manifest.permission.READ_CONTACTS)
    val updatedOnPermissionGranted by rememberUpdatedState(onPermissionGranted)

    // Trigger `onPermissionGranted` when permission is granted
    if (contactPermissionState.status.isGranted) {
        updatedOnPermissionGranted()
    } else {
        // Display UI for requesting permission
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                val textToShow = if (contactPermissionState.status.shouldShowRationale) {
                    "The app needs access to your contacts to display them. Please grant permission by pressing the button below."
                } else {
                    "You have denied contact permission. Please enable it from Settings > Security & Privacy > Privacy Control > Permission Manager > Contacts > Personal Alert Device."
                }

                Text(
                    text = textToShow,
                    fontSize = 30.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                // Hide the button if the contacts permission is denied
                if (contactPermissionState.status.shouldShowRationale) {
                    Button(
                        onClick = { contactPermissionState.launchPermissionRequest() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF32a852)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .size(width = 250.dp, height = 80.dp)
                    ) {
                        Text(
                            text = "Request Permission",
                            fontSize = 20.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
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
fun ContactsListScreen(navController: NavController) {
    val context = LocalContext.current
    var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
    var groupedContacts by remember { mutableStateOf<Map<Char, List<Contact>>>(emptyMap()) }
    val designatedContacts = remember { mutableStateListOf<Contact>() }

    RequestContactPermission(onPermissionGranted = {
        contacts = getContacts(context)
        groupedContacts = groupContactsByLetter(contacts)
    })
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
                items(contactsList) { contact ->
                    ContactItem(
                        contact = contact,
                        isSelected = designatedContacts.contains(contact),
                        onClick = { selectedContact ->
                            if (designatedContacts.contains(selectedContact)) {
                                designatedContacts.remove(selectedContact)
                            } else {
                                designatedContacts.add(selectedContact)
                            }
                        }
                    )
                }
            }
        }
    }

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
