package com.example.foodie.ui.restaurants

import android.annotation.SuppressLint
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
import com.example.foodie.databinding.FragmentNavigationDetailBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.Place

class NavigationDetailFragment : Fragment() {
    private val locationViewModel: LocationViewModel by activityViewModels()
    private val nearbyRestaurantsViewModel: NearbyRestaurantsViewModel by activityViewModels()
    private lateinit var binding: FragmentNavigationDetailBinding
    private lateinit var mapView: MapView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        locationViewModel.isGPSEnabled()
        locationViewModel.checkLocationPermission()
        locationViewModel.requestLocationUpdates(5000, 2000)

        binding = FragmentNavigationDetailBinding.inflate(inflater)
        mapView = binding.mvNavigation
        mapView.onCreate(savedInstanceState)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val restaurant = nearbyRestaurantsViewModel.currentRestaurant.value!!

        addLocationPermissionObserver()
        initializeMap(restaurant)

        binding.fabMyLocation.setOnClickListener {
            locationViewModel.currentLocation.value?.let { location ->
                val currentPosition = LatLng(location.latitude, location.longitude)
                mapView.getMapAsync { googleMap ->
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 15f))
                }
            }
        }

        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
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