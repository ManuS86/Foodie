package com.example.foodie.ui.restaurants

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.foodie.MainViewModel
import com.example.foodie.R
import com.example.foodie.databinding.FragmentNavigationDetailBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class NavigationDetailFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentNavigationDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.isGPSEnabled(requireContext())
        viewModel.checkLocationPermission(requireContext())
        binding = FragmentNavigationDetailBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mvNavigation.getMapAsync { googleMap ->
            // Add initial marker
            val markerOptions = MarkerOptions()
                .position(LatLng(0.0, 0.0))
                .title("Marker")
            googleMap.addMarker(markerOptions)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerOptions.position, 15f))
        }


//        viewModel.currentLocation.observe(viewLifecycleOwner)
//        { location ->
//            location?.let {
//                val initialPosition = LatLng(location.latitude, location.longitude)
//                binding.mvNavigation.getMapAsync { googleMap ->
//                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPosition, 15f))
//                }
//            }
//        }

        viewModel.locationPermission.observe(viewLifecycleOwner) { granted ->
            if (granted == true) {
                // Permissions granted, start location tracking
                viewModel.gpsProvider.observe(viewLifecycleOwner) { enabled ->
                    if (enabled) {
                        // GPS enabled
                    } else {
                        findNavController().navigate(R.id.noGpsFragment)
                    }
                }
            } else {
                findNavController().navigate(R.id.locationPermissionFragment)
            }
        }

        binding.fabMyLocationNavigation.setOnClickListener {

        }

        binding.fabDirectionsNavigation.setOnClickListener {

        }

        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.requestLocationUpdates(requireActivity())
    }

    override fun onResume() {
        super.onResume()
        viewModel.isGPSEnabled(requireContext())
    }
}