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
import com.example.foodie.R
import com.example.foodie.adapter.RestaurantsAdapter
import com.example.foodie.data.model.DiscoverySettings
import com.example.foodie.data.model.Id
import com.example.foodie.databinding.FragmentRestaurantsBinding
import com.example.foodie.ui.viewmodels.LocationViewModel
import com.example.foodie.ui.viewmodels.PlacesViewModel
import com.example.foodie.ui.viewmodels.UserViewModel
import com.example.foodie.utils.ListType
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
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
    private lateinit var filteredRestaurants: MutableList<Place>
    private lateinit var restaurantsAdapter: RestaurantsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRestaurantsBinding.inflate(inflater)
        discoverySettings = userViewModel.currentDiscoverySettings.value ?: DiscoverySettings()
        restaurantsAdapter = RestaurantsAdapter(
            requireContext(),
            mutableListOf(),
            locationViewModel,
            placesViewModel,
            userViewModel
        )

        locationViewModel.isGPSEnabled()
        locationViewModel.checkLocationPermission()

        if (userViewModel.historyIds.value != null) {
            placesViewModel.loadRestaurantById("history", userViewModel.historyIds.value!!)
        }

        if (userViewModel.likesIds.value != null) {
            placesViewModel.loadRestaurantById("likes", userViewModel.likesIds.value!!)
        }

        if (userViewModel.nopesIds.value != null) {
            placesViewModel.loadRestaurantById("nopes", userViewModel.nopesIds.value!!)
        }

        addCurrentUserObserver()
        addLocationPermissionObserver()
        addCurrentLocationObserver()
        addNearbyRestaurantObserver()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cardStackLayoutManager =
            CardStackLayoutManager(requireContext(), object : CardStackListener {
                override fun onCardDragging(direction: Direction?, ratio: Float) {
                }

                override fun onCardSwiped(direction: Direction?) {
                    val position = cardStackLayoutManager.topPosition - 1
                    val restaurantId = Id(
                        filteredRestaurants[position].name!!,
                        filteredRestaurants[position].id!!,
                        null
                    )

                    when (direction) {
                        Direction.Right -> {
                            placesViewModel.likes.value?.let {
                                if (!it.contains(filteredRestaurants[position])) {
                                    placesViewModel.addRestaurantToLiveData(
                                        ListType.LIKES,
                                        filteredRestaurants[position]
                                    )
                                }
                            }
                            userViewModel.likesIds.value?.let {
                                if (!it.contains(restaurantId)) {
                                    userViewModel.likesIds.value?.add(restaurantId)
                                }
                                userViewModel.saveRestaurant(
                                    "likes",
                                    filteredRestaurants[position].name!!,
                                    restaurantId
                                )
                            }
                        }

                        Direction.Left -> {
                            placesViewModel.nopes.value?.let {
                                if (!it.contains(filteredRestaurants[position])) {
                                    placesViewModel.addRestaurantToLiveData(
                                        ListType.NOPES,
                                        filteredRestaurants[position]
                                    )
                                }
                            }

                            userViewModel.nopesIds.value?.let {
                                if (!it.contains(restaurantId)) {
                                    userViewModel.nopesIds.value?.add(restaurantId)
                                }
                            }

                            userViewModel.saveRestaurant(
                                "nopes",
                                filteredRestaurants[position].name!!,
                                restaurantId
                            )
                        }

                        else -> {
                            // Handle other directions if needed
                        }
                    }
                }

                override fun onCardRewound() {
                    val position = cardStackLayoutManager.topPosition

                    placesViewModel.removeRestaurantFromLiveData(
                        ListType.LIKES,
                        filteredRestaurants[position]
                    )

                    placesViewModel.removeRestaurantFromLiveData(
                        ListType.NOPES,
                        filteredRestaurants[position]
                    )

                    filteredRestaurants[position].name?.let {
                        userViewModel.deleteRestaurant("likes", it)
                    }

                    filteredRestaurants[position].name?.let {
                        userViewModel.deleteRestaurant("nopes", it)
                    }
                }

                override fun onCardCanceled() {
                }

                override fun onCardAppeared(view: View?, position: Int) {
                }

                override fun onCardDisappeared(view: View?, position: Int) {
                }
            })
        cardStackLayoutManager.setMaxDegree(30.0f)
        cardStackLayoutManager.setDirections(Direction.HORIZONTAL)

        binding.rvRestaurantsStack.let { cardStackView ->
            cardStackView.adapter = restaurantsAdapter
            cardStackView.layoutManager = cardStackLayoutManager
            cardStackView.hasFixedSize()
        }

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

    private fun addCurrentLocationObserver() {
        locationViewModel.currentLocation.observe(viewLifecycleOwner) { location ->
            if (location != null) {
                val currentPosition = LatLng(location.latitude, location.longitude)

                placesViewModel.loadNearbyRestaurants(
                    currentPosition,
                    discoverySettings
                )
            } else {
                Log.e("RestaurantsFragment", "Failed to obtain location.")
            }
        }
    }

    private fun addNearbyRestaurantObserver() {
        placesViewModel.nearbyRestaurants.observe(viewLifecycleOwner) { nearbyRestaurants ->
            if (nearbyRestaurants != null) {
                filteredRestaurants =
                    nearbyRestaurants.filter { it.rating!! >= discoverySettings.minRating }
                        .toMutableList()

                if (discoverySettings.priceLevels.isNotEmpty()) {
                    filteredRestaurants =
                        filteredRestaurants.filter { restaurants ->
                            discoverySettings.priceLevels.any { priceLevel ->
                                restaurants.priceLevel == priceLevel
                            }
                        }.toMutableList()
                }

                val matchingCategories =
                    userViewModel.firestoreRepository.foodCategories.filter { category ->
                        discoverySettings.placeTypes.any { categoryString -> category.name == categoryString }
                    }.map { it.type }
                filteredRestaurants =
                    filteredRestaurants.filter { restaurants ->
                        if (matchingCategories.isNotEmpty()) {
                            matchingCategories.any { categories ->
                                if (restaurants.placeTypes.isNullOrEmpty()) {
                                    true
                                } else {
                                    restaurants.placeTypes!!.contains(categories)
                                }
                            }
                        } else {
                            true
                        }
                    }.toMutableList()

                if (discoverySettings.openNow) {
                    filteredRestaurants =
                        filteredRestaurants.filter {
                            placesViewModel.isPlaceOpenNow(it) == true
                        }.toMutableList()
                }

                val allPlaceIds = mutableListOf<Id>()

                userViewModel.likesIds.value?.let {
                    allPlaceIds += it
                }
                userViewModel.nopesIds.value?.let {
                    allPlaceIds += it
                }

                filteredRestaurants = filteredRestaurants.filter { restaurant ->
                    !allPlaceIds.any { restaurantId ->
                        restaurant.id == restaurantId.id
                    }
                }.toMutableList()

                restaurantsAdapter.addRestaurants(filteredRestaurants)
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