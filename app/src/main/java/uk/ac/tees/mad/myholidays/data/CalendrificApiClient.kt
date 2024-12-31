package uk.ac.tees.mad.myholidays.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import uk.ac.tees.mad.myholidays.models.CalendarificApiService

object CalendarificApiClient {
    private const val BASE_URL = "https://calendarific.com/api/v2/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: CalendarificApiService = retrofit.create(CalendarificApiService::class.java)
}