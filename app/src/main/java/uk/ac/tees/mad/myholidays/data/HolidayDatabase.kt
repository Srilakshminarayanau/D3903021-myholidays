package uk.ac.tees.mad.myholidays.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [HolidayEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class HolidayDatabase : RoomDatabase() {
    abstract fun holidayDao(): HolidayDao

    companion object {
        @Volatile
        private var INSTANCE: HolidayDatabase? = null

        fun getDatabase(context: Context): HolidayDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HolidayDatabase::class.java,
                    "holiday_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}