package com.example.foodie.ui.permissions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.foodie.LocationViewModel
import com.example.foodie.R
import com.example.foodie.databinding.FragmentLocationPermissionBinding

class LocationPermissionFragment : Fragment() {
    private val locationViewModel: LocationViewModel by activityViewModels()
    private lateinit var binding: FragmentLocationPermissionBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLocationPermissionBinding.inflate(inflater)

        locationViewModel.isGPSEnabled()
        locationViewModel.checkLocationPermission()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addLocationPermissionObserver()

        binding.btnAllow.setOnClickListener {
            locationViewModel.nullLocationPermission()
            locationViewModel.handleLocationRequest(requireActivity())
            locationViewModel.locationPermission.observe(viewLifecycleOwner) { hasPermission ->
                if (hasPermission != null) {
                    if (hasPermission) {
                        findNavController().navigate(R.id.restaurantsFragment)
                    } else {
                        findNavController().navigate(R.id.locationDeniedFragment)
                    }
                }
            }
        }
    }

    private fun addLocationPermissionObserver() {
        locationViewModel.locationPermission.observe(viewLifecycleOwner) { granted ->
            if (granted == true) {
                // Permissions granted, start location tracking
                locationViewModel.requestLocationUpdates(60000, 30000)
                addGPSObserver()
            }
        }
    }

    private fun addGPSObserver() {
        locationViewModel.gpsProvider.observe(viewLifecycleOwner) { enabled ->
            if (enabled) {
                // GPS enabled
                findNavController().navigate(R.id.restaurantsFragment)
            } else {
                findNavController().navigate(R.id.noGpsFragment)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        locationViewModel.isGPSEnabled()
        locationViewModel.checkLocationPermission()
    }
}