package com.example.foodie.adapter

import android.content.Context
import android.location.Location
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.foodie.LocationViewModel
import com.example.foodie.PlacesViewModel
import com.example.foodie.R
import com.example.foodie.UserViewModel
import com.example.foodie.addIndicatorChipSmall
import com.example.foodie.data.model.DiscoverySettings
import com.example.foodie.databinding.ItemHistoryBinding
import com.google.android.libraries.places.api.model.Place
import kotlin.math.roundToInt

class HistoryAdapter(
    private val context: Context,
    private val dataset: List<Place>,
    private val lifecycleOwner: LifecycleOwner,
    private val locationViewModel: LocationViewModel,
    private val placesViewModel: PlacesViewModel,
    private val userViewModel: UserViewModel
) : RecyclerView.Adapter<HistoryAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        if (dataset.isNotEmpty()) {
            val restaurant = dataset?.get(position)
            val appSettings = userViewModel.currentAppSettings.value
            val discoverySettings = userViewModel.currentDiscoverySettings.value

            if (restaurant != null) {
                val matchingCategories =
                    userViewModel.firestoreRepository.foodCategories.filter { category ->
                        restaurant.placeTypes!!.any { categoryString -> category.type == categoryString }
                    }
                val restaurantLocation = Location("").apply {
                    latitude = restaurant.latLng?.latitude!!
                    longitude = restaurant.latLng?.longitude!!
                }
                val userLocation = locationViewModel.currentLocation.value
                val distanceInMeters = userLocation?.distanceTo(restaurantLocation)
//        val photoMetadata = restaurant?.photoMetadatas?.get(0)
                holder.binding.let { binding ->
//                if (photoMetadata != null) {
//                    placesViewModel.getPhoto(photoMetadata)
//                        .observe(lifecycleOwner) { bitmap ->
//                            if (bitmap != null) {
//                                binding.ivRestaurantPicHistory.setImageBitmap(bitmap)
//                            } else {
//                                binding.ivRestaurantPicHistory.setImageResource(R.drawable.placeholder_image)
//                            }
//                        }
//                }
                    if (restaurant.photoMetadatas?.isNotEmpty() == true) {
                        val photoMetadata = restaurant.photoMetadatas?.get(0)
                        if (photoMetadata != null) {
                            placesViewModel.resetPhotosLiveData()
                            placesViewModel.loadPhoto(photoMetadata)
                        }
                    } else {
                        Log.e("Error", "Photos list is empty")
                    }
                    if (placesViewModel.photos.value?.isNotEmpty() == true) {
                        binding.ivRestaurantPicHistory.setImageBitmap(
                            placesViewModel.photos.value?.get(0)
                        )
                    } else {
                        binding.ivRestaurantPicHistory.setImageResource(R.drawable.placeholder_image)
                    }
                    binding.tvRestaurantNameHistory.text = restaurant.name
                    val chipGroup = binding.cpgCategoriesHistory
                    matchingCategories.forEach { category ->
                        addIndicatorChipSmall(
                            category.name,
                            chipGroup,
                            context,
                            discoverySettings ?: DiscoverySettings()
                        )
                    }

//                    binding.tvDateHistory.text = userViewModel.historyIds.value(position)

                    binding.tvDistanceHistory.text = if (appSettings?.distanceUnit == "Km") {
                        val distanceInKm = (distanceInMeters?.div(1000.0f))?.roundToInt()
                        if (distanceInKm!! < 1) {
                            "Less than 1 Km away"
                        } else {
                            "$distanceInKm Km away"
                        }
                    } else {
                        val distanceInMi = (distanceInMeters?.div(621.371f))?.roundToInt()
                        if (distanceInMi!! < 1) {
                            "Less than 1 Mi away"
                        } else {
                            "$distanceInMi Mi away"
                        }
                    }

                    binding.cvHistory.setOnClickListener {
                        placesViewModel.setCurrentRestaurant(position)
                        holder.itemView.findNavController().navigate(
                            R.id.restaurantDetailFragment
                        )
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return dataset?.size ?: 0
    }
}