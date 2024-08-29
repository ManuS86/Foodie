package com.example.foodie.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodie.data.model.Category
import com.example.foodie.databinding.ItemFoodFilterBinding

class FoodFilterAdapter(
    private val dataset: List<Category>
) : RecyclerView.Adapter<FoodFilterAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(val binding: ItemFoodFilterBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding =
            ItemFoodFilterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val foodCategory = dataset[position]

        holder.binding.cpCategory.text = foodCategory.name
    }

    override fun getItemCount(): Int {
        return dataset.size
    }
}