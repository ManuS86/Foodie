package com.example.foodie.ui.viewmodels

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import kotlinx.coroutines.launch

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private val REQUEST_CODE_LOCATION_PERMISSION = 1
    private val applicationContext = application

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

    init {
        requestLocationUpdates(300000, 150000)
    }

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

    fun checkLocationPermission() {
        _locationPermission.postValue(
            ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    fun nullLocationPermission() {
        _locationPermission.value = null
    }

    fun isGPSEnabled() {
        try {
            val locationManager = applicationContext.getSystemService(LocationManager::class.java)
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

            _gpsProvider.value = isGpsEnabled
        } catch (e: Exception) {
            Log.e(TAG, "GPS enabled check failed: $e")
        }
    }

    fun requestLocationUpdates(update: Long, minUpdate: Long) {
        checkLocationPermission()

        viewModelScope.launch {
            try {
                val fusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(applicationContext)

                val locationRequest = LocationRequest.Builder(update)
                    .setMinUpdateIntervalMillis(minUpdate)
                    .setPriority(PRIORITY_HIGH_ACCURACY)
                    .build()

                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        val location = locationResult.lastLocation

                        _currentLocation.value = location
                        _lastLocationUpdate.postValue(System.currentTimeMillis())
                        Log.d("LocationUpdate", "LiveData Updated")
                    }
                }

                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to request location updates", e)
            }
        }
    }
}