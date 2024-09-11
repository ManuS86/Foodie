package com.example.foodie.ui.restaurants

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.foodie.R
import com.example.foodie.adapter.HistoryAdapter
import com.example.foodie.databinding.FragmentHistoryBinding
import com.example.foodie.ui.viewmodels.LocationViewModel
import com.example.foodie.ui.viewmodels.PlacesViewModel
import com.example.foodie.ui.viewmodels.UserViewModel

class HistoryFragment : Fragment() {
    private val locationViewModel: LocationViewModel by activityViewModels()
    private val placesViewModel: PlacesViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var binding: FragmentHistoryBinding
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(inflater)

        locationViewModel.isGPSEnabled()
        locationViewModel.checkLocationPermission()

        if (userViewModel.historyIds.value != null) {
            placesViewModel.loadRestaurantById("history", userViewModel.historyIds.value!!)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        historyAdapter = HistoryAdapter(
            requireContext(),
            mutableListOf(),
            locationViewModel,
            placesViewModel,
            userViewModel
        )

        binding.rvHistory.adapter = historyAdapter
        Log.d(TAG, "History LiveData ${placesViewModel.history.value}")
        placesViewModel.history.value?.let { historyAdapter.addHistory(it) }
        binding.rvHistory.hasFixedSize()

        addLocationPermissionObserver()
        addHistoryObserverWithAdapter()

        binding.etSearch.addTextChangedListener { s ->
            historyAdapter.filterHistory(s.toString())
            if (s.isNullOrBlank()) {
                binding.ivCloseSearch.visibility = View.INVISIBLE
            } else {
                binding.ivCloseSearch.visibility = View.VISIBLE
                binding.ivCloseSearch.setOnClickListener {
                    binding.etSearch.setText("")
                }
            }
        }
    }

    private fun addHistoryObserverWithAdapter() {
        placesViewModel.history.observe(viewLifecycleOwner) { history ->
            historyAdapter.addHistory(history)
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