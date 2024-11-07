package com.example.personalalertdevice.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateListOf
import com.example.personalalertdevice.Contact

class ContactsViewModel : ViewModel() {
    val designatedContacts = mutableStateListOf<Contact>()
}