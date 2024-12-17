package com.mrakramov.yandextest.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mrakramov.yandextest.databinding.ItemFavoriteLocationBinding
import com.mrakramov.yandextest.domain.LocationEntity

class LocationsListAdapter(val onClick: (id: Int) -> Unit) :
    ListAdapter<LocationEntity, LocationsListAdapter.VH>(DiffUtils) {

    object DiffUtils : ItemCallback<LocationEntity>() {
        override fun areItemsTheSame(oldItem: LocationEntity, newItem: LocationEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: LocationEntity, newItem: LocationEntity): Boolean {
            return oldItem == newItem
        }
    }

    inner class VH(private val binding: ItemFavoriteLocationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(data: LocationEntity) {
            binding.tvName.text = data.name
            binding.tvAddress.text = data.address

            binding.root.setOnClickListener {
                onClick(data.id)
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