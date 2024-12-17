package com.mrakramov.yandextest.ui.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrakramov.yandextest.domain.LocationEntity
import com.mrakramov.yandextest.domain.LocationRepository
import com.mrakramov.yandextest.utils.resultOf
import com.yandex.mapkit.geometry.Point
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(private val repository: LocationRepository) :
    ViewModel() {

    private val _event = Channel<LocationEvents>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()


    fun loadLocations() {
        viewModelScope.launch(Dispatchers.IO) {
            resultOf { repository.loadLocationsList() }.onSuccess {
                _event.send(LocationEvents.ShowLocations(it))
            }
        }
    }

    fun addFavoriteLocation(name: String, address: String, point: Point) {
        viewModelScope.launch(Dispatchers.IO) {
            resultOf {
                repository.insertLocation(
                    LocationEntity(name, address, point.latitude, point.longitude)
                )
            }.onSuccess {
                _event.send(LocationEvents.SuccessfullyAdded)
            }.onFailure {
                _event.send(LocationEvents.Failure(it.message ?: ""))
            }
        }
    }
}

sealed class LocationEvents {
    data object SuccessfullyAdded : LocationEvents()
    data class Failure(val message: String) : LocationEvents()
    data class ShowLocations(val list: List<LocationEntity> = emptyList()) : LocationEvents()
}
