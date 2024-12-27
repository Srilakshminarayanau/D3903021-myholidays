package uk.ac.tees.mad.myholidays.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uk.ac.tees.mad.myholidays.data.CalendarificApiClient
import uk.ac.tees.mad.myholidays.models.CalendarificApiService
import uk.ac.tees.mad.myholidays.models.Holiday
import uk.ac.tees.mad.myholidays.models.HolidaysState
import uk.ac.tees.mad.myholidays.models.LocationResult
import uk.ac.tees.mad.myholidays.utils.LocationService
import java.time.LocalDate

class HomeViewModel : ViewModel() {

    private val apiService = CalendarificApiClient.apiService

    private val _holidaysState = MutableStateFlow<HolidaysState>(HolidaysState.Initial)
    val holidaysState: StateFlow<HolidaysState> = _holidaysState.asStateFlow()

    private val _locationState = MutableStateFlow<LocationResult?>(null)
    val locationState: StateFlow<LocationResult?> = _locationState.asStateFlow()

    // Manually selected country
    private val _selectedCountry = MutableStateFlow<String?>(null)
    val selectedCountry: StateFlow<String?> = _selectedCountry.asStateFlow()

    fun fetchLocation(locationService: LocationService) {
        viewModelScope.launch {
            val result = locationService.getCurrentLocation()
            _locationState.value = result
            Log.d("HomeViewModel", "Location result: ${_locationState.value}")
            // If location is successfully retrieved then fetch holidays
            if (result is LocationResult.Success) {
                fetchHolidays(result.countryCode)
            }
        }
    }

    fun setManualCountry(countryCode: String) {
        _selectedCountry.value = countryCode
        fetchHolidays(countryCode)
    }

    private fun fetchHolidays(countryCode: String) {
        viewModelScope.launch {
            _holidaysState.value = HolidaysState.Loading

            try {
                val currentYear = LocalDate.now().year
                val response = apiService.getHolidays(
                    apiKey = "HRMbNEQ8AOK8rGpE9CevlWZK3P88dFFJ",
                    country = countryCode,
                    year = currentYear
                )

                val uiHolidays = response.response.holidays.map { apiHoliday ->
                    Holiday(
                        id = apiHoliday.hashCode().toString(),
                        name = apiHoliday.name,
                        date = LocalDate.of(
                            apiHoliday.date.datetime.year,
                            apiHoliday.date.datetime.month,
                            apiHoliday.date.datetime.day
                        ),
                        country = countryCode,
                        description = apiHoliday.description,
                        type = apiHoliday.type?.firstOrNull() ?: "National"
                    )
                }.filter { it.date.isAfter(LocalDate.now()) }
                    .sortedBy { it.date }
                    .distinctBy { it.name }

                val nextYearHolidayResponse = apiService.getHolidays(
                    apiKey = "HRMbNEQ8AOK8rGpE9CevlWZK3P88dFFJ",
                    country = countryCode,
                    year = currentYear + 1
                )
                val nextHolidays = nextYearHolidayResponse.response.holidays.map { apiHoliday ->
                    Holiday(
                        id = apiHoliday.hashCode().toString(),
                        name = apiHoliday.name,
                        date = LocalDate.of(
                            apiHoliday.date.datetime.year,
                            apiHoliday.date.datetime.month,
                            apiHoliday.date.datetime.day
                        ),
                        country = countryCode,
                        description = apiHoliday.description,
                        type = apiHoliday.type?.firstOrNull() ?: "National"
                    )
                }.filter { it.date.isAfter(LocalDate.now()) }
                    .sortedBy { it.date }
                    .distinctBy { it.name }

                Log.d("HomeViewModel", "Fetched holidays: $uiHolidays")
                _holidaysState.value = HolidaysState.Success(uiHolidays + nextHolidays)
            } catch (e: Exception) {
                _holidaysState.value = HolidaysState.Error(e.message ?: "Unknown error")
            }
        }
    }
}