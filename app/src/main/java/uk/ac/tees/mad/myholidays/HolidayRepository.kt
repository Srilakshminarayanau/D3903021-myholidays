package uk.ac.tees.mad.myholidays

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uk.ac.tees.mad.myholidays.data.CalendarificApiClient
import uk.ac.tees.mad.myholidays.data.HolidayDao
import uk.ac.tees.mad.myholidays.data.HolidayEntity
import uk.ac.tees.mad.myholidays.models.CalendarificApiService
import uk.ac.tees.mad.myholidays.models.Holiday
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class HolidayRepository(
    private val holidayDao: HolidayDao,
    private val apiService: CalendarificApiClient,
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
                val holidays = mutableListOf<Holiday>()

                // Fetch current year holidays
                val currentYearResponse = apiService.apiService.getHolidays(
                    apiKey = "HRMbNEQ8AOK8rGpE9CevlWZK3P88dFFJ",
                    country = countryCode,
                    year = currentYear
                )

                holidays.addAll(currentYearResponse.response.holidays.map {
                    Holiday(
                        id = it.hashCode().toString(),
                        name = it.name,
                        date = LocalDate.of(
                            it.date.datetime.year,
                            it.date.datetime.month,
                            it.date.datetime.day
                        ),
                        country = countryCode,
                        description = it.description,
                        type = it.type?.firstOrNull() ?: "National"
                    )
                })

                // Fetch next year holidays
                val nextYearResponse = apiService.apiService.getHolidays(
                    apiKey = "HRMbNEQ8AOK8rGpE9CevlWZK3P88dFFJ",
                    country = countryCode,
                    year = currentYear + 1
                )

                holidays.addAll(nextYearResponse.response.holidays.map {
                    Holiday(
                        id = it.hashCode().toString(),
                        name = it.name,
                        date = LocalDate.of(
                            it.date.datetime.year,
                            it.date.datetime.month,
                            it.date.datetime.day
                        ),
                        country = countryCode,
                        description = it.description,
                        type = it.type?.firstOrNull() ?: "National"
                    )
                })

                // Filter, sort and make distinct
                val processedHolidays = holidays
                    .filter { it.date.isAfter(LocalDate.now()) }
                    .sortedBy { it.date }
                    .distinctBy { it.name }
                    .map { it.toEntity() }

                // Save to database
                holidayDao.deleteHolidaysForCountry(countryCode)
                holidayDao.insertHolidays(processedHolidays)
            }
        } catch (e: Exception) {
            throw e
        }
    }
}

// Extension functions
private fun Holiday.toEntity() = HolidayEntity(
    id = id,
    name = name,
    date = date,
    country = country,
    description = description,
    type = type,
    lastUpdated = System.currentTimeMillis()
)

private fun HolidayEntity.toHoliday() = Holiday(
    id = id,
    name = name,
    date = date,
    country = country,
    description = description,
    type = type
)