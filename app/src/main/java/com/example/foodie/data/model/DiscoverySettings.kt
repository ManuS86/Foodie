package com.example.foodie.data.model

data class DiscoverySettings(
    var radius: Long,
    var openNow: Boolean,
    var minRating: Float,
    var minPriceLevel: String,
    var placeTypes: MutableList<String>,
)
