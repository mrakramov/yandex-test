package com.mrakramov.yandextest.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mrakramov.yandextest.domain.LocationEntity

@Database(
    entities = [LocationEntity::class], version = 2, exportSchema = false
)
abstract class YandexTestDB : RoomDatabase() {

    internal abstract val dao: LocationDao

    companion object {
        const val DATABASE_NAME = "location_db"
    }
}

@Synchronized
fun getDB(context: Context) = Room.databaseBuilder(
    context, YandexTestDB::class.java, YandexTestDB.DATABASE_NAME
).fallbackToDestructiveMigration().build()