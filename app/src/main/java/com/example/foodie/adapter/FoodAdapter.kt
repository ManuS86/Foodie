package com.example.foodie.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodie.data.model.Food
import com.example.foodie.databinding.ItemFoodBinding

class FoodAdapter(
    private val dataset: List<Food>
) : RecyclerView.Adapter<FoodAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(val binding: ItemFoodBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val food = dataset[position]

    }

    override fun getItemCount(): Int {
        return dataset.size
    }
}