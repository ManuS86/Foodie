package com.example.foodie.ui.permissions

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.foodie.LocationViewModel
import com.example.foodie.R
import com.example.foodie.databinding.FragmentLocationDeniedBinding

class LocationDeniedFragment : Fragment() {
    private val locationViewModel: LocationViewModel by activityViewModels()
    private lateinit var binding: FragmentLocationDeniedBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        locationViewModel.isGPSEnabled()
        locationViewModel.checkLocationPermission()
        binding = FragmentLocationDeniedBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addLocationPermissionObserver()

        binding.btnGoToSettings.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val uri = Uri.parse("package:com.example.foodie")
            intent.data = uri
            startActivity(intent)
        }
    }

    private fun addLocationPermissionObserver() {
        locationViewModel.locationPermission.observe(viewLifecycleOwner) { granted ->
            if (granted == true) {
                // Permissions granted, start location tracking
                locationViewModel.requestLocationUpdates(60000,30000)
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