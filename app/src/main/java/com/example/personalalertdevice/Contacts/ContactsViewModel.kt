package com.example.personalalertdevice.Contacts

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class ContactsViewModel : ViewModel() {
    val designatedContacts = mutableStateListOf<Contact>() // Store Contact objects
    private val firestore = FirebaseFirestore.getInstance()
    private var userId: String? = null
    private var userDocRef = firestore.collection("Users").document("default")

    init {
        fetchUserIdAndLoadContacts()
    }

    private fun fetchUserIdAndLoadContacts() {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            userId = currentUser.uid
            userDocRef = firestore.collection("Users").document(userId!!)
            loadDesignatedContacts()
        }
    }

    private fun loadDesignatedContacts() {
        if (userId == null) {
            Log.e("ContactsViewModel", "Unable to load contacts.")
            return
        }

        userDocRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.contains("designated contacts")) {
                    val contactsList = document["designated contacts"] as? List<Map<String, String>> ?: emptyList()

                    designatedContacts.clear()
                    designatedContacts.addAll(
                        contactsList.map { Contact(it["id"] ?: "", it["name"] ?: "Unknown", it["phone number"] ?: "No Number") }                    )

                    Log.d("Firestore", "Loaded designated contacts: $designatedContacts")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to load designated contacts: $e")
            }
    }

    // Add Contact with Phone Number
    fun addContact(contact: Contact) {
        if (designatedContacts.any { it.id == contact.id }) return

        designatedContacts.add(contact)

        val contactMap = mapOf("id" to contact.id, "name" to contact.name, "phone number" to contact.phoneNumber)
        userDocRef.update("designated contacts", FieldValue.arrayUnion(contactMap))
            .addOnSuccessListener {
                Log.d("Firestore", "Added contact: $contact")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to add contact: $e")
            }
    }

    // Remove Contact
    fun removeContact(contact: Contact) {
        designatedContacts.removeAll { it.id == contact.id }

        val contactMap = mapOf("id" to contact.id, "name" to contact.name, "phoneNumber" to contact.phoneNumber)
        userDocRef.update("designatedContacts", FieldValue.arrayRemove(contactMap))
            .addOnSuccessListener {
                Log.d("Firestore", "Removed contact: $contact")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to remove contact: $e")
            }
    }
}



