package com.example.foodie.adapter

import android.content.Context
import android.location.Location
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.foodie.R
import com.example.foodie.data.model.DiscoverySettings
import com.example.foodie.data.model.Id
import com.example.foodie.databinding.ItemHistoryBinding
import com.example.foodie.ui.viewmodels.LocationViewModel
import com.example.foodie.ui.viewmodels.PlacesViewModel
import com.example.foodie.ui.viewmodels.UserViewModel
import com.example.foodie.utils.addIndicatorChipSmall
import com.google.android.libraries.places.api.model.Place
import java.text.DecimalFormat

class HistoryAdapter(
    private val context: Context,
    private var dataset: MutableList<Place>,
    private val locationViewModel: LocationViewModel,
    private val placesViewModel: PlacesViewModel,
    private val userViewModel: UserViewModel,
    private val view: View
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
            val date = userViewModel.historyIds.value?.get(position)?.date
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

            val alertDialogRemove = AlertDialog.Builder(view.context)
                .setMessage("Are you sure you want to remove ${restaurant.name} from your History?")
                .setTitle("Remove History Entry")
                .setPositiveButton("Remove") { _, _ ->
                    val restaurantId = Id(restaurant.name, restaurant.id, null)

                    userViewModel.historyIds.value?.removeAll { it.id == restaurantId.id }
                    userViewModel.deleteRestaurant("history", restaurant.name!!)
                    dataset.removeAt(position)
                    notifyItemRemoved(position)
                }.setNegativeButton("Cancel") { _, _ ->
                }.create()

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

                binding.tvDateHistory.text = date
                binding.tvDistanceHistory.text =
                    if (appSettings?.distanceUnit == "Km") {
                        val decimalFormat = DecimalFormat("#.#")
                        val distanceInKm = distanceInMeters?.div(1000.0f)?.toDouble()
                        val formattedDistanceInKm = decimalFormat.format(distanceInKm)
                        "$formattedDistanceInKm Km away"
                    } else {
                        val decimalFormat = DecimalFormat("#.#")
                        val distanceInMi = distanceInMeters?.let { it / 1609.34 }
                        val formattedDistanceInMi = decimalFormat.format(distanceInMi)
                        "$formattedDistanceInMi Mi away"
                    }

                binding.cvHistory.setOnClickListener {
                    placesViewModel.setCurrentRestaurantLikesAndHistory("history", position)
                    holder.itemView.findNavController().navigate(
                        R.id.restaurantsDetailFragmentNoButtons
                    )
                }

                binding.ivRemoveEntry.setOnClickListener {
                    alertDialogRemove.show()
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