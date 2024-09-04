package com.example.foodie

import android.content.Context
import android.view.LayoutInflater
import com.example.foodie.data.model.DiscoverySettings
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

fun addIndicatorChip(
    category: String,
    chipGroup: ChipGroup,
    context: Context,
    discoverySettings: DiscoverySettings
) {
    val chipLayout =
        LayoutInflater.from(context).inflate(R.layout.item_indicator_chip, chipGroup, false)
    val chip = chipLayout.findViewById<Chip>(R.id.cp_indicator)
    chip.text = category
    if (discoverySettings.placeTypes.contains(category)) {
        chip.isChecked = true
    }
    chipGroup.addView(chip)
}

fun addSelectorChip(
    category: String,
    chipGroup: ChipGroup,
    context: Context,
    discoverySettings: DiscoverySettings
) {
    val chipLayout =
        LayoutInflater.from(context).inflate(R.layout.item_category_chip, chipGroup, false)
    val chip = chipLayout.findViewById<Chip>(R.id.cp_category)
    chip.text = category
    if (discoverySettings.placeTypes.contains(category)) {
        chip.isChecked = true
    }
    chip.setOnCheckedChangeListener { _, isChecked ->
        if (isChecked) {
            discoverySettings.placeTypes.add(chip.text as String)
        } else {
            discoverySettings.placeTypes.remove(chip.text)
        }
    }
    chipGroup.addView(chip)
}