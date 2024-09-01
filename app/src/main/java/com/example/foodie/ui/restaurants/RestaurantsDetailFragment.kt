package com.example.foodie.ui.restaurants

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
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
import com.example.foodie.UserViewModel
import com.example.foodie.data.Repository
import com.example.foodie.databinding.FragmentRestaurantsDetailBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.Place
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.shape.ShapeAppearanceModel

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
        val matchingCategories = Repository().foodCategories.filter { category ->
            restaurant.placeTypes?.any { categoryString -> category.type == categoryString }
                ?: false
        }
        val formattedOpeningHoursToday =
            restaurant.currentOpeningHours?.periods?.get(0)?.let { period ->
                val openTime =
                    "%02d:%02d".format(period.open?.time?.hours, period.open?.time?.minutes)
                val closeTime =
                    "%02d:%02d".format(period.close?.time?.hours, period.close?.time?.minutes)
                "$openTime - $closeTime"
            } ?: "Closed"
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
            } ?: "N/A"

        addLocationPermissionObserver()
        initializeMap(restaurant)

        binding.tvNameTitle.text = restaurant.name
        matchingCategories.forEach { category ->
            addChip(category.name, chipGroup)
        }
        binding.tvTodaysHours.text = formattedOpeningHoursToday
        binding.tvWeekdays.text = formattedOpeningWeekdays
        binding.tvTimes.text = formattedOpeningHours
        binding.tvRating.text = restaurant.rating?.toString() ?: "N/A"
        if (restaurant.rating != null) {
            binding.rbRating.rating = restaurant.rating?.toFloat()!!
        }
        binding.tvRatingsTotal.text = "(${restaurant.userRatingsTotal?.toString() ?: "0"})"
        binding.tvPriceLevelDiscovery.text = when (restaurant.priceLevel?.toString()) {
            "1" -> {
                "€"
            }

            "2" -> {
                "€€"
            }

            "3" -> {
                "€€€"
            }

            "4" -> {
                "€€€€"
            }

            else -> {
                "N/A"
            }
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

        binding.fabDismiss.setOnClickListener {
            userViewModel.addNewRestaurant("dismissed", restaurant.name!!, restaurant)
        }

        binding.fabLike.setOnClickListener {
            userViewModel.addNewRestaurant("liked", restaurant.name!!, restaurant)
        }
    }

    private fun addChip(
        category: String,
        chipGroup: ChipGroup
    ) {
        val chip = Chip(context)
        chip.text = category
        chip.setChipBackgroundColorResource(R.color.off_grey)
        chip.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_LabelSmall)
        chip.setTextColor(resources.getColor(R.color.off_white, null))
        chip.shapeAppearanceModel = ShapeAppearanceModel().withCornerSize(160f)
        chipGroup.addView(chip)
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