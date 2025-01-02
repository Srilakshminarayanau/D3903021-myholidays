package uk.ac.tees.mad.myholidays.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.time.LocalDate

@Entity(tableName = "holidays")
data class HolidayEntity(
    @PrimaryKey val id: String,
    val name: String,
    val date: LocalDate,
    val country: String,
    val description: String? = null,
    val type: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)

// Type Converters for Room
class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate): String = date.toString()

    @TypeConverter
    fun toLocalDate(dateString: String): LocalDate = LocalDate.parse(dateString)
}