package com.example.foodie

import android.app.Application
import android.graphics.Bitmap
import android.icu.util.Calendar
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.PhotoMetadata
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import com.google.android.libraries.places.api.net.SearchNearbyResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class NearbyRestaurantsViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "NearbyRestaurantsViewModel"
    private val applicationContext = application
    private lateinit var placesClient: PlacesClient

    private var _nearbyRestaurants = MutableLiveData<List<Place>>()
    val nearbyRestaurants: LiveData<List<Place>>
        get() = _nearbyRestaurants

    private var _currentRestaurant = MutableLiveData<Place>()
    val currentRestaurant: LiveData<Place>
        get() = _currentRestaurant

    fun setCurrentRestaurant(position: Int) {
        _currentRestaurant.postValue(nearbyRestaurants.value?.get(position))
    }

    init {
        createPlacesClient()
    }

    private fun createPlacesClient() {
        // Define a variable to hold the Places API key.
        val apiKey = BuildConfig.PLACES_API_KEY

        // Log an error if apiKey is not set.
        if (apiKey.isEmpty() || apiKey == "DEFAULT_API_KEY") {
            Log.e(
                "Places test",
                "No api key"
            )
            return
        }

        // Initialize the SDK
        try {
            Places.initializeWithNewPlacesApiEnabled(applicationContext, apiKey)
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Error initializing Places API",
                e
            )
        }

        // Create a new PlacesClient instance
        val placesClientNew = Places.createClient(applicationContext)
        placesClient = placesClientNew
    }

    fun getNearbyRestaurants(
        center: LatLng,
        radius: Double,
        categories: List<String>,
        regionCode: String
    ) {
        val circle = CircularBounds.newInstance(center, radius)

        val placeFields = listOf(
            Place.Field.NAME,
            Place.Field.PHOTO_METADATAS,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG,
            Place.Field.TYPES,
            Place.Field.CURRENT_OPENING_HOURS,
            Place.Field.RATING,
            Place.Field.USER_RATINGS_TOTAL,
            Place.Field.PRICE_LEVEL,
            Place.Field.WEBSITE_URI,
        )

        val searchNearbyRequest = SearchNearbyRequest.builder(circle, placeFields)
            .setIncludedPrimaryTypes(listOf("restaurant"))
            .setIncludedTypes(categories)
            .setRankPreference(SearchNearbyRequest.RankPreference.POPULARITY)
            .setRegionCode(regionCode)
            .build()
        Log.d(
            TAG,
            "Search request built with fields: $placeFields"
        )

        viewModelScope.launch {
            try {
                val response = placesClient.searchNearby(searchNearbyRequest).await()
                _nearbyRestaurants.value = response.places
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "Failed to retrieve nearby places",
                    e
                )
            }
        }
    }

    fun getPhoto(photoMetadata: PhotoMetadata): MutableLiveData<Bitmap?> {
        val photoRequest = FetchPhotoRequest.builder(photoMetadata)
            .setMaxWidth(1000)
            .setMaxHeight(1000)
            .build()

        val liveData = MutableLiveData<Bitmap?>()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = placesClient.fetchPhoto(photoRequest).await()
                liveData.postValue(response.bitmap)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch photo", e)
                liveData.postValue(null) // Or handle the error differently
            }
        }
        return liveData
    }

    private fun isPlaceOpenNow(place: Place): Boolean {
        val openingHours = place.currentOpeningHours
            ?: return false // No opening hours information available

        val currentTime = Calendar.getInstance()

        for (period in openingHours.periods) {
            val openTime = period.open
            val closeTime = period.close

            // Convert opening and closing times to Calendar objects
            val openCalendar = Calendar.getInstance()
            if (openTime != null) {
                // Add 1 as Calendar uses 1-based indexing
                openCalendar.set(Calendar.DAY_OF_WEEK, openTime.day.ordinal + 1)
                openCalendar.set(Calendar.HOUR_OF_DAY, openTime.time.hours)
                openCalendar.set(Calendar.MINUTE, openTime.time.minutes)
            }

            val closeCalendar = Calendar.getInstance()
            if (closeTime != null) {
                closeCalendar.set(Calendar.DAY_OF_WEEK, closeTime.day.ordinal + 1)
                closeCalendar.set(Calendar.HOUR_OF_DAY, closeTime.time.hours)
                closeCalendar.set(Calendar.MINUTE, closeTime.time.minutes)
            }

            // Check if the current time falls within the opening hours
            if (currentTime.after(openCalendar) && currentTime.before(closeCalendar)) {
                return true
            }
        }
        return false
    }
}
