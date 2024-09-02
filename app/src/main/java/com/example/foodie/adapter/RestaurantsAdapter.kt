package com.example.foodie.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.foodie.NearbyRestaurantsViewModel
import com.example.foodie.R
import com.example.foodie.data.Repository
import com.example.foodie.databinding.ItemRestaurantBinding
import com.google.android.libraries.places.api.model.Place
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.shape.ShapeAppearanceModel

class RestaurantsAdapter(
    private val dataset: List<Place>?,
    private val nearbyRestaurantsViewModel: NearbyRestaurantsViewModel,
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<RestaurantsAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(val binding: ItemRestaurantBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding =
            ItemRestaurantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val restaurant = dataset?.get(position)
        val chipGroup = holder.binding.cpgCategoriesRestaurant
        val matchingCategories = Repository().foodCategories.filter { category ->
            restaurant?.placeTypes!!.any { categoryString -> category.type == categoryString }
        }
        val formattedOpeningHoursToday =
            restaurant?.currentOpeningHours?.periods?.get(0)?.let { period ->
                val openTime =
                    "%02d:%02d".format(period.open?.time?.hours, period.open?.time?.minutes)
                val closeTime =
                    "%02d:%02d".format(period.close?.time?.hours, period.close?.time?.minutes)
                "$openTime - $closeTime"
            } ?: "Closed"

        holder.binding.let { binding ->
            restaurant?.photoMetadatas?.take(1)?.forEach { photoMetadata ->
                nearbyRestaurantsViewModel.getPhoto(photoMetadata)
                    .observe(lifecycleOwner) { bitmap ->
                        // Update UI with the fetched photos
                        if (bitmap != null) {
                            binding.ivRestaurant.setImageBitmap(bitmap)
                        } else {
                            binding.ivRestaurant.setImageResource(R.drawable.placeholder_image)
                        }
                    }
            }
//            binding.tvAttributions.text = restaurant?.photoMetadatas?.get(0)?.authorAttributions.toString()
            if (restaurant != null) {
                binding.tvRestaurantName.text =
                    restaurant.name!! +
                            when (restaurant.priceLevel?.toString()) {
                                "1" -> { ", €" }
                                "2" -> { ", €€" }
                                "3" -> { ", €€€" }
                                "4" -> { ", €€€€" }
                                else -> { "" }
                            }
                matchingCategories.forEach { category ->
                    addChip(category.name, chipGroup)
                }
                binding.tvRating.text = restaurant.rating?.toString() ?: "n/a"
                binding.rbRating.rating = restaurant.rating?.toFloat()  ?: 0f
                binding.tvRatingTotal.text = "(${restaurant.userRatingsTotal?.toString() ?: "0"})"
                binding.tvHoursNow.text = "Hours: $formattedOpeningHoursToday"
                binding.ivDecollapseButton.setOnClickListener {
                    nearbyRestaurantsViewModel.setCurrentRestaurant(position)
                    holder.itemView.findNavController().navigate(
                        R.id.restaurantDetailFragment
                    )
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return dataset?.size ?: 0
    }

    private fun addChip(category: String, chipGroup: ChipGroup) {
        val chip = Chip(context)
        chip.text = category
        chip.setChipBackgroundColorResource(R.color.off_grey)
        chip.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_LabelLarge)
        chip.setTextColor(context.resources.getColor(R.color.off_white, null))
        chip.chipStrokeWidth = 0f
        chip.isClickable = false
        chip.shapeAppearanceModel = ShapeAppearanceModel().withCornerSize(160f)

        chipGroup.addView(chip)
    }

    private fun addCheckedChip(category: String, chipGroup: ChipGroup) {
        val chip = Chip(context)
        chip.text = category
        chip.background = ResourcesCompat.getDrawable(context.resources, R.drawable.gradient_button_primary,null)
        chip.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_LabelLarge)
        chip.setTextColor(context.resources.getColor(R.color.off_black, null))
        chip.chipStrokeWidth = 0f
        chip.isClickable = false
        chip.shapeAppearanceModel = ShapeAppearanceModel().withCornerSize(160f)
        chipGroup.addView(chip)
    }
}