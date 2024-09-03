package com.example.foodie.data

import androidx.appcompat.app.AppCompatDelegate
import com.example.foodie.data.model.AppSettings
import com.example.foodie.data.model.DiscoverySettings

val appSettings =
    AppSettings(
        false,
        false,
        "Km",
        AppCompatDelegate.MODE_NIGHT_NO
    )

val discoverySettings =
    DiscoverySettings(
        1000,
        true,
        0f,
        "",
        mutableListOf()
    )