package com.example.foodie.data.model

data class Id(
    val name: String?,
    val id: String?,
    val date: String?
) {
    constructor() : this(null, null, null)
}
