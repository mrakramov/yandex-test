package com.mrakramov.yandextest.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SearchItem(
    val name: String,
    val latitude: Double,
    val longitude: Double
) : Parcelable