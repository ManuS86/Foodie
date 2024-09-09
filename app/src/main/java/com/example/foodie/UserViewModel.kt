package com.example.foodie

import android.app.Activity
import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.foodie.data.FirestoreRepository
import com.example.foodie.data.model.AppSettings
import com.example.foodie.data.model.DiscoverySettings
import com.example.foodie.data.model.Id
import com.facebook.AccessToken
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private var analytics: FirebaseAnalytics
    private val applicationContext = application
    private val auth = Firebase.auth
    private val db = FirebaseFirestore.getInstance()
    val firestoreRepository = FirestoreRepository(db)
    private val TAG = "UserViewModel"
    private lateinit var userRef: DocumentReference
    private lateinit var resendToken: ForceResendingToken
    private lateinit var storedVerificationId: String

    private var _currentUser = MutableLiveData<FirebaseUser?>(auth.currentUser)
    val currentUser: LiveData<FirebaseUser?>
        get() = _currentUser

    private var _likesIds = MutableLiveData<MutableList<String>>()
    val likesIds: LiveData<MutableList<String>>
        get() = _likesIds

    private var _nopesIds = MutableLiveData<MutableList<String>>()
    val nopesIds: LiveData<MutableList<String>>
        get() = _nopesIds

    private var _historyIds = MutableLiveData<MutableList<String>>()
    val historyIds: LiveData<MutableList<String>>
        get() = _historyIds

    private var _currentAppSettings = MutableLiveData<AppSettings>()
    val currentAppSettings: LiveData<AppSettings>
        get() = _currentAppSettings

    private var _currentDiscoverySettings = MutableLiveData<DiscoverySettings>()
    val currentDiscoverySettings: LiveData<DiscoverySettings>
        get() = _currentDiscoverySettings

    init {
        if (auth.currentUser != null) {
            setUpUserEnv()
        }
        AppCompatDelegate.setDefaultNightMode(
            _currentAppSettings.value?.nightmode ?: AppSettings().nightmode
        )
        analytics = Firebase.analytics
    }

    private fun setUpUserEnv() {
        _currentUser.value = auth.currentUser
        userRef = firestoreRepository.getUserRef(auth.currentUser?.uid!!)
        loadAppSettings()
        loadDiscoverySettings()
        loadRestaurantIdList("history")
        loadRestaurantIdList("likes")
        loadRestaurantIdList("nopes")
    }

    private fun loadAppSettings() {
        viewModelScope.launch {
            try {
                val settings = firestoreRepository.getAppSettings(userRef)
                _currentAppSettings.postValue(settings ?: AppSettings())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to retrieve app settings", e)
            }
        }
    }

    fun saveAppSettings() {
        viewModelScope.launch {
            try {
                if (_currentAppSettings.value != null) {
                    firestoreRepository.setAppSettings(userRef, _currentAppSettings.value!!)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save app settings", e)
            }
        }
    }

    private fun loadDiscoverySettings() {
        viewModelScope.launch {
            try {
                val settings = firestoreRepository.getDiscoverySettings(userRef)
                _currentDiscoverySettings.postValue(settings ?: DiscoverySettings())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to retrieve discovery settings", e)
            }
        }
    }

    fun saveDiscoverySettings() {
        viewModelScope.launch {
            try {
                if (_currentAppSettings.value != null) {
                    firestoreRepository.setDiscoverySettings(
                        userRef,
                        _currentDiscoverySettings.value!!
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save discovery settings", e)
            }
        }
    }

    fun saveRestaurant(collection: String, restaurantName: String, restaurantId: Id) {
        viewModelScope.launch {
            try {
                firestoreRepository.addRestaurant(
                    userRef,
                    collection,
                    restaurantName,
                    restaurantId
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add $restaurantName to $collection", e)
            }
        }
    }

    private fun loadRestaurantIdList(collection: String) {
        viewModelScope.launch {
            try {
                when (collection) {
                    "history" -> _historyIds.postValue(
                        firestoreRepository.getRestaurantIdList(
                            userRef,
                            collection
                        )
                    )

                    "likes" -> _likesIds.postValue(
                        firestoreRepository.getRestaurantIdList(
                            userRef,
                            collection
                        )
                    )

                    "nopes" -> _nopesIds.postValue(
                        firestoreRepository.getRestaurantIdList(
                            userRef,
                            collection
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to retrieve $collection", e)
            }
        }
    }

    fun deleteLikedRestaurant(restaurantName: String) {
        viewModelScope.launch {
            try {
                firestoreRepository.deleteLikedRestaurant(userRef, restaurantName)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete $restaurantName from likes", e)
            }
        }
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

        viewModelScope.launch {
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
    }

    fun handleFacebookAccessToken(token: AccessToken, activity: Activity) {
        Log.d(TAG, "handleFacebookAccessToken:$token")

        viewModelScope.launch {
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
                is FirebaseAuthInvalidCredentialsException -> Log.e(
                    TAG,
                    "Invalid request",
                    e
                )

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