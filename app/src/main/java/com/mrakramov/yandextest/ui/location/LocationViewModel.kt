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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(private val repository: LocationRepository) :
    ViewModel() {

    private val _screenState = MutableStateFlow(LocationsScreenState())
    val screenState = _screenState.asStateFlow()


    private val _event = Channel<LocationEvents>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()


    fun loadLocations() {
        repository.loadLocations().flowOn(Dispatchers.IO)
            .onStart { _screenState.value = LocationsScreenState(loading = true) }
            .onEach { _event.send(LocationEvents.ShowLocations(it)) }
            .catch { _screenState.value = LocationsScreenState(loading = false) }
            .launchIn(viewModelScope)
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

data class LocationsScreenState(
    val loading: Boolean = false
)