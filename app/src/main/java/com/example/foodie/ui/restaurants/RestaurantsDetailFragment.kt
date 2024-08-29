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
    private val nearbyRestaurantsViewModel: NearbyRestaurantsViewModel by activityViewModels()
    private val locationViewModel: LocationViewModel by activityViewModels()
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
        val formattedOpeningHours =
            restaurant.currentOpeningHours?.weekdayText?.joinToString("\n") {
                val weekday = it.replace("[", "").replace("]", "").substring(0, 3)
                val openingTime = it.substring(it.indexOf(":") + 1)
                "    $weekday $openingTime"
            } ?: "N/A"

        addLocationPermissionObserver()
        initializeMap(restaurant)

        binding.tvNameTitle.text = restaurant.name
        matchingCategories.forEach { category ->
            addChip(category.name, chipGroup)
        }
        binding.tvHours.text = "Hours:\n$formattedOpeningHours"
        binding.tvRating.text = restaurant.rating?.toString() ?: "N/A"
        if (restaurant.rating != null) {
            binding.rbRating.rating = restaurant.rating?.toFloat()!!
        }
        binding.tvRatingsTotal.text = "(${restaurant.userRatingsTotal?.toString() ?: "0"})"
        binding.tvPriceRangeDiscovery.text = when (restaurant.priceLevel?.toString()) {
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