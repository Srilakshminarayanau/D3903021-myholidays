package uk.ac.tees.mad.myholidays.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.myholidays.models.LocationResult
import uk.ac.tees.mad.myholidays.utils.LocationService


class HomeViewModel : ViewModel() {
    private val _locationState = MutableStateFlow<LocationResult?>(null)
    val locationState: StateFlow<LocationResult?> = _locationState.asStateFlow()

    fun fetchLocation(locationService: LocationService) {
        viewModelScope.launch {
            val result = locationService.getCurrentLocation()
            _locationState.value = result
            Log.d("HomeViewModel", "Location fetched: $result")
        }
    }
}