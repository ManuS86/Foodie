package com.example.foodie

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.PhotoMetadata
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import com.google.android.libraries.places.api.net.SearchNearbyResponse

class NearbyRestaurantsViewModel : ViewModel() {
    val TAG = "NearbyRestaurantsViewModel"

    private var _nearbyRestaurants = MutableLiveData<List<Place>>()
    val nearbyRestaurants: LiveData<List<Place>>
        get() = _nearbyRestaurants

    fun getNearbyRestaurants(
        activity: Activity,
        center: LatLng,
        radius: Double,
        categories: List<String>,
        preference: SearchNearbyRequest.RankPreference,
        regionCode: String
    ) {
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
            Places.initializeWithNewPlacesApiEnabled(activity, apiKey)
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Error initializing Places API",
                e
            )
        }

        // Create a new PlacesClient instance
        val placesClient = Places.createClient(activity)

        val circle = CircularBounds.newInstance(center, radius)

        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.PHOTO_METADATAS,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG,
            Place.Field.TYPES,
            Place.Field.CURRENT_OPENING_HOURS,
            Place.Field.OPENING_HOURS,
            Place.Field.RATING,
            Place.Field.USER_RATINGS_TOTAL,
            Place.Field.PRICE_LEVEL,
            Place.Field.WEBSITE_URI,
        )

        val searchNearbyRequest = SearchNearbyRequest.builder(circle, placeFields)
            .setIncludedPrimaryTypes(listOf("restaurant"))
            .setIncludedTypes(categories)
            .setRankPreference(preference)
            .setRegionCode(regionCode)
            .build()
        Log.d(
            TAG,
            "Search request built with fields: $placeFields"
        )

        try {
            placesClient.searchNearby(searchNearbyRequest)
                .addOnSuccessListener { response: SearchNearbyResponse ->
                    _nearbyRestaurants.value = response.places
                }
                .addOnFailureListener { exception ->
                    Log.e(
                        TAG,
                        "Failed to retrieve nearby places",
                        exception
                    )
                }
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Error searching nearby places",
                e
            )
        }
    }

    fun getPhotos(activity: Activity, photoMetadataList: List<PhotoMetadata>) {

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
            Places.initializeWithNewPlacesApiEnabled(activity, apiKey)
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Error initializing Places API",
                e
            )
        }

        // Create a new PlacesClient instance
        val placesClient = Places.createClient(activity)

        // Get the individual photo metadata.
        photoMetadataList.forEach { photoMetadata ->

            // Create a FetchPhotoRequest.
            val photoRequest = FetchPhotoRequest.builder(photoMetadata)
                .setMaxWidth(400)
                .setMaxHeight(400)
                .build()

            placesClient.fetchPhoto(photoRequest)
                .addOnSuccessListener { fetchPhotoResponse ->
                    val bitmap = fetchPhotoResponse.getBitmap()
                }.addOnFailureListener { exception ->
                    // Handle error with given status code.
                    Log.e(
                        TAG,
                        "Failed to fetch photo",
                        exception
                    )
                }
        }
    }
}
