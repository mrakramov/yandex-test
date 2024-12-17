package com.mrakramov.yandextest.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LocationEntity(
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)
