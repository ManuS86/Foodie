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

class RestaurantsFragment : Fragment() {
    private val locationViewModel: LocationViewModel by activityViewModels()
    private val nearbyRestaurantsViewModel: NearbyRestaurantsViewModel by activityViewModels()
    private lateinit var binding: FragmentRestaurantsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        locationViewModel.isGPSEnabled(requireContext())
        locationViewModel.checkLocationPermission(requireContext())

        binding = FragmentRestaurantsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val location = locationViewModel.currentLocation

//        if (location.value != null) {
//            val currentPosition = LatLng(location.value!!.latitude, location.value!!.longitude)
//            Log.d(
//                "RestaurantsFragment",
//                "Current location: ${location.value!!.latitude}, ${location.value!!.longitude}"
//            )
//        nearbyRestaurantsViewModel.getNearbyRestaurants(
//            requireActivity(),
//            LatLng(40.7580, -73.9855),
//            500.0,
//            listOf(),
//            SearchNearbyRequest.RankPreference.POPULARITY,
//            "de"
//        )
//        } else {
//            Log.e(
//                "RestaurantsFragment",
//                "Failed to obtain location."
//            )
//        }

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
                        locationViewModel.requestLocationUpdates(requireActivity())
                    } else {
                        findNavController().navigate(R.id.noGpsFragment)
                    }
                }
            } else {
                findNavController().navigate(R.id.locationPermissionFragment)
            }
        }

        binding.fabDismiss.setOnClickListener {

        }

        binding.fabUndo.setOnClickListener {
            binding.rvRestaurantsStack.rewind()
        }

        binding.fabLike.setOnClickListener {
            binding.rvRestaurantsStack.swipe()
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