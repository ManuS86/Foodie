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
import com.facebook.AccessToken
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.android.libraries.places.api.model.Place
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private var analytics: FirebaseAnalytics
    private val applicationContext = application
    private val auth = Firebase.auth
    private val fireStore = Firebase.firestore
    private val TAG = "FirebaseViewModel"
    private lateinit var profileRef: DocumentReference
    private lateinit var resendToken: ForceResendingToken
    private lateinit var storedVerificationId: String

    private var _currentUser = MutableLiveData<FirebaseUser?>(auth.currentUser)
    val currentUser: LiveData<FirebaseUser?>
        get() = _currentUser

    private var _likedRestaurants = MutableLiveData<List<Place>>()
    val likedRestaurants: LiveData<List<Place>>
        get() = _likedRestaurants

    private var _dismissedRestaurants = MutableLiveData<List<Place>>()
    val dismissedRestaurants: LiveData<List<Place>>
        get() = _dismissedRestaurants

    private var _visitedRestaurants = MutableLiveData<List<Place>>()
    val visitedRestaurants: LiveData<List<Place>>
        get() = _visitedRestaurants

    init {
        if (auth.currentUser != null) {
            setUpUserEnv()
        }
        analytics = Firebase.analytics
    }

    private fun setUpUserEnv() {
        _currentUser.value = auth.currentUser
        profileRef =
            fireStore.collection("users").document(auth.currentUser?.uid!!)
    }

    fun addNewRestaurant(collection: String, restaurantName: String, restaurant: Place) {
        profileRef.collection(collection).document(restaurantName).set(restaurant)
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
                        "Welcome back",
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

    fun sendVerificationCode(activity: Activity, phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(storedVerificationId, code)
        signInWithPhoneAuthCredential(credential)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d(TAG, "onVerificationCompleted:$credential")
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.w(TAG, "onVerificationFailed", e)
            when (e) {
                is FirebaseAuthInvalidCredentialsException -> Log.e(TAG, "Invalid request", e)
                is FirebaseTooManyRequestsException -> Log.e(
                    TAG,
                    "The SMS quota for the project has been exceeded",
                    e
                )

                is FirebaseAuthMissingActivityForRecaptchaException -> Log.e(
                    TAG,
                    "reCAPTCHA verification attempted with null Activity",
                    e
                )
            }
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d(TAG, "onCodeSent:$verificationId")

            // Save verification ID and resending token so we can use them later
            storedVerificationId = verificationId
            resendToken = token
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _currentUser.value = auth.currentUser

                    Toast.makeText(
                        applicationContext,
                        "Welcome ${_currentUser.value?.phoneNumber}",
                        Toast.LENGTH_SHORT,
                    ).show()
                } else {
                    _currentUser.postValue(null)

                    Toast.makeText(
                        applicationContext,
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