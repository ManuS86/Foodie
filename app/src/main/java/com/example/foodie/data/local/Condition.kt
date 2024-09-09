package com.example.foodie.data.local

import com.google.android.libraries.places.api.model.Place

interface Condition<T> {
    fun apply(element: T): Boolean
}

class ratingCondition(private val minRating: Int) : Condition<Place> {
    override fun apply(element: Place): Boolean {
        return element.rating!! >= minRating
    }
}

//class priceLvlCondition(private val priceLvl: String) : Condition<Place> {
//    override fun apply(element: Place): Boolean {
//        return element.priceLevel == priceLvl
//    }
//}

class categoryCondition(private val startsWith: String) : Condition<Place> {
    override fun apply(element: Place): Boolean {
        return element.name.startsWith(startsWith)
    }
}