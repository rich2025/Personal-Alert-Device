package com.example.personalalertdevice

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

// shared view model for designated contacts
class ContactsViewModel : ViewModel() {
    val designatedContacts = mutableStateListOf<String>()

}