package com.example.foodie

import android.app.Activity
import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.foodie.data.model.Restaurant
import com.facebook.AccessToken
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "FirebaseViewModel"
    private val auth = Firebase.auth
    private val applicationContext = application
    private val fireStore = Firebase.firestore
    private var analytics: FirebaseAnalytics
    private lateinit var profileRef: DocumentReference

    private var _currentUser = MutableLiveData<FirebaseUser?>(auth.currentUser)
    val currentUser: LiveData<FirebaseUser?>
        get() = _currentUser

    init {
        if (auth.currentUser != null) {
            setUpUserEnv()
        }
        analytics = Firebase.analytics
    }

    private fun setUpUserEnv() {
        _currentUser.value = auth.currentUser
        profileRef =
            fireStore.collection("profiles").document(auth.currentUser?.uid!!)
    }

    fun addNewRestaurant(collection: String, restaurantName: String, restaurantData: Restaurant) {
        profileRef.collection(collection).document(restaurantName).set(restaurantData)
    }

    private fun getCredentialRequest(filterByAuthorizedAccounts: Boolean): GetCredentialRequest {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
            .setServerClientId(applicationContext.getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(true)
            .build()

        return GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    fun handleGoogleSignIn() {
        val credentialManager = CredentialManager.create(applicationContext)

        viewModelScope.launch {
            try {
                val result = credentialManager.getCredential(
                    applicationContext,
                    getCredentialRequest(true)
                )
                handleSignIn(result)
            } catch (ex: GetCredentialException) {
                Log.e("Credential retrieval failed:", ex.toString())
                try {
                    val result = credentialManager.getCredential(
                        applicationContext,
                        getCredentialRequest(false)
                    )
                    handleSignIn(result)

                    Toast.makeText(
                        applicationContext,
                        "Welcome ${_currentUser.value?.email}",
                        Toast.LENGTH_SHORT,
                    ).show()

                } catch (ex: GetCredentialException) {
                    Log.e("Credential retrieval failed:", ex.toString())

                    Toast.makeText(
                        applicationContext,
                        "Add a Google account to your device.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }

    private fun handleSignIn(result: GetCredentialResponse) {
        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken

                        // Use ID token for Firebase authentication
                        firebaseAuthWithGoogle(idToken)
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

    private fun firebaseAuthWithGoogle(idToken: String) {
        Log.d(TAG, "firebaseAuthWithGoogle:$idToken")

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    _currentUser.value = auth.currentUser

                    Toast.makeText(
                        applicationContext,
                        "Welcome back ${_currentUser.value?.email}",
                        Toast.LENGTH_SHORT,
                    ).show()

                    setUpUserEnv()
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    _currentUser.postValue(null)

                    Toast.makeText(
                        applicationContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
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
                    _currentUser.value = auth.currentUser

                    Toast.makeText(
                        activity,
                        "Welcome ${_currentUser.value?.email}",
                        Toast.LENGTH_SHORT,
                    ).show()

                    setUpUserEnv()
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    _currentUser.postValue(null)

                    Toast.makeText(
                        activity,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
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
                    _currentUser.value = null
                }
            }
    }
}