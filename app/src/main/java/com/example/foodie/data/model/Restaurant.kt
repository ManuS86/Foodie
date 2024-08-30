package com.example.foodie.data.model

import android.graphics.Bitmap
import android.net.Uri
import com.google.android.gms.maps.model.LatLng

data class Restaurant(
    val name: String,
    val address: String,
    val placeTypes: List<String>,
    val location: LatLng,
    val formatedOpeningHours: String,
    val rating: String,
    val userRatingsTotal: String,
    val priceLevel: String,
    val websiteUri: Uri,
    val photos: List<Bitmap>
)
