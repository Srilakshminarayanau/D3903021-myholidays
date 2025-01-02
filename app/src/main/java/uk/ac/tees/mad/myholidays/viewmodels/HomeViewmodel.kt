package uk.ac.tees.mad.myholidays.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.myholidays.HolidayRepository
import uk.ac.tees.mad.myholidays.data.CalendarificApiClient
import uk.ac.tees.mad.myholidays.data.HolidayDatabase
import uk.ac.tees.mad.myholidays.models.HolidaysState
import uk.ac.tees.mad.myholidays.models.LocationResult
import uk.ac.tees.mad.myholidays.utils.LocationService

class HomeViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val repository: HolidayRepository

    private val _holidaysState = MutableStateFlow<HolidaysState>(HolidaysState.Initial)
    val holidaysState: StateFlow<HolidaysState> = _holidaysState.asStateFlow()

    private val _locationState = MutableStateFlow<LocationResult?>(null)
    val locationState: StateFlow<LocationResult?> = _locationState.asStateFlow()

    init {
        val database = HolidayDatabase.getDatabase(application)
        repository = HolidayRepository(database.holidayDao(), CalendarificApiClient)
    }

    fun fetchLocation(locationService: LocationService) {
        viewModelScope.launch {
            _holidaysState.value = HolidaysState.Loading
            val result = locationService.getCurrentLocation()
            _locationState.value = result

            if (result is LocationResult.Success) {
                fetchHolidays(result.countryCode)
            }
        }
    }

    private fun fetchHolidays(countryCode: String) {
        viewModelScope.launch {
            try {
                // First try to fetch from API and update cache
                repository.refreshHolidays(countryCode)

                // Then start collecting from database
                repository.getUpcomingHolidays(countryCode).collect { holidays ->
                    if (holidays.isEmpty()) {
                        _holidaysState.value = HolidaysState.Error("No holidays found")
                    } else {
                        _holidaysState.value = HolidaysState.Success(holidays)
                    }
                }
            } catch (e: Exception) {
                // If API fails, try to get cached data
                try {
                    repository.getUpcomingHolidays(countryCode).collect { holidays ->
                        if (holidays.isEmpty()) {
                            _holidaysState.value = HolidaysState.Error("No cached holidays found")
                        } else {
                            _holidaysState.value = HolidaysState.Success(holidays)
                        }
                    }
                } catch (e: Exception) {
                    _holidaysState.value = HolidaysState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }
}