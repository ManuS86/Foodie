package com.example.foodie.data.model

data class DiscoverySettings(
    var radius: Double = 3000.0,
    var openNow: Boolean = true,
    var minRating: Float = 0f,
    var priceLevels: MutableList<Int> = mutableListOf(),
    var placeTypes: MutableList<String> = mutableListOf(),
)
