package com.example.foodie

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facebook.AccessToken
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val TAG = "LoginViewModel"

    private var _currentUser = MutableLiveData<FirebaseUser?>(auth.currentUser)
    val currentUser: LiveData<FirebaseUser?>
        get() = _currentUser

    private fun getCredentialRequest(
        activity: Activity,
        filterByAuthorizedAccounts: Boolean
    ): GetCredentialRequest {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
            .setServerClientId(activity.getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(true)
            .build()
        return GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    fun handleGoogleSignIn(activity: Activity) {
        val credentialManager = CredentialManager.create(activity)

        viewModelScope.launch {
            try {
                val result =
                    credentialManager.getCredential(activity, getCredentialRequest(activity, true))
                handleSignIn(result, activity)
                Toast.makeText(
                    activity,
                    "Welcome back ${_currentUser.value?.email}",
                    Toast.LENGTH_SHORT,
                ).show()
            } catch (ex: GetCredentialException) {
                Log.e("Credential retrieval failed:", ex.toString())
                try {
                    val result = credentialManager.getCredential(
                        activity,
                        getCredentialRequest(activity, false)
                    )
                    handleSignIn(result, activity)
                    Toast.makeText(
                        activity,
                        "Welcome ${_currentUser.value?.email}",
                        Toast.LENGTH_SHORT,
                    ).show()
                } catch (ex: GetCredentialException) {
                    Log.e("Credential retrieval failed:", ex.toString())
                    Toast.makeText(
                        activity,
                        "Add a Google account to your device.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }

    private fun handleSignIn(result: GetCredentialResponse, activity: Activity) {

        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(credential.data)

                        val idToken = googleIdTokenCredential.idToken
                        // Use ID token for Firebase authentication
                        firebaseAuthWithGoogle(idToken, activity)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Received an invalid google id token response", e)
                    }
                } else {
                    // Handle other custom credential types
                    Log.e(TAG, "Unexpected type of credential")
                }
            }

            else -> {
                // Handle unrecognized credential types
                Log.e(TAG, "Unexpected type of credential")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String, activity: Activity) {
        Log.d(TAG, "firebaseAuthWithGoogle:$idToken")

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    _currentUser.value = auth.currentUser
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(
                        activity,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    _currentUser.postValue(null)
                }
            }
    }

    fun handleFacebookAccessToken(token: AccessToken, activity: Activity) {
        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    _currentUser.value = (auth.currentUser)
                    Toast.makeText(
                        activity,
                        "Welcome ${_currentUser.value?.email}",
                        Toast.LENGTH_SHORT,
                    ).show()
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(
                        activity,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    _currentUser.postValue(null)
                }
            }
    }

    fun logout() {
        auth.signOut()
        _currentUser.value = null
    }

    fun deleteUser() {
        currentUser.value?.delete()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "User account deleted.")
                }
                _currentUser.value = null
            }
    }
}