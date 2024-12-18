package uk.ac.tees.mad.myholidays.models

sealed class HolidaysState {
    object Loading : HolidaysState()
    data class Success(val holidays: List<Holiday>) : HolidaysState()
    data class Error(val message: String) : HolidaysState()
}