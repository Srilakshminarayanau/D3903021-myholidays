package uk.ac.tees.mad.myholidays.models

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import com.google.gson.annotations.SerializedName

data class CalendarificResponse(
    @SerializedName("response") val response: HolidayResponse
)

data class HolidayResponse(
    @SerializedName("holidays") val holidays: List<HolidayData>
)

data class HolidayData(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("date") val date: HolidayDate,
    @SerializedName("type") val type: List<String>? = null,
    @SerializedName("locations") val locations: String? = null
)

data class HolidayDate(
    @SerializedName("iso") val isoDate: String,
    @SerializedName("datetime") val datetime: DateTimeDetails
)

data class DateTimeDetails(
    @SerializedName("year") val year: Int,
    @SerializedName("month") val month: Int,
    @SerializedName("day") val day: Int
)

interface CalendarificApiService {
    @GET("holidays")
    suspend fun getHolidays(
        @Query("api_key") apiKey: String,
        @Query("country") country: String,
        @Query("year") year: Int,
        @Query("type") type: String = "national,religious"
    ): CalendarificResponse
}
