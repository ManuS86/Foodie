package com.example.foodie

import android.app.Application
import android.graphics.Bitmap
import android.icu.util.Calendar
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.foodie.data.PlacesRepository
import com.example.foodie.data.model.DiscoverySettings
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.PhotoMetadata
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class PlacesViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "NearbyRestaurantsViewModel"
    private val applicationContext = application
    private val placesRepository = PlacesRepository()
    private lateinit var placesClient: PlacesClient

    private var _nearbyRestaurants = MutableLiveData<List<Place>>()
    val nearbyRestaurants: LiveData<List<Place>>
        get() = _nearbyRestaurants

    private var _currentRestaurant = MutableLiveData<Place>()
    val currentRestaurant: LiveData<Place>
        get() = _currentRestaurant

    private var _likes = MutableLiveData<List<Place>>()
    val likes: LiveData<List<Place>>
        get() = _likes

    private var _nopes = MutableLiveData<List<Place>>()
    val nopes: LiveData<List<Place>>
        get() = _nopes

    private var _history = MutableLiveData<List<Place>>()
    val history: LiveData<List<Place>>
        get() = _history

    private var _photos = MutableStateFlow<List<Bitmap>>(emptyList())
    val photos: LiveData<List<Bitmap>>
        get() = _photos.asLiveData()

    fun setCurrentRestaurant(position: Int) {
        _currentRestaurant.postValue(nearbyRestaurants.value?.get(position))
    }

    init {
        createPlacesClient()
    }

    private fun createPlacesClient() {
        val apiKey = BuildConfig.PLACES_API_KEY

        if (apiKey.isEmpty() || apiKey == "DEFAULT_API_KEY") {
            Log.e("Places test", "No api key")
            return
        }

        try {
            Places.initializeWithNewPlacesApiEnabled(applicationContext, apiKey)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Places API", e)
        }

        val placesClientNew = Places.createClient(applicationContext)
        placesClient = placesClientNew
    }

    fun loadNearbyRestaurants(
        center: LatLng,
        categories: List<String>,
        discoverySettings: DiscoverySettings
    ) {
        viewModelScope.launch {
            try {
                val restaurants = placesRepository.fetchNearbyRestaurants(
                    center,
                    discoverySettings.radius,
                    categories,
                    placesClient
                )

                if (discoverySettings.openNow) {
                    _nearbyRestaurants.value = restaurants.filter { isPlaceOpenNow(it) == true }
                } else {
                    _nearbyRestaurants.value = restaurants
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to retrieve nearby places", e)
            }
        }
    }

    fun loadRestaurantById(collection: String, placeIdList: List<String>) {
        viewModelScope.launch {
            try {
                val restaurants = placesRepository.fetchRestaurantsById(placeIdList, placesClient)
                when (collection) {
                    "history" -> _history.value = restaurants
                    "likes" -> _likes.value = restaurants
                    "nopes" -> _nopes.value = restaurants
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch place", e)
            }
        }
    }

    fun loadPhoto(photoMetadata: PhotoMetadata) {
        viewModelScope.launch {
            try {
                placesRepository.fetchPhotoAsFlow(photoMetadata, placesClient)
                    .collect { photo ->
                        if (photo != null) {
                            _photos.value += photo
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch photo", e)
            }
        }
    }

    fun resetPhotosLiveData() {
        _photos.value = emptyList()
    }

    fun isPlaceOpenNow(place: Place): Boolean? {
        val openingHours = place.currentOpeningHours
            ?: return null // No opening hours information available

        val currentTime = Calendar.getInstance()

        for (period in openingHours.periods) {
            val openTime = period?.open
            val closeTime = period?.close

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

//    fun getPhoto(photoMetadata: PhotoMetadata): MutableLiveData<Bitmap?> {
//        val photoRequest = FetchPhotoRequest.builder(photoMetadata)
//            .setMaxWidth(1000)
//            .setMaxHeight(1000)
//            .build()
//
//        val liveData = MutableLiveData<Bitmap?>()
//
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                val response = placesClient.fetchPhoto(photoRequest).await()
//                liveData.postValue(response.bitmap)
//            } catch (e: Exception) {
//                Log.e(TAG, "Failed to fetch photo", e)
//                liveData.postValue(null) // Or handle the error differently
//            }
//        }
//        return liveData
//    }

//    fun filterList(list: List<Place>, conditions: List<Condition>): List<Place> {
//        val filteredList = mutableListOf<Place>()
//        for (element in list) {
//            var meetsAllConditions = true
//            for (condition in conditions) {
//                if (!condition.apply(element)) {
//                    meetsAllConditions = false
//                    break
//                }
//            }
//            if (meetsAllConditions) {
//                filteredList.add(element)
//            }
//        }
//        return filteredList
//    }


//                val restaurants = response.places.map {
//                    val periods = mutableListOf<CustomPeriod>()
//
//                    it.currentOpeningHours?.periods?.forEach { period ->
//                        periods.add(
//                            CustomPeriod(
//                                PeriodPart(
//                                    Date(
//                                        period.close?.date?.day,
//                                        period.close?.date?.month,
//                                        period.close?.date?.year
//                                    ),
//                                    period.close?.day?.name,
//                                    Time(
//                                        period.close?.time?.hours,
//                                        period.close?.time?.minutes
//                                    )
//                                ),
//                                PeriodPart(
//                                    Date(
//                                        period.open?.date?.day,
//                                        period.open?.date?.month,
//                                        period.open?.date?.year
//                                    ),
//                                    period.open?.day?.name,
//                                    Time(
//                                        period.open?.time?.hours,
//                                        period.open?.time?.minutes
//                                    )
//                                )
//                            )
//                        )
//                    }
//
//                    val photoMetadatas = mutableListOf<CustomPhotoMetadata>()
//
//                    it.photoMetadatas?.forEach { photoMetadata ->
//                        photoMetadatas.add(
//                            CustomPhotoMetadata(
//                                photoMetadata.attributions,
//                                photoMetadata.height,
//                                photoMetadata.width
//                            )
//                        )
//                    }
//
//                    Restaurant(
//                        it.address,
//                        CustomOpeningHours(
//                            periods,
//                            it.currentOpeningHours?.weekdayText
//                        ),
//                        it.name,
//                        com.example.foodie.data.model.CustomLatLng(
//                            it.latLng?.latitude,
//                            it.latLng?.longitude
//                        ),
//                        photoMetadatas,
//                        it.placeTypes,
//                        it.priceLevel,
//                        it.rating,
//                        it.userRatingsTotal,
//                        it.websiteUri?.toString()
//                    )
//                }
}
