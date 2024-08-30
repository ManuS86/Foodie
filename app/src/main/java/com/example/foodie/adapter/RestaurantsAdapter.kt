package com.example.foodie.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
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
    private val context: Context
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
        val matchingCategories = Repository().foodCategories.filter { category ->
            restaurant?.placeTypes!!.any { categoryString -> category.type == categoryString }
        }

        holder.binding.let { binding ->
            if (restaurant != null) {
                binding.tvRestaurantName.text =
                    restaurant.name!! +
                            " " +
                            when (restaurant.priceLevel?.toString()) {
                                "1" -> {
                                    "€"
                                }

                                "2" -> {
                                    "€€"
                                }

                                "3" -> {
                                    "€€€"
                                }

                                "4" -> {
                                    "€€€€"
                                }

                                else -> {
                                    "N/A"
                                }
                            }
                matchingCategories.forEach { category ->
                    addChip(category.name, holder)
                }
                binding.tvRating.text = restaurant.rating?.toString() ?: "N/A"
                if (restaurant.rating != null) {
                    binding.rbRating.rating = restaurant.rating?.toFloat()!!
                }
                binding.tvRatingTotal.text = "(${restaurant.userRatingsTotal?.toString() ?: "0"})"
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

    private fun addChip(
        category: String,
        holder: ItemViewHolder
    ) {
        val chipGroup = holder.binding.cpgCategoriesRestaurant
        val chip = Chip(context)
        chip.text = category
        chip.setChipBackgroundColorResource(R.color.off_grey)
        chip.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_LabelSmall)
        chip.setTextColor(context.resources.getColor(R.color.off_white, null))
        chip.shapeAppearanceModel = ShapeAppearanceModel().withCornerSize(160f)
        chipGroup.addView(chip)
    }
}