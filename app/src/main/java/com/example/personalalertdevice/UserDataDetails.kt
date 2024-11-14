package com.example.personalalertdevice

import android.content.Context

fun saveUserData(context: Context, userName: String, profilePictureUrl: String?) {
    val sharedPref = context.getSharedPreferences("UserData", Context.MODE_PRIVATE)
    with(sharedPref.edit()) {
        putString("userName", userName)
        putString("profilePictureUrl", profilePictureUrl)
        apply()
    }
}

fun loadUserData(context: Context): Pair<String, String?> {
    val sharedPref = context.getSharedPreferences("UserData", Context.MODE_PRIVATE)
    val userName = sharedPref.getString("userName", "User") ?: "User"
    val profilePictureUrl = sharedPref.getString("profilePictureUrl", null)
    return Pair(userName, profilePictureUrl)
}