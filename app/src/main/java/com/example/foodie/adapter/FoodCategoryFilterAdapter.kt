package com.example.foodie.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodie.data.model.Category
import com.example.foodie.databinding.ItemFoodsFilterBinding

class FoodCategoryFilterAdapter(
    private val dataset: List<Category>
) : RecyclerView.Adapter<FoodCategoryFilterAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(val binding: ItemFoodsFilterBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding =
            ItemFoodsFilterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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