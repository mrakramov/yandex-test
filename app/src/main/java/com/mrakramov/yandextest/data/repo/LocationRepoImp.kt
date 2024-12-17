package com.mrakramov.yandextest.data.repo

import com.mrakramov.yandextest.data.LocationDao
import com.mrakramov.yandextest.domain.LocationEntity
import com.mrakramov.yandextest.domain.LocationRepository
import kotlinx.coroutines.flow.Flow

class LocationRepoImp(private val dao: LocationDao) : LocationRepository {

    override fun loadLocations(): Flow<List<LocationEntity>> = dao.loadLocations()

    override suspend fun searchLocations(text: String): List<LocationEntity> =
        dao.searchLocation(text)

    override suspend fun loadLocationById(id: Int): LocationEntity = dao.loadLocationById(id)

    override suspend fun insertLocation(entity: LocationEntity) = dao.insertLocation(entity)

    override suspend fun deleteLocation(id: Int) = dao.deleteLocation(id)
}