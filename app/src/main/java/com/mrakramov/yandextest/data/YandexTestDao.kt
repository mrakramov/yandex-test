package com.mrakramov.yandextest.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mrakramov.yandextest.domain.LocationEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface LocationDao {

    @Query("SELECT * FROM locationentity")
    fun loadLocations(): Flow<List<LocationEntity>>

    @Query("SELECT * FROM locationentity WHERE id = :id")
    suspend fun loadLocationById(id: Int): LocationEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(entity: LocationEntity)

    @Query("DELETE FROM locationentity WHERE id=:id")
    suspend fun deleteLocation(id: Int)

    @Query("SELECT * FROM locationentity WHERE name like '%' || :text  ||  '%'")
    suspend fun searchLocation(text: String): List<LocationEntity>
}