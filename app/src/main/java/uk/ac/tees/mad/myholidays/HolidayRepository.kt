package uk.ac.tees.mad.myholidays

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uk.ac.tees.mad.myholidays.data.HolidayDao
import uk.ac.tees.mad.myholidays.data.HolidayEntity
import uk.ac.tees.mad.myholidays.models.CalendarificApiService
import uk.ac.tees.mad.myholidays.models.Holiday
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class HolidayRepository(
    private val holidayDao: HolidayDao,
    private val apiService: CalendarificApiService,
) {
    private val CACHE_TIMEOUT = TimeUnit.HOURS.toMillis(24) // 24 hour cache

    fun getUpcomingHolidays(countryCode: String): Flow<List<Holiday>> {
        return holidayDao.getUpcomingHolidays(countryCode, LocalDate.now())
            .map { entities ->
                entities.map { it.toHoliday() }
            }
    }

    suspend fun refreshHolidays(countryCode: String) {
        try {
            val lastUpdate = holidayDao.getLastUpdateTime(countryCode)
            val shouldUpdate = lastUpdate == null ||
                    System.currentTimeMillis() - lastUpdate > CACHE_TIMEOUT

            if (shouldUpdate) {
                val currentYear = LocalDate.now().year

                // Fetch current year holidays
                val currentYearHolidays = fetchYearHolidays(countryCode, currentYear)

                // Fetch next year holidays
                val nextYearHolidays = fetchYearHolidays(countryCode, currentYear + 1)

                // Combine and save to database
                val allHolidays = (currentYearHolidays + nextYearHolidays)
                    .distinctBy { it.name }
                    .map { it.toEntity(countryCode) }

                holidayDao.deleteHolidaysForCountry(countryCode)
                holidayDao.insertHolidays(allHolidays)
            }
        } catch (e: Exception) {
            throw e
        }
    }

    private suspend fun fetchYearHolidays(countryCode: String, year: Int): List<Holiday> {
        return apiService.getHolidays(
            apiKey = "HRMbNEQ8AOK8rGpE9CevlWZK3P88dFFJ",
            country = countryCode,
            year = year
        ).response.holidays
            .map { data ->
                Holiday(
                    id = data.hashCode().toString(),
                    name = data.name,
                    date = LocalDate.of(
                        data.date.datetime.year,
                        data.date.datetime.month,
                        data.date.datetime.day
                    ),
                    country = countryCode,
                    description = data.description,
                    type = data.type?.firstOrNull() ?: "National"
                )
            }
            .filter { it.date.isAfter(LocalDate.now()) }
            .sortedBy { it.date }
    }


}

private fun HolidayEntity.toHoliday() = Holiday(
    id = id,
    name = name,
    date = date,
    country = country,
    description = description,
    type = type,

    )

private fun Holiday.toEntity(countryCode: String) = HolidayEntity(
    id = id,
    name = name,
    date = date,
    country = countryCode,
    description = description,
    type = type
)