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
import com.example.foodie.adapter.FoodCategoryFilterAdapter
import com.example.foodie.data.Repository
import com.example.foodie.databinding.FragmentDiscoveryDetailBinding

class DiscoveryDetailFragment : Fragment() {
    private val foodCategories = Repository().foodCategories
    private val viewModel: LocationViewModel by activityViewModels()
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
                    R.id.btn_openNow -> binding.tvStatusIndicatorDiscovery.text = "Open now"
                    R.id.btn_any -> binding.tvStatusIndicatorDiscovery.text = "Any"
                }
            }
        }

        binding.rvFoodCategories.adapter = FoodCategoryFilterAdapter(foodCategories)
        binding.rvFoodCategories.setHasFixedSize(true)

        binding.rbRatingDiscovery.setOnClickListener {

        }

        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}