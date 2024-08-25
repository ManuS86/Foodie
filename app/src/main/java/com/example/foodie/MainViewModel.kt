package com.example.foodie

import android.Manifest
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place

class MainViewModel : ViewModel() {
    private val REQUEST_CODE_LOCATION_PERMISSION = 1

    private var _gpsProvider = MutableLiveData<Boolean>()
    val gpsProvider: LiveData<Boolean>
        get() = _gpsProvider

    private var _locationPermission = MutableLiveData<Boolean?>()
    val locationPermission: LiveData<Boolean?>
        get() = _locationPermission

    private var _currentLocation = MutableLiveData<Location?>()
    val currentLocation: LiveData<Location?>
        get() = _currentLocation

    private val _lastLocationUpdate = MutableLiveData<Long>()
    val lastLocationUpdate: LiveData<Long>
        get() = _lastLocationUpdate

    private var _currentRestaurants = MutableLiveData<List<Place>>()
    val currentRestaurants: LiveData<List<Place>>
        get() = _currentRestaurants

    fun handleLocationRequest(activity: Activity) {
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), REQUEST_CODE_LOCATION_PERMISSION
            )
        } else {
            _locationPermission.value = true
        }
    }

    fun checkLocationPermission(context: Context) {
        _locationPermission.postValue(
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    fun nullLocationPermission() {
        _locationPermission.value = null
    }

    fun isGPSEnabled(context: Context) {
        try {
            val locationManager = context.getSystemService(LocationManager::class.java)
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

            _gpsProvider.postValue(isGpsEnabled)
        } catch (e: Exception) {
            Log.e(TAG, "GPS enabled check failed: $e")
        }
    }

    fun requestLocationUpdates(activity: Activity) {
        val fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(activity)

        val locationRequest = LocationRequest.Builder(10000)
            .setMinUpdateIntervalMillis(5000)
            .setPriority(PRIORITY_HIGH_ACCURACY)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation

                _currentLocation.postValue(location)
                _lastLocationUpdate.postValue(System.currentTimeMillis())
            }
        }

        checkLocationPermission(activity)
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun createPlacesClient(activity: Activity) {
        // Define a variable to hold the Places API key.
        val apiKey = BuildConfig.PLACES_API_KEY

        // Log an error if apiKey is not set.
        if (apiKey.isEmpty() || apiKey == "DEFAULT_API_KEY") {
            Log.e("Places test", "No api key")
            return
        }

        // Initialize the SDK
        Places.initializeWithNewPlacesApiEnabled(activity, apiKey)

        // Create a new PlacesClient instance
        val placesClient = Places.createClient(activity)
    }
}