package com.example.foodie.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.foodie.R
import com.example.foodie.data.model.AppSettings
import com.example.foodie.data.model.Category
import com.example.foodie.data.model.DiscoverySettings
import com.example.foodie.databinding.FragmentDiscoverySettingsDetailBinding
import com.example.foodie.ui.viewmodels.UserViewModel
import com.example.foodie.utils.addSelectorChip

class DiscoverySettingsDetailFragment : Fragment() {
    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var appSettings: AppSettings
    private lateinit var binding: FragmentDiscoverySettingsDetailBinding
    private lateinit var discoverySettings: DiscoverySettings
    private lateinit var foodCategories: List<Category>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        appSettings = userViewModel.currentAppSettings.value ?: AppSettings()
        binding = FragmentDiscoverySettingsDetailBinding.inflate(inflater)
        discoverySettings = userViewModel.currentDiscoverySettings.value ?: DiscoverySettings()
        foodCategories = userViewModel.firestoreRepository.foodCategories

        setSettingsUI()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val chipGroup = binding.cpgFoodCategories

        binding.sbDistanceSlider.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?, progress:
                Int, fromUser: Boolean
            ) {
                if (appSettings.distanceUnit == "Km") {
                    binding.tvKmMi.text = "${progress} Km"
                    discoverySettings.radius = (progress * 1000).toDouble()
                    userViewModel.saveDiscoverySettings()
                } else if (appSettings.distanceUnit == "Mi") {
                    binding.tvKmMi.text = "${progress} Mi"
                    discoverySettings.radius = (progress * 1609.34)
                    userViewModel.saveDiscoverySettings()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        binding.tglBtnOpenAny.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btn_openNow -> {
                        discoverySettings.openNow = true
                        binding.tvOpenStatus.text = "Open now"
                        userViewModel.saveDiscoverySettings()
                    }

                    R.id.btn_any -> {
                        discoverySettings.openNow = false
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
                discoverySettings,
                userViewModel
            )
        }
        binding.rbRatingFilter.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            if (fromUser) {
                // User clicked on a star
                val clickedStarIndex = rating.toInt() // Adjust index as needed
                // Perform actions based on the clicked star index
                when (clickedStarIndex) {
                    1 -> {
                        if (ratingBar.rating == 1f) {
                            ratingBar.rating = 0f
                            ratingBar.rating = discoverySettings.minRating
                            userViewModel.saveDiscoverySettings()
                        } else {
                            discoverySettings.minRating = 1f
                            ratingBar.rating = discoverySettings.minRating
                            userViewModel.saveDiscoverySettings()
                        }
                    }

                    2 -> {
                        discoverySettings.minRating = 2f
                        ratingBar.rating = discoverySettings.minRating
                        userViewModel.saveDiscoverySettings()
                    }

                    3 -> {
                        discoverySettings.minRating = 3f
                        ratingBar.rating = discoverySettings.minRating
                        userViewModel.saveDiscoverySettings()
                    }

                    4 -> {
                        discoverySettings.minRating = 4f
                        ratingBar.rating = discoverySettings.minRating
                        userViewModel.saveDiscoverySettings()
                    }

                    5 -> {
                        discoverySettings.minRating = 5f
                        ratingBar.rating = discoverySettings.minRating
                        userViewModel.saveDiscoverySettings()
                    }
                }
            }
        }

        binding.cpPriceRangeLow.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                discoverySettings.priceLevels.add(1)
            } else {
                discoverySettings.priceLevels.remove(1)
            }
            userViewModel.saveDiscoverySettings()
        }

        binding.cpPriceRangeMed.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                discoverySettings.priceLevels.add(2)
            } else {
                discoverySettings.priceLevels.remove(2)
            }
            userViewModel.saveDiscoverySettings()
        }

        binding.cpPriceRangeHi.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                discoverySettings.priceLevels.add(3)
            } else {
                discoverySettings.priceLevels.remove(3)
            }
            userViewModel.saveDiscoverySettings()
        }

        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setSettingsUI() {
        val radius = userViewModel.currentDiscoverySettings.value?.radius
        if (appSettings.distanceUnit == "Km") {
            binding.sbDistanceSlider.progress = (radius?.div(1000))?.toInt() ?: 2000
            binding.tvKmMi.text = "${(radius?.div(1000)?.toInt())} Km"
        } else if (appSettings.distanceUnit == "Mi") {
            binding.sbDistanceSlider.progress = (radius?.div(621.371))?.toInt() ?: 1000
            binding.tvKmMi.text = "${(radius?.div(1609.34))?.toInt()} Mi"
        }

        val openNow = discoverySettings.openNow
        if (openNow) {
            binding.btnOpenNow.isChecked = true
            binding.tvOpenStatus.text = "Open now"
        } else {
            binding.btnAny.isChecked = true
            binding.tvOpenStatus.text = "Any"
        }

        val rating = discoverySettings.minRating
        val ratingBar = binding.rbRatingFilter
        when (rating) {
            1f -> ratingBar.rating = 1f

            2f -> ratingBar.rating = 2f

            3f -> ratingBar.rating = 3f

            4f -> ratingBar.rating = 4f

            5f -> ratingBar.rating = 5f
        }

        val priceLevel = discoverySettings.priceLevels
        if (priceLevel.contains(1)) {
            binding.cpPriceRangeLow.isChecked = true
        }

        if (priceLevel.contains(2)) {
            binding.cpPriceRangeMed.isChecked = true
        }

        if (priceLevel.contains(3)) {
            binding.cpPriceRangeHi.isChecked = true
        }
    }
}