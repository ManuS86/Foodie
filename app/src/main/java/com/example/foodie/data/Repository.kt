package com.example.foodie.data

import com.example.foodie.data.model.Category

class Repository {
    val foodCategories = loadFoodCategories()

    private fun loadFoodCategories(): List<Category> {
        return foodCategoryData
    }
}