package com.example.foodie.ui.restaurants

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.foodie.R
import com.example.foodie.addSelectorChip
import com.example.foodie.data.Repository
import com.example.foodie.data.discoverySettings
import com.example.foodie.databinding.FragmentDiscoverySettingsDetailBinding

class DiscoverySettingsDetailFragment : Fragment() {
    private val foodCategories = Repository().foodCategories
    private lateinit var binding: FragmentDiscoverySettingsDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDiscoverySettingsDetailBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val chipGroup = binding.cpgFoodCategories

        binding.tglBtnOpenAny.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btn_openNow -> {
                        discoverySettings.openNow = true
                        binding.tvStatusIndicatorDiscovery.text = "Open now"
                    }

                    R.id.btn_any -> {
                        discoverySettings.openNow = false
                        binding.tvStatusIndicatorDiscovery.text = "Any"
                    }
                }
            }
        }
        foodCategories.forEach { category ->
            addSelectorChip(category.name, chipGroup, requireContext())
        }
        binding.rbRatingDiscovery.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            if (fromUser) {
                // User clicked on a star
                val clickedStarIndex = rating.toInt() // Adjust index as needed
                // Perform actions based on the clicked star index
                when (clickedStarIndex) {
                    1 -> {

//                        if (ratingBar.rating == 1f) {
//                            ratingBar.rating = 0f
//                        } else {
                        discoverySettings.minRating = 1f
                        ratingBar.rating = discoverySettings.minRating
//                        }
                    }

                    2 -> {
                        discoverySettings.minRating = 2f
                        ratingBar.rating = discoverySettings.minRating
                    }

                    3 -> {
                        discoverySettings.minRating = 3f
                        ratingBar.rating = discoverySettings.minRating
                    }

                    4 -> {
                        discoverySettings.minRating = 4f
                        ratingBar.rating = discoverySettings.minRating
                    }

                    5 -> {
                        discoverySettings.minRating = 5f
                        ratingBar.rating = discoverySettings.minRating
                    }
                }
            }
        }

        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}