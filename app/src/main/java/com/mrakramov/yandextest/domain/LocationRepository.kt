package com.mrakramov.yandextest.domain

import kotlinx.coroutines.flow.Flow

interface LocationRepository {

    fun loadLocations(): Flow<List<LocationEntity>>

    fun loadLocationsList(): List<LocationEntity>

   suspend fun searchLocations(text:String): List<LocationEntity>

    suspend fun loadLocationById(id: Int): LocationEntity

    suspend fun insertLocation(entity: LocationEntity)

    suspend fun deleteLocation(id: Int)
}