package com.example.foodie.data.model

data class DiscoverySettings(
    var radius: Long = 2000,
    var openNow: Boolean = true,
    var minRating: Float = 0f,
    var priceLevels: MutableList<String> = mutableListOf(),
    var placeTypes: MutableList<String> = mutableListOf(),
)
