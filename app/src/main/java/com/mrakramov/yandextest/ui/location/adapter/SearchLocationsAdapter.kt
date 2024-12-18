package com.mrakramov.yandextest.ui.location.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mrakramov.yandextest.databinding.ItemFavoriteLocationBinding
import com.mrakramov.yandextest.domain.SearchItem

class SearchLocationsAdapter(val onClick: (data: SearchItem) -> Unit) :
    ListAdapter<SearchItem, SearchLocationsAdapter.VH>(DiffUtils) {

    object DiffUtils : ItemCallback<SearchItem>() {
        override fun areItemsTheSame(oldItem: SearchItem, newItem: SearchItem): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: SearchItem, newItem: SearchItem): Boolean {
            return oldItem == newItem
        }
    }

    inner class VH(private val binding: ItemFavoriteLocationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(data: SearchItem) {
            binding.tvName.text = data.name
            binding.tvAddress.text = data.name

            binding.root.setOnClickListener {
                onClick(data)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemFavoriteLocationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.onBind(getItem(position))
    }
}