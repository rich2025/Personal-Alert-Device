import android.Manifest
import android.content.Context
import android.provider.ContactsContract
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
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

data class Contact(val id: String, val name: String, val phoneNumber: String)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestContactPermission(onPermissionGranted: () -> Unit) {
    val context = LocalContext.current
    val contactPermissionState = rememberPermissionState(permission = Manifest.permission.READ_CONTACTS)

    if (contactPermissionState.status.isGranted) {
        onPermissionGranted()
    } else {
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

            var phoneNumber = "No Phone Number"
            phoneCursor?.use { pc ->
                if (pc.moveToFirst()) {
                    phoneNumber = pc.getString(pc.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                }
            }
            contactsList.add(Contact(id, name, phoneNumber))
        }
    }

    return contactsList.sortedBy { it.name }
}

@Composable
fun ContactsListScreen(navController: NavController) {
    val context = LocalContext.current
    var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }

    RequestContactPermission(onPermissionGranted = {
        contacts = getContacts(context)
    })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            items(contacts) { contact ->
                ContactItem(contact)
            }
        }
    }
}

@Composable
fun ContactItem(contact: Contact) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "${contact.name}: ${contact.phoneNumber}",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp)
        )
    }
}
