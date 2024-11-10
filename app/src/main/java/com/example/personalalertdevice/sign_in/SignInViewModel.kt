package com.example.personalalertdevice.sign_in

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

class SignInViewModel: ViewModel(){
    private val _state = MutableStateFlow(SignInState())
    val state: StateFlow<SignInState> = _state.asStateFlow()

    fun onSignInResult(signInResult: SignInResult) {
        Timber.d("Received sign-in result: %s", signInResult)
        if (signInResult.data != null) {
            Timber.d("Sign-in Successful for user: %s", signInResult)
            _state.update { it.copy(isSignInSuccessful = true) }
        } else {
            Timber.e("Sign-in failed with error: %s", signInResult.errorMessage)
            _state.update { it.copy(signInError = signInResult.errorMessage) }
        }
    }

    fun resetState() {
        Timber.d("Resetting sign-in state")
        _state.update { SignInState() }
    }
}