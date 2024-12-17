package com.mrakramov.yandextest.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.mrakramov.yandextest.data.YandexTestDB
import com.mrakramov.yandextest.data.getDB
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule{

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): YandexTestDB {
        return getDB(context)
    }
}