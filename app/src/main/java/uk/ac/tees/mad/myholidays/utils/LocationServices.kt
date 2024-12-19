package uk.ac.tees.mad.myholidays.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat.isLocationEnabled
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await
import uk.ac.tees.mad.myholidays.models.LocationResult
import java.util.Locale


class LocationService(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    suspend fun getCurrentLocation(): LocationResult {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        //Check gps
        if (!isLocationEnabled(locationManager)) {
            return LocationResult.Error("GPS is disabled")
        }
        
        // Check location permissions
        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return LocationResult.PermissionDenied
        }


        return try {
            val location = fusedLocationClient.lastLocation.await()

            location?.let {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)

                if (!addresses.isNullOrEmpty()) {
                    val countryCode = addresses[0].countryCode
                    val countryName = addresses[0].countryName

                    LocationResult.Success(
                        latitude = it.latitude,
                        longitude = it.longitude,
                        countryCode = countryCode,
                        countryName = countryName
                    )
                } else {
                    LocationResult.Error("Unable to get country information")
                }
            } ?: LocationResult.Error("Location not available")
        } catch (e: Exception) {
            LocationResult.Error(e.message ?: "Unknown error")
        }
    }
}