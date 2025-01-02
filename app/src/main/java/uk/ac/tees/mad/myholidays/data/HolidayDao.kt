package uk.ac.tees.mad.myholidays.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface HolidayDao {
    @Query("SELECT * FROM holidays WHERE country = :countryCode AND date > :currentDate ORDER BY date ASC")
    fun getUpcomingHolidays(countryCode: String, currentDate: LocalDate): Flow<List<HolidayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolidays(holidays: List<HolidayEntity>)

    @Query("DELETE FROM holidays WHERE country = :countryCode")
    suspend fun deleteHolidaysForCountry(countryCode: String)

    @Query("SELECT MAX(lastUpdated) FROM holidays WHERE country = :countryCode")
    suspend fun getLastUpdateTime(countryCode: String): Long?
}