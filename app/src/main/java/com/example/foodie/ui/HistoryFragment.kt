package com.example.foodie.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.foodie.LocationViewModel
import com.example.foodie.PlacesViewModel
import com.example.foodie.R
import com.example.foodie.UserViewModel
import com.example.foodie.adapter.HistoryAdapter
import com.example.foodie.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {
    private val locationViewModel: LocationViewModel by activityViewModels()
    private val placesViewModel: PlacesViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var binding: FragmentHistoryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(inflater)

        locationViewModel.isGPSEnabled()
        locationViewModel.checkLocationPermission()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addLocationPermissionObserver()
        addPlaceObserverWithHistoryObserverAndAdapter()
    }

    private fun addPlaceObserverWithHistoryObserverAndAdapter() {
        userViewModel.historyIds.observe(viewLifecycleOwner) {
                placesViewModel.loadRestaurantById("history", it)
            addHistoryObserverWithAdapter()
        }
    }

    private fun addHistoryObserverWithAdapter() {
        placesViewModel.history.observe(viewLifecycleOwner) { history ->
            binding.rvHistory.let { recyclerView ->
                recyclerView.adapter = HistoryAdapter(
                    requireContext(),
                    history,
                    viewLifecycleOwner,
                    locationViewModel,
                    placesViewModel,
                    userViewModel
                )
                recyclerView.setHasFixedSize(true)
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

    override fun onResume() {
        super.onResume()
        locationViewModel.isGPSEnabled()
    }
}