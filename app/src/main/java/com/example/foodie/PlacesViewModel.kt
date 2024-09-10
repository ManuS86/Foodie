package com.example.foodie

import android.app.Application
import android.graphics.Bitmap
import android.icu.util.Calendar
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.foodie.data.PlacesRepository
import com.example.foodie.data.model.DiscoverySettings
import com.example.foodie.data.model.Id
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.PhotoMetadata
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.launch

class PlacesViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "NearbyRestaurantsViewModel"
    private val applicationContext = application
    private val placesRepository = PlacesRepository()
    private lateinit var placesClient: PlacesClient

    private var _nearbyRestaurants = MutableLiveData<MutableList<Place>>()
    val nearbyRestaurants: LiveData<MutableList<Place>>
        get() = _nearbyRestaurants

    private var _currentRestaurant = MutableLiveData<Place>()
    val currentRestaurant: LiveData<Place>
        get() = _currentRestaurant

    private var _likes = MutableLiveData<MutableList<Place>>()
    val likes: LiveData<MutableList<Place>>
        get() = _likes

    private var _nopes = MutableLiveData<MutableList<Place>>()
    val nopes: LiveData<MutableList<Place>>
        get() = _nopes

    private var _history = MutableLiveData<MutableList<Place>>()
    val history: LiveData<MutableList<Place>>
        get() = _history

    init {
        createPlacesClient()
    }

    fun setCurrentRestaurant(position: Int) {
        _currentRestaurant.postValue(nearbyRestaurants.value?.get(position))
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

                val filteredByRating =
                    restaurants.filter { it.rating!! >= discoverySettings.minRating }
                var filteredByRatingAndPriceLevel = filteredByRating

                if (discoverySettings.priceLevels.isNotEmpty()) {
                    filteredByRatingAndPriceLevel = filteredByRating.filter { filteredRestaurants ->
                        filteredRestaurants.priceLevel?.let { priceLvl ->
                            discoverySettings.priceLevels.contains(priceLvl)
                        } == true
                    }
                }

                if (discoverySettings.openNow) {
                    _nearbyRestaurants.value =
                        filteredByRatingAndPriceLevel.filter { isPlaceOpenNow(it) == true }
                            .toMutableList()
                } else {
                    _nearbyRestaurants.value = filteredByRatingAndPriceLevel.toMutableList()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to retrieve nearby places", e)
            }
        }
    }

    fun loadRestaurantById(collection: String, placeIdList: MutableList<Id>) {
        viewModelScope.launch {
            try {
                val restaurants = placesRepository.fetchRestaurantsById(placeIdList, placesClient)
                when (collection) {
                    "history" -> _history.postValue(restaurants)
                    "likes" -> _likes.postValue(restaurants)
                    "nopes" -> _nopes.postValue(restaurants)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch place", e)
            }
        }
    }

    fun addRestaurantToLiveData(liveData: String, restaurant: Place) {
        when (liveData) {
            "history" -> {
                _history.value?.add(restaurant)
                _history.value = _history.value
            }

            "likes" -> {
                _likes.value?.add(restaurant)
                _likes.value = _likes.value
            }

            "nopes" -> {
                _nopes.value?.add(restaurant)
                _nopes.value = _nopes.value
            }
        }
    }

    fun removeRestaurantFromLiveData(liveData: String, restaurant: Place) {
        when (liveData) {
            "history" -> {
                _history.value?.remove(restaurant)
                _history.value = _history.value
            }

            "likes" -> {
                _likes.value?.remove(restaurant)
                _likes.value = _likes.value
            }

            "nopes" -> {
                _nopes.value?.remove(restaurant)
                _nopes.value = _nopes.value
            }
        }
    }

    fun loadPhoto(photoMetadata: PhotoMetadata, onCompletion: (Bitmap) -> (Unit)) {
        viewModelScope.launch {
            try {
                val photo = placesRepository.fetchPhoto(photoMetadata, placesClient)

                onCompletion(photo)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch photo", e)
            }
        }
    }

    fun loadPhotoList(
        photoMetadataList: List<PhotoMetadata>,
        onCompletion: (List<Bitmap>) -> (Unit)
    ) {
        viewModelScope.launch {
            try {
                val photoList = placesRepository.fetchPhotoList(photoMetadataList, placesClient)

                onCompletion(photoList)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch photos", e)
            }
        }
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
