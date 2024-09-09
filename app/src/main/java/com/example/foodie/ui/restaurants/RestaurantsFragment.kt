package com.example.foodie.ui.restaurants

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.foodie.LocationViewModel
import com.example.foodie.PlacesViewModel
import com.example.foodie.R
import com.example.foodie.UserViewModel
import com.example.foodie.adapter.RestaurantsAdapter
import com.example.foodie.data.model.DiscoverySettings
import com.example.foodie.data.model.Id
import com.example.foodie.databinding.FragmentRestaurantsBinding
import com.google.android.gms.maps.model.LatLng
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.Direction
import com.yuyakaido.android.cardstackview.Duration
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting

class RestaurantsFragment : Fragment() {
    private val locationViewModel: LocationViewModel by activityViewModels()
    private val placesViewModel: PlacesViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var binding: FragmentRestaurantsBinding
    private lateinit var cardStackLayoutManager: CardStackLayoutManager
    private lateinit var discoverySettings: DiscoverySettings

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRestaurantsBinding.inflate(inflater)
        discoverySettings = userViewModel.currentDiscoverySettings.value ?: DiscoverySettings()

        locationViewModel.isGPSEnabled()
        locationViewModel.checkLocationPermission()

        addCurrentUserObserver()
        addLocationPermissionObserver()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val restaurants = placesViewModel.nearbyRestaurants.value
        cardStackLayoutManager = CardStackLayoutManager(
            requireContext(),
            object : CardStackListener {
                override fun onCardDragging(direction: Direction?, ratio: Float) {
                }

                override fun onCardSwiped(direction: Direction?) {
                    val position = cardStackLayoutManager.topPosition - 1
                    if (restaurants != null) {
                        when (direction) {
                            Direction.Left -> {
                                userViewModel.saveRestaurant(
                                    "nopes",
                                    restaurants[position].name!!,
                                    Id(
                                        restaurants[position].name!!,
                                        restaurants[position].id!!
                                    )
                                )
                            }

                            Direction.Right -> {
                                userViewModel.saveRestaurant(
                                    "likes",
                                    restaurants[position].name!!,
                                    Id(
                                        restaurants[position].name!!,
                                        restaurants[position].id!!
                                    )
                                )
                            }

                            else -> {
                                // Handle other directions if needed
                            }
                        }
                    }
                }

                override fun onCardRewound() {
                }

                override fun onCardCanceled() {
                }

                override fun onCardAppeared(view: View?, position: Int) {
                }

                override fun onCardDisappeared(view: View?, position: Int) {
                }
            }
        )

        addCurrentLocationObserverWithNearbyRestaurantsObserver()

        binding.fabNope.setOnClickListener {
            val setting = SwipeAnimationSetting.Builder()
                .setDirection(Direction.Left)
                .setDuration(Duration.Normal.duration)
                .setInterpolator(AccelerateInterpolator())
                .build()
            cardStackLayoutManager.setSwipeAnimationSetting(setting)
            binding.rvRestaurantsStack.swipe()
        }

        binding.fabUndo.setOnClickListener {
            binding.rvRestaurantsStack.rewind()
        }

        binding.fabLike.setOnClickListener {
            val setting = SwipeAnimationSetting.Builder()
                .setDirection(Direction.Right)
                .setDuration(Duration.Normal.duration)
                .setInterpolator(AccelerateInterpolator())
                .build()
            cardStackLayoutManager.setSwipeAnimationSetting(setting)
            binding.rvRestaurantsStack.swipe()
        }

        binding.ivDiscoverySettings.setOnClickListener {
            findNavController().navigate(R.id.discoveryDetailFragment)
        }
    }

    private fun addCurrentLocationObserverWithNearbyRestaurantsObserver() {
        locationViewModel.currentLocation.observe(viewLifecycleOwner) { location ->
            if (location != null) {
                val matchingCategories =
                    userViewModel.firestoreRepository.foodCategories.filter { category ->
                        discoverySettings.placeTypes.any { categoryString -> category.name == categoryString }
                    }.map { it.type }
                val currentPosition = LatLng(location.latitude, location.longitude)

                placesViewModel.loadNearbyRestaurants(
                    currentPosition,
                    matchingCategories,
                    discoverySettings
                )
            } else {
                Log.e("RestaurantsFragment", "Failed to obtain location.")
            }
            addNearbyRestaurantObserverWithAdapter()
        }
    }

    private fun addNearbyRestaurantObserverWithAdapter() {
        placesViewModel.nearbyRestaurants.observe(viewLifecycleOwner) { nearbyRestaurants ->
            binding.rvRestaurantsStack.let { cardStackView ->
                cardStackView.adapter =
                    RestaurantsAdapter(
                        requireContext(),
                        nearbyRestaurants,
                        viewLifecycleOwner,
                        locationViewModel,
                        placesViewModel,
                        userViewModel
                    )
                cardStackView.setHasFixedSize(true)
                cardStackView.layoutManager = cardStackLayoutManager
                cardStackLayoutManager.setMaxDegree(30.0f)
                cardStackLayoutManager.setDirections(Direction.HORIZONTAL)
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