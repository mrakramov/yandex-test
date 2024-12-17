package com.mrakramov.yandextest

import android.app.Application
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class YandexTestApp: Application(){

    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey("522fb9ba-acc3-4c2a-ad64-371448cace44")
    }
}