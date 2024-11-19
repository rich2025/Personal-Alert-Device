import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class ContactsViewModel : ViewModel() {
    val designatedContacts = mutableStateListOf<String>() // Mutable state for UI updates
    private val firestore = FirebaseFirestore.getInstance()
    private var userId: String? = null
    private var userDocRef = firestore.collection("Users").document("default")

    init {
        fetchUserIdAndLoadContacts()
    }

    // Function to check user authentication and load designated contacts into firestore initially
    private fun fetchUserIdAndLoadContacts() {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        // Load designated contacts if user authenticated
        if (currentUser != null) {
            userId = currentUser.uid
            userDocRef = firestore.collection("Users").document(userId!!)
            loadDesignatedContacts()
        } else {
            // Listen for authentication state changes
            auth.addAuthStateListener { updatedAuth ->
                val updatedUser = updatedAuth.currentUser
                if (updatedUser != null) {
                    userId = updatedUser.uid
                    userDocRef = firestore.collection("Users").document(userId!!)
                    loadDesignatedContacts()
                } else {
                    Log.e("ContactsViewModel", "User is not authenticated.")
                }
            }
        }
    }

    private fun loadDesignatedContacts() {

        if (userId == null) {
            Log.e("ContactsViewModel", "Unable to load contacts.")
            return
        }
        userDocRef.get()
            .addOnSuccessListener { document ->

                //reuploadDesignatedContacts()
                Log.d("ContactsViewModel", "Designated contacts on app initialization: $designatedContacts")

                if (document != null && document.contains("Designated Contacts")) {
                    val contacts = document["Designated Contacts"] as? List<String> ?: emptyList()

                    // Update the designatedContacts list and log the current contacts
                    designatedContacts.clear()
                    designatedContacts.addAll(contacts)
                    Log.d("Firestore", "Loaded designated contacts: $designatedContacts")
                } else {
                    Log.d("Firestore", "No designated contacts found.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to load designated contacts: $e")
            }
    }

//    private fun reuploadDesignatedContacts() {
//        userDocRef.update("Designated Contacts", FieldValue.arrayUnion(*designatedContacts.toTypedArray()))
//            .addOnSuccessListener {
//                Log.d("Firestore", "Re-uploaded designated contacts to Firestore: $designatedContacts")
//            }
//            .addOnFailureListener { e ->
//                Log.e("Firestore", "Failed to re-upload designated contacts: $e")
//            }
//    }


    // Update Firestore with the latest list of designated contacts
    private fun updateFirestoreContacts(contacts: List<String>) {
        userDocRef.update("Designated Contacts", FieldValue.arrayUnion(*contacts.toTypedArray()))
            .addOnSuccessListener {
                Log.d("Firestore", "Successfully updated Firestore with designated contacts: $contacts")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to update Firestore with designated contacts: $e")
            }
    }

    // Add designated contact to both the ViewModel and Firestore
    fun addContact(contact: String) {
        if (!designatedContacts.contains(contact)) {
            designatedContacts.add(contact)
            userDocRef.update("Designated Contacts", FieldValue.arrayUnion(contact))
                .addOnSuccessListener {
                    Log.d("Firestore", "Added contact: $contact")
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Failed to add contact: $e")
                }
        }
    }

    // Remove designated contact from ViewModel and Firestore
    fun removeContact(contact: String) {
        if (designatedContacts.contains(contact)) {
            designatedContacts.remove(contact)
            userDocRef.update("Designated Contacts", FieldValue.arrayRemove(contact))
                .addOnSuccessListener {
                    Log.d("Firestore", "Removed contact: $contact")
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Failed to remove contact: $e")
                }
        }
    }
}


