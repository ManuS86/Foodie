package com.example.foodie.ui.restaurants

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.foodie.LocationViewModel
import com.example.foodie.NearbyRestaurantsViewModel
import com.example.foodie.R
import com.example.foodie.UserViewModel
import com.example.foodie.addIndicatorChip
import com.example.foodie.data.model.DiscoverySettings
import com.example.foodie.databinding.FragmentRestaurantsDetailBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.Place

class RestaurantsDetailFragment : Fragment() {
    private val locationViewModel: LocationViewModel by activityViewModels()
    private val nearbyRestaurantsViewModel: NearbyRestaurantsViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var binding: FragmentRestaurantsDetailBinding
    private lateinit var mapView: MapView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        locationViewModel.isGPSEnabled()
        locationViewModel.checkLocationPermission()

        binding = FragmentRestaurantsDetailBinding.inflate(inflater)
        mapView = binding.ivMapPreview
        mapView.onCreate(savedInstanceState)
        return binding.root
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val restaurant = nearbyRestaurantsViewModel.currentRestaurant.value!!
        val chipGroup = binding.cpgRestaurantCategories
        val discoverySettings = userViewModel.currentDiscoverySettings.value
        val matchingCategories = userViewModel.repository.foodCategories.filter { category ->
            restaurant.placeTypes?.any { categoryString -> category.type == categoryString }
                ?: false
        }
        val formattedOpeningWeekdays =
            restaurant.currentOpeningHours?.periods?.joinToString("\n") { period ->
                period.open?.day.toString().let { day ->
                    day.substring(0, 1) + day.substring(1, 3).lowercase()
                }
            } ?: ""
        val formattedOpeningHours =
            restaurant.currentOpeningHours?.periods?.joinToString("\n") { period ->
                val openTime =
                    "%02d:%02d".format(period.open?.time?.hours, period.open?.time?.minutes)
                val closeTime =
                    "%02d:%02d".format(period.close?.time?.hours, period.close?.time?.minutes)
                "$openTime - $closeTime"
            } ?: "N/a"

        addLocationPermissionObserver()
        initializeMap(restaurant)

        binding.tvNameTitle.text = restaurant.name
        matchingCategories.forEach { category ->
            addIndicatorChip(category.name, chipGroup, requireContext(), discoverySettings ?: DiscoverySettings())
        }
        binding.tvTodaysHours.text = if (nearbyRestaurantsViewModel.isPlaceOpenNow(restaurant)) {
            binding.tvTodaysHours.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.open_green
                )
            )
            "Open"
        } else {
            binding.tvTodaysHours.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.open_red
                )
            )
            "Closed"
        }
        binding.tvWeekdays.text = formattedOpeningWeekdays
        binding.tvTimes.text = formattedOpeningHours
        binding.tvRating.text = restaurant.rating?.toString() ?: "n/a"
        binding.rbRating.rating = restaurant.rating?.toFloat() ?: 0f
        binding.tvRatingsTotal.text = "(${restaurant.userRatingsTotal?.toString() ?: "0"})"
        binding.tvPriceLevelDiscovery.text = when (restaurant.priceLevel?.toString()) {
            "1" -> "€"
            "2" -> "€€"
            "3" -> "€€€"
            "4" -> "€€€€"
            else -> "N/A"
        }
        binding.tvAddress.text = restaurant.address
        if (restaurant.websiteUri != null) {
            binding.cvWebsite.visibility = View.VISIBLE
            binding.btnWebsite.setOnClickListener {
                val uri = Uri.parse(restaurant.websiteUri?.toString())
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
        } else {
            binding.cvWebsite.visibility = View.GONE
        }

        binding.ivDecollapse.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.fabNavigate.setOnClickListener {
            findNavController().navigate(R.id.navigationDetailFragment)
        }

        binding.fabNope.setOnClickListener {
            userViewModel.addNewRestaurant("dismissed", restaurant.name!!, restaurant)
        }

        binding.fabLike.setOnClickListener {
            userViewModel.addNewRestaurant("liked", restaurant.name!!, restaurant)
        }
    }

    @SuppressLint("MissingPermission")
    private fun initializeMap(restaurant: Place) {
        mapView.getMapAsync { googleMap ->
            googleMap.isMyLocationEnabled = true
            googleMap.uiSettings.isCompassEnabled = true
            googleMap.uiSettings.isMyLocationButtonEnabled = false
            googleMap.uiSettings.setAllGesturesEnabled(true)
            googleMap.addMarker(
                MarkerOptions()
                    .position(restaurant.latLng!!)
                    .title(restaurant.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(HUE_RED))
            )
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(restaurant.latLng!!, 15f))
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

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        locationViewModel.isGPSEnabled()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}