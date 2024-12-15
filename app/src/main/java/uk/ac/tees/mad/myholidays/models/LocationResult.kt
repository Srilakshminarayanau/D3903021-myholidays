package uk.ac.tees.mad.myholidays.models

sealed class LocationResult {
    data class Success(
        val latitude: Double,
        val longitude: Double,
        val countryCode: String,
        val countryName: String
    ) : LocationResult()

    data class Error(val message: String) : LocationResult()
    object PermissionDenied : LocationResult()
}