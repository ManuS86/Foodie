package com.example.foodie.data

import android.graphics.Bitmap
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.PhotoMetadata
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class PlacesRepository {
    private val placeFields = listOf(
        Place.Field.ID,
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

    suspend fun fetchNearbyRestaurants(
        center: LatLng,
        radius: Double,
        categories: List<String>,
        placesClient: PlacesClient
    ): List<Place> {
        val circle = CircularBounds.newInstance(center, radius)

        val searchNearbyRequest = SearchNearbyRequest.builder(circle, placeFields)
            .setIncludedPrimaryTypes(listOf("restaurant"))
            .setIncludedTypes(categories)
            .setRankPreference(SearchNearbyRequest.RankPreference.POPULARITY)
            .setRegionCode("de")
            .build()

        val response = withContext(Dispatchers.IO) {
            placesClient.searchNearby(searchNearbyRequest).await()
        }
        return response.places
    }

    suspend fun fetchRestaurantByIdAsFlow(
        placeId: String,
        placesClient: PlacesClient
    ): Flow<Place?> {
        return flow {
            val placeDetailsRequest = FetchPlaceRequest.newInstance(placeId, placeFields)
            val response = withContext(Dispatchers.IO) {
                placesClient.fetchPlace(placeDetailsRequest).await()
            }
            emit(response.place)
        }
    }

    suspend fun fetchPhotoAsFlow(
        photoMetadata: PhotoMetadata,
        placesClient: PlacesClient
    ): Flow<Bitmap?> {
        return flow {
            val photoRequest = FetchPhotoRequest.builder(photoMetadata)
                .setMaxWidth(1000)
                .setMaxHeight(1000)
                .build()

            val response = withContext(Dispatchers.IO) {
                placesClient.fetchPhoto(photoRequest).await()
            }
            emit(response.bitmap)
        }
    }
}

