package com.mrakramov.yandextest.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrakramov.yandextest.domain.LocationEntity
import com.mrakramov.yandextest.domain.LocationRepository
import com.mrakramov.yandextest.utils.resultOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: LocationRepository) : ViewModel() {

    private val _screenState = MutableStateFlow(MainScreenState())
    val screenState = _screenState.asStateFlow()

    init {
        loadLocations()
    }

    private fun loadLocations() {
        repository.loadLocations().flowOn(Dispatchers.IO)
            .onStart { _screenState.value = MainScreenState(loading = true) }
            .onEach { _screenState.value = MainScreenState(list = it) }
            .catch { _screenState.value = MainScreenState(loading = false) }
            .launchIn(viewModelScope)
    }

    fun deleteLocation(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            resultOf { repository.deleteLocation(id) }.onSuccess {
                val locationEntities = _screenState.value.list.toMutableList()
                locationEntities.removeAll { entity -> entity.id == id }
                _screenState.value = MainScreenState(list = locationEntities)
            }
        }
    }
}

data class MainScreenState(
    val loading: Boolean = false, val list: List<LocationEntity> = emptyList()
)
