package com.garlicbread.gofish.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.garlicbread.gofish.room.dao.CheckpointDao
import com.garlicbread.gofish.room.dao.FishDao
import com.garlicbread.gofish.room.dao.LogDao
import com.garlicbread.gofish.room.dao.WeatherDao
import com.garlicbread.gofish.room.entity.CheckpointEntity
import com.garlicbread.gofish.room.entity.CurrentWeatherEntity
import com.garlicbread.gofish.room.entity.DailyForecastEntity
import com.garlicbread.gofish.room.entity.FishEntity
import com.garlicbread.gofish.room.entity.HourlyForecastEntity
import com.garlicbread.gofish.room.entity.LogEntity
import com.garlicbread.gofish.room.entity.WeatherEntity

@Database(entities = [FishEntity::class, WeatherEntity::class, HourlyForecastEntity::class,
    DailyForecastEntity::class, CurrentWeatherEntity::class, LogEntity::class, CheckpointEntity::class],
    version = 1, exportSchema = false)
abstract class GoFishDatabase : RoomDatabase() {
    abstract fun fishDao(): FishDao
    abstract fun weatherDao(): WeatherDao
    abstract fun logDao(): LogDao
    abstract fun checkpointDao(): CheckpointDao

    companion object {
        @Volatile
        private var INSTANCE: GoFishDatabase? = null

        fun getDatabase(context: Context): GoFishDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GoFishDatabase::class.java,
                    "fish_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}