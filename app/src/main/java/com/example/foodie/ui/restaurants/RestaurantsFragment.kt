package com.example.foodie.ui.restaurants

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.foodie.LocationViewModel
import com.example.foodie.NearbyRestaurantsViewModel
import com.example.foodie.R
import com.example.foodie.adapter.RestaurantsAdapter
import com.example.foodie.databinding.FragmentRestaurantsBinding
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.net.SearchNearbyRequest

class RestaurantsFragment : Fragment() {
    private val locationViewModel: LocationViewModel by activityViewModels()
    private val nearbyRestaurantsViewModel: NearbyRestaurantsViewModel by activityViewModels()
    private lateinit var binding: FragmentRestaurantsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val location = locationViewModel.currentLocation

        if (location.value != null) {
            val currentPosition = LatLng(location.value!!.latitude, location.value!!.longitude)

            nearbyRestaurantsViewModel.getNearbyRestaurants(
                requireActivity(),
                currentPosition,
                500.0,
                listOf(),
                SearchNearbyRequest.RankPreference.POPULARITY,
                "de"
            )
        }

        locationViewModel.isGPSEnabled(requireContext())
        locationViewModel.checkLocationPermission(requireContext())

        binding = FragmentRestaurantsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nearbyRestaurantsViewModel.nearbyRestaurants.observe(viewLifecycleOwner) { nearbyRestaurants ->
            binding.rvRestaurantsStack.adapter =
                RestaurantsAdapter(nearbyRestaurants)
            binding.rvRestaurantsStack.setHasFixedSize(true)
        }

        locationViewModel.locationPermission.observe(viewLifecycleOwner) { granted ->
            if (granted == true) {
                // Permissions granted, start location tracking
                locationViewModel.gpsProvider.observe(viewLifecycleOwner) { enabled ->
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

        binding.fabLike.setOnClickListener {
            findNavController().navigate(R.id.navigationDetailFragment)
        }

        binding.ivDiscoverySettings.setOnClickListener {
            findNavController().navigate(R.id.discoveryDetailFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        locationViewModel.isGPSEnabled(requireContext())
    }
}