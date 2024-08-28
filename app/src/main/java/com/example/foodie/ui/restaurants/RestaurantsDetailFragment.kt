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
import com.example.foodie.databinding.FragmentRestaurantsDetailBinding

class RestaurantsDetailFragment : Fragment() {
    private val viewModel: LocationViewModel by activityViewModels()
    private lateinit var binding: FragmentRestaurantsDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.isGPSEnabled(requireContext())
        viewModel.checkLocationPermission(requireContext())
        binding = FragmentRestaurantsDetailBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.locationPermission.observe(viewLifecycleOwner) { granted ->
            if (granted == true) {
                // Permissions granted, start location tracking
                viewModel.gpsProvider.observe(viewLifecycleOwner) { enabled ->
                    if (enabled) {
                        // GPS enabled
                    } else {
                        findNavController().navigate(R.id.noGpsFragment)
                    }
                }
            } else {
                findNavController().navigate(R.id.locationPermissionFragment)
            }
        }

        binding.ivDecollapse.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.isGPSEnabled(requireContext())
    }
}