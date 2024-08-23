package com.example.foodie

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place

class MainViewModel : ViewModel() {

    //    private val fusedLocationProviderClient =
//        LocationServices.getFusedLocationProviderClient(activity)
    private val REQUEST_CODE_LOCATION_PERMISSION = 1

    private var _locationPermissionGranted = MutableLiveData<Boolean>()
    val locationPermissionGranted: LiveData<Boolean>
        get() = _locationPermissionGranted

    private var _currentLocation = MutableLiveData<LatLng>()
    val currentLocation: LiveData<LatLng>
        get() = _currentLocation

    private var _currentRestaurants = MutableLiveData<List<Place>>()
    val currentRestaurants: LiveData<List<Place>>
        get() = _currentRestaurants

    fun handleLocationRequest(activity: Activity) {
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permissions
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), REQUEST_CODE_LOCATION_PERMISSION
            )
        } else {
            _locationPermissionGranted.value = true
        }
    }

    fun checkLocationRequest(activity: Activity) {
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            _locationPermissionGranted.value = false
        } else {
            _locationPermissionGranted.value = true
        }
    }

    fun updateLocationPermission() {
        _locationPermissionGranted.value = true
    }

//        fun getCurrentLocation(activity: Activity) {
//
//        fusedLocationProviderClient.getCurrentLocation(
//            Priority.PRIORITY_HIGH_ACCURACY,
//            object : CancellationToken() {
//                override fun onCanceledRequested(p0: OnTokenCanceledListener) =
//                    CancellationTokenSource().token
//
//                override fun isCancellationRequested() = false
//            })
//            .addOnSuccessListener { location: Location? ->
//                if (location == null)
//                    Toast.makeText(activity, "Cannot get location.", Toast.LENGTH_SHORT).show()
//                else {
//                    val lat = location.latitude
//                    val lon = location.longitude
//                }
//
//            }
//    }

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