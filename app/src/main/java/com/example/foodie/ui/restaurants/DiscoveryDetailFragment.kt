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
import com.example.foodie.data.Repository
import com.example.foodie.data.minRating
import com.example.foodie.data.openNow
import com.example.foodie.databinding.FragmentDiscoveryDetailBinding

class DiscoveryDetailFragment : Fragment() {
    private val foodCategories = Repository().foodCategories
    private val locationViewModel: LocationViewModel by activityViewModels()
    private lateinit var binding: FragmentDiscoveryDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDiscoveryDetailBinding.inflate(inflater)
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
        binding.rvFoodCategories.setHasFixedSize(true)

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
}