package com.example.foodie.data.model

import androidx.appcompat.app.AppCompatDelegate

data class AppSettings(
    var emails: Boolean = false,
    var pushNotifications: Boolean = false,
    var distanceUnit: String = "Km",
    var nightmode: Int = AppCompatDelegate.MODE_NIGHT_NO
)
