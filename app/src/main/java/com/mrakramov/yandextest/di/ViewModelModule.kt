package com.mrakramov.yandextest.di

import com.mrakramov.yandextest.data.YandexTestDB
import com.mrakramov.yandextest.data.repo.LocationRepoImp
import com.mrakramov.yandextest.domain.LocationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import javax.inject.Singleton

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {

    @Provides
    fun provideRepository(db: YandexTestDB): LocationRepository {
        return LocationRepoImp(db.dao)
    }
}