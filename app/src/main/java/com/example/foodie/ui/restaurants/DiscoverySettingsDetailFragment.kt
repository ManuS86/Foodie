package com.example.foodie.ui.restaurants

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.foodie.LocationViewModel
import com.example.foodie.R
import com.example.foodie.adapter.FoodFilterAdapter
import com.example.foodie.adapter.RestaurantsAdapter.ItemViewHolder
import com.example.foodie.data.Repository
import com.example.foodie.data.minRating
import com.example.foodie.data.openNow
import com.example.foodie.databinding.FragmentDiscoverySettingsDetailBinding
import com.google.android.material.chip.Chip
import com.google.android.material.shape.ShapeAppearanceModel

class DiscoverySettingsDetailFragment : Fragment() {
    private val foodCategories = Repository().foodCategories
    private val locationViewModel: LocationViewModel by activityViewModels()
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

        binding.tglBtnOpenAny.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btn_openNow -> {
                        binding.tvStatusIndicatorDiscovery.text = "Open now"
                        openNow = true
                    }

                    R.id.btn_any -> {
                        binding.tvStatusIndicatorDiscovery.text = "Any"
                        openNow = false
                    }
                }
            }
        }

        binding.rvFoodCategories.adapter = FoodFilterAdapter(foodCategories)

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
                        ratingBar.rating = 1f
                        minRating = 1
//                        }
                    }

                    2 -> {
                        ratingBar.rating = 2f
                        minRating = 2
                    }

                    3 -> {
                        ratingBar.rating = 3f
                        minRating = 3
                    }

                    4 -> {
                        ratingBar.rating = 4f
                        minRating = 4
                    }

                    5 -> {
                        ratingBar.rating = 5f
                        minRating = 5
                    }
                }
            }
        }

        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun addChip(
        category: String,
        holder: ItemViewHolder
    ) {
        val chipGroup = holder.binding.cpgCategoriesRestaurant
        val chip = Chip(context)
        chip.text = category
        chip.setChipBackgroundColorResource(R.drawable.selector_chip_color)
        chip.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_LabelLarge)
//        chip.setTextColor(context.resources.getDrawable(R.drawable.selector_chip_text, null))
        chip.shapeAppearanceModel = ShapeAppearanceModel().withCornerSize(160f)
        chipGroup.addView(chip)
    }
}