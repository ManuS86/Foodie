package com.example.foodie.data

import android.graphics.Bitmap
import com.example.foodie.data.model.Id
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.PhotoMetadata
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import kotlinx.coroutines.tasks.await

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

        val response = placesClient.searchNearby(searchNearbyRequest).await()

        return response.places
    }

    suspend fun fetchRestaurantsById(
        placeIdList: List<Id>,
        placesClient: PlacesClient
    ): MutableList<Place> {
        val places = mutableListOf<Place>()
        placeIdList.forEach { placeId ->
            val placeDetailsRequest =
                placeId.id?.let { FetchPlaceRequest.newInstance(it, placeFields) }
            val response = placeDetailsRequest?.let { placesClient.fetchPlace(it).await() }

            if (response != null) {
                places.add(response.place)
            }
        }
        return places
    }

    suspend fun fetchPhoto(
        photoMetadata: PhotoMetadata,
        placesClient: PlacesClient
    ): Bitmap {
        val photoRequest = FetchPhotoRequest.builder(photoMetadata)
            .setMaxWidth(1000)
            .setMaxHeight(1000)
            .build()

        val response = placesClient.fetchPhoto(photoRequest).await()

        return response.bitmap
    }

    suspend fun fetchPhotoList(
        photoMetadataList: List<PhotoMetadata>,
        placesClient: PlacesClient
    ): List<Bitmap> {
        val photos = mutableListOf<Bitmap>()
        photoMetadataList.forEach { photoMetadata ->
            val photoRequest = FetchPhotoRequest.builder(photoMetadata)
                .setMaxWidth(1000)
                .setMaxHeight(1000)
                .build()

            val response = placesClient.fetchPhoto(photoRequest).await()
            photos.add(response.bitmap)
        }
        return photos
    }
}

