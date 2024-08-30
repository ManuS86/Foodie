package com.example.foodie.ui.restaurants

import android.os.Bundle
import android.util.Log
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
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.Direction

class RestaurantsFragment : Fragment() {
    private val locationViewModel: LocationViewModel by activityViewModels()
    private val nearbyRestaurantsViewModel: NearbyRestaurantsViewModel by activityViewModels()
    private lateinit var binding: FragmentRestaurantsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        locationViewModel.isGPSEnabled()
        locationViewModel.checkLocationPermission()

        addLocationPermissionObserver()
        addCurrentLocationObserverWithGetNearbyRestaurants()

        binding = FragmentRestaurantsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvRestaurantsStack.layoutManager

        binding.fabDismiss.setOnClickListener {
            binding.rvRestaurantsStack.swipe()
        }

        binding.fabUndo
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

    private fun addCurrentLocationObserverWithGetNearbyRestaurants() {
        locationViewModel.currentLocation.observe(viewLifecycleOwner) { location ->
            if (location != null) {
                val currentPosition = LatLng(location.latitude, location.longitude)

                nearbyRestaurantsViewModel.getNearbyRestaurants(
                    currentPosition,
                    500.0,
                    listOf(),
                    "de"
                )
            } else {
                Log.e(
                    "RestaurantsFragment",
                    "Failed to obtain location."
                )
            }
            addNearbyRestaurantObserverWithAdapter()
        }
    }

    private fun addNearbyRestaurantObserverWithAdapter() {
        nearbyRestaurantsViewModel.nearbyRestaurants.observe(viewLifecycleOwner) { nearbyRestaurants ->
            binding.rvRestaurantsStack.layoutManager = CardStackLayoutManager(requireContext(), CardStackListener.DEFAULT)
            binding.rvRestaurantsStack.adapter =
                RestaurantsAdapter(nearbyRestaurants, nearbyRestaurantsViewModel, requireContext())
            binding.rvRestaurantsStack.setHasFixedSize(true)
        }
    }

    private fun addLocationPermissionObserver() {
        locationViewModel.locationPermission.observe(viewLifecycleOwner) { granted ->
            if (granted == true) {
                // Permissions granted, start location tracking
                addGPSObserver()
            } else {
                findNavController().navigate(R.id.locationPermissionFragment)
            }
        }
    }

    private fun addGPSObserver() {
        locationViewModel.gpsProvider.observe(viewLifecycleOwner) { enabled ->
            if (enabled) {
                // GPS enabled
            } else {
                findNavController().navigate(R.id.noGpsFragment)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        locationViewModel.isGPSEnabled()
    }
}