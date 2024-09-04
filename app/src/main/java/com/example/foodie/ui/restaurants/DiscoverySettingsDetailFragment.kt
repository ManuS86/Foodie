package com.example.foodie.ui.restaurants

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.foodie.R
import com.example.foodie.UserViewModel
import com.example.foodie.addSelectorChip
import com.example.foodie.data.model.Category
import com.example.foodie.data.model.DiscoverySettings
import com.example.foodie.databinding.FragmentDiscoverySettingsDetailBinding

class DiscoverySettingsDetailFragment : Fragment() {
    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var binding: FragmentDiscoverySettingsDetailBinding
    private lateinit var foodCategories: List<Category>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDiscoverySettingsDetailBinding.inflate(inflater)
        foodCategories = userViewModel.repository.foodCategories

        val openNow = userViewModel.currentDiscoverySettings.value?.openNow
        if (openNow == true) {
            binding.btnOpenNow.isChecked = true
            binding.tvOpenStatus.text = "Open now"
        } else {
            binding.btnAny.isChecked = true
            binding.tvOpenStatus.text = "Any"
        }

        val rating = userViewModel.currentDiscoverySettings.value?.minRating
        val ratingBar = binding.rbRatingDiscovery
        when (rating) {
            1f -> ratingBar.rating = 1f

            2f -> ratingBar.rating = 2f

            3f -> ratingBar.rating = 3f

            4f -> ratingBar.rating = 4f

            5f -> ratingBar.rating = 5f
        }

        val priceLevel = userViewModel.currentDiscoverySettings.value?.priceLevels
            if (priceLevel?.contains("€") == true) {
                binding.cpPriceRangeLowDiscovery.isChecked = true
            }

            if (priceLevel?.contains("€€") == true) {
                binding.cpPriceRangeMedDiscovery.isChecked = true
            }

            if (priceLevel?.contains("€€€") == true) {
                binding.cpPriceRangeHiDiscovery.isChecked = true
            }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val chipGroup = binding.cpgFoodCategories
        val discoverySettings = userViewModel.currentDiscoverySettings.value

        binding.tglBtnOpenAny.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btn_openNow -> {
                        discoverySettings?.openNow = true
                        binding.tvOpenStatus.text = "Open now"
                        userViewModel.saveDiscoverySettings()
                    }

                    R.id.btn_any -> {
                        discoverySettings?.openNow = false
                        binding.tvOpenStatus.text = "Any"
                        userViewModel.saveDiscoverySettings()
                    }
                }
            }
        }
        foodCategories.forEach { category ->
            addSelectorChip(
                category.name,
                chipGroup,
                requireContext(),
                discoverySettings ?: DiscoverySettings()
            )
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
                        discoverySettings?.minRating = 1f
                        ratingBar.rating = discoverySettings?.minRating ?: 0f
                        userViewModel.saveDiscoverySettings()
//                        }
                    }

                    2 -> {
                        discoverySettings?.minRating = 2f
                        ratingBar.rating = discoverySettings?.minRating ?: 0f
                        userViewModel.saveDiscoverySettings()
                    }

                    3 -> {
                        discoverySettings?.minRating = 3f
                        ratingBar.rating = discoverySettings?.minRating ?: 0f
                        userViewModel.saveDiscoverySettings()
                    }

                    4 -> {
                        discoverySettings?.minRating = 4f
                        ratingBar.rating = discoverySettings?.minRating ?: 0f
                        userViewModel.saveDiscoverySettings()
                    }

                    5 -> {
                        discoverySettings?.minRating = 5f
                        ratingBar.rating = discoverySettings?.minRating ?: 0f
                        userViewModel.saveDiscoverySettings()
                    }
                }
            }
        }

        binding.cpPriceRangeLowDiscovery.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                discoverySettings?.priceLevels?.add("€")
            } else {
                discoverySettings?.priceLevels?.remove("€")
            }
            userViewModel.saveDiscoverySettings()
        }

        binding.cpPriceRangeMedDiscovery.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                discoverySettings?.priceLevels?.add("€€")
            } else {
                discoverySettings?.priceLevels?.remove("€€")
            }
            userViewModel.saveDiscoverySettings()
        }

        binding.cpPriceRangeHiDiscovery.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                discoverySettings?.priceLevels?.add("€€€")
            } else {
                discoverySettings?.priceLevels?.remove("€€€")
            }
            userViewModel.saveDiscoverySettings()
        }

        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}