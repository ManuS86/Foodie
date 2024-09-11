package com.example.foodie.adapter

import android.content.Context
import android.location.Location
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.foodie.R
import com.example.foodie.addIndicatorChipSmall
import com.example.foodie.data.model.DiscoverySettings
import com.example.foodie.databinding.ItemHistoryBinding
import com.example.foodie.ui.viewmodels.LocationViewModel
import com.example.foodie.ui.viewmodels.PlacesViewModel
import com.example.foodie.ui.viewmodels.UserViewModel
import com.google.android.libraries.places.api.model.Place
import kotlin.math.roundToInt

class HistoryAdapter(
    private val context: Context,
    private var dataset: List<Place>,
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
            val restaurant = dataset[position]
            val appSettings = userViewModel.currentAppSettings.value
            val discoverySettings = userViewModel.currentDiscoverySettings.value

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

            holder.binding.let { binding ->
                if (restaurant.photoMetadatas?.isNotEmpty() == true) {
                    val photoMetadata = restaurant.photoMetadatas?.get(0)
                    if (photoMetadata != null) {
                        placesViewModel.loadPhoto(photoMetadata) { photo ->
                            binding.ivRestaurantPicHistory.setImageBitmap(photo)
                        }
                    }
                } else {
                    binding.ivRestaurantPicHistory.setImageResource(R.drawable.placeholder_image)
                    Log.e("Error", "Photos list is empty")
                }
                binding.tvRestaurantNameHistory.text = restaurant.name
                val chipGroup = binding.cpgCategoriesHistory
                chipGroup.removeAllViews()
                matchingCategories.forEach { category ->
                    addIndicatorChipSmall(
                        category.name,
                        chipGroup,
                        context,
                        discoverySettings ?: DiscoverySettings()
                    )
                }

                val date = userViewModel.historyIds.value?.get(position)?.date
                binding.tvDateHistory.text = date

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
                    placesViewModel.setCurrentRestaurantLikesAndHistory("history", position)
                    holder.itemView.findNavController().navigate(
                        R.id.restaurantsDetailFragmentNoButtons
                    )
                }
            }
        }
    }

    fun addHistory(history: MutableList<Place>) {
        dataset = history
        notifyDataSetChanged()
    }

    fun filterHistory(s: String) {
        if (placesViewModel.history.value != null) {
            dataset = placesViewModel.history.value?.filter {
                it.name!!.contains(s, true)
            }?.toMutableList()!!
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return dataset.size ?: 0
    }
}