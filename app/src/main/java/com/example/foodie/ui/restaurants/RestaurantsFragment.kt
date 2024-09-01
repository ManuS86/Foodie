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
import com.example.foodie.UserViewModel
import com.example.foodie.adapter.RestaurantsAdapter
import com.example.foodie.databinding.FragmentRestaurantsBinding
import com.google.android.gms.maps.model.LatLng

class RestaurantsFragment : Fragment() {
    private val locationViewModel: LocationViewModel by activityViewModels()
    private val nearbyRestaurantsViewModel: NearbyRestaurantsViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var binding: FragmentRestaurantsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        locationViewModel.isGPSEnabled()
        locationViewModel.checkLocationPermission()

        addCurrentUserObserver()
        addLocationPermissionObserver()
        addCurrentLocationObserverWithGetNearbyRestaurants()

        binding = FragmentRestaurantsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            binding.rvRestaurantsStack.let {
                it.adapter =
                    RestaurantsAdapter(
                        nearbyRestaurants,
                        nearbyRestaurantsViewModel,
                        requireContext()
                    )
                it.setHasFixedSize(true)
//                it.layoutManager = CardStackLayoutManager(requireContext(),
//                    object : CardStackListener {
//                        override fun onCardDragging(direction: Direction?, ratio: Float) {
//                            TODO("Not yet implemented")
//                        }
//
//                        override fun onCardSwiped(direction: Direction?) {
//                            val adapter = (it.adapter as RestaurantsAdapter)
//                            val swipedCardId = adapter.getItemId(adapter.itemCount - 1)
//                            when (direction) {
//                                Direction.Left -> {
////                                    userViewModel.addNewRestaurant("liked restaurants", nearbyRestaurants[position].name, nearbyRestaurants[position])
//                                }
//
//                                Direction.Right -> {
////                                    userViewModel.addNewRestaurant("dismissed restaurants", nearbyRestaurants[position].name, nearbyRestaurants[position])
//                                }
//
//                                else -> {
//                                    // Handle other directions if needed
//                                }
//                            }
//                        }
//
//                        override fun onCardRewound() {
//                            TODO("Not yet implemented")
//                        }
//
//                        override fun onCardCanceled() {
//                            TODO("Not yet implemented")
//                        }
//
//                        override fun onCardAppeared(view: View?, position: Int) {
//                            TODO("Not yet implemented")
//                        }
//
//                        override fun onCardDisappeared(view: View?, position: Int) {
//                            TODO("Not yet implemented")
//                        }
//                    }
//                )
            }
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

    private fun addCurrentUserObserver() {
        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user == null) {
                findNavController().navigate(R.id.loginFragment)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        locationViewModel.isGPSEnabled()
    }
}