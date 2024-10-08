package com.example.foodie.adapter

import android.content.Context
import android.icu.text.SimpleDateFormat
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
import com.example.foodie.databinding.ItemLikesBinding
import com.example.foodie.ui.viewmodels.LocationViewModel
import com.example.foodie.ui.viewmodels.PlacesViewModel
import com.example.foodie.ui.viewmodels.UserViewModel
import com.example.foodie.utils.addIndicatorChipSmall
import com.google.android.libraries.places.api.model.Place
import java.text.DecimalFormat
import java.util.Date
import java.util.Locale

class LikesAdapter(
    private val context: Context,
    private var dataset: MutableList<Place>,
    private val locationViewModel: LocationViewModel,
    private val placesViewModel: PlacesViewModel,
    private val userViewModel: UserViewModel,
    private val view: View
) : RecyclerView.Adapter<LikesAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(val binding: ItemLikesBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemLikesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

            val alertDialogHistory =
                AlertDialog.Builder(view.context)
                    .setMessage("Are you sure you want to mark ${restaurant.name} as visited?")
                    .setTitle("Add to History").setPositiveButton("Move") { _, _ ->
                        val currentTime = System.currentTimeMillis()
                        val date = Date(currentTime)
                        val formattedDate =
                            SimpleDateFormat("dd.mm.yyyy", Locale.getDefault()).format(date)
                        val restaurantId = Id(restaurant.name, restaurant.id, formattedDate)

                        userViewModel.likesIds.value?.removeAll { it.id == restaurantId.id }
                        restaurant.name?.let { restaurantName ->
                            userViewModel.saveRestaurant(
                                "history",
                                restaurantName,
                                restaurantId
                            )
                        }
                        userViewModel.historyIds.value?.add(restaurantId)
                        userViewModel.deleteRestaurant("likes", restaurant.name!!)
                        dataset.removeAt(position)
                        notifyItemRemoved(position)
                    }.setNegativeButton("Cancel") { _, _ ->
                    }.create()

            val alertDialogRemove = AlertDialog.Builder(view.context)
                .setMessage("Are you sure you want to remove ${restaurant.name} from your Likes?")
                .setTitle("Remove Like")
                .setPositiveButton("Remove") { _, _ ->
                    val restaurantId = Id(restaurant.name, restaurant.id, null)

                    userViewModel.likesIds.value?.removeAll { it.id == restaurantId.id }
                    userViewModel.deleteRestaurant("likes", restaurant.name!!)
                    dataset.removeAt(position)
                    notifyItemRemoved(position)
                }.setNegativeButton("Cancel") { _, _ ->
                }.create()

            holder.binding.let { binding ->
                if (restaurant.photoMetadatas?.isNotEmpty() == true) {
                    val photoMetadata = restaurant.photoMetadatas?.get(0)
                    if (photoMetadata != null) {
                        placesViewModel.loadPhoto(photoMetadata) { photo ->
                            binding.ivRestaurantPicLikes.setImageBitmap(photo)
                        }
                    }
                } else {
                    binding.ivRestaurantPicLikes.setImageResource(R.drawable.placeholder_image)
                    Log.e("Error", "Photos list is empty")
                }
                binding.tvRestaurantNameLikes.text = restaurant.name
                val chipGroup = binding.cpgCategoriesLikes
                chipGroup.removeAllViews()
                matchingCategories.forEach { category ->
                    addIndicatorChipSmall(
                        category.name,
                        chipGroup,
                        context,
                        discoverySettings ?: DiscoverySettings()
                    )
                }

                binding.tvDistanceLikes.text =
                    if (appSettings?.distanceUnit == "Km") {
                        val decimalFormat = DecimalFormat("#.#")
                        val distanceInKm = distanceInMeters?.let { (it.div(1000.0f)).toDouble() }
                        val formattedDistanceInKm = decimalFormat.format(distanceInKm)
                        "$formattedDistanceInKm Km away"
                    } else {
                        val decimalFormat = DecimalFormat("#.#")
                        val distanceInMi = distanceInMeters?.let { it / 1609.34 }
                        val formattedDistanceInMi = decimalFormat.format(distanceInMi)
                        "$formattedDistanceInMi Mi away"
                    }

                binding.cvLikes.setOnClickListener {
                    placesViewModel.setCurrentRestaurantLikesAndHistory("likes", position)
                    holder.itemView.findNavController().navigate(
                        R.id.restaurantsDetailFragmentNoButtons
                    )
                }

                binding.ivApproveLikes.setOnClickListener {
                    alertDialogHistory.show()
                }

                binding.ivDismissLikes.setOnClickListener {
                    alertDialogRemove.show()
                }
            }
        }
    }

    fun addLikes(likes: MutableList<Place>) {
        dataset = likes
        notifyDataSetChanged()
    }

    fun filterLikes(s: String) {
        if (placesViewModel.likes.value != null) {
            dataset = placesViewModel.likes.value?.filter {
                it.name!!.contains(s, true)
            }?.toMutableList()!!
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return dataset.size
    }
}