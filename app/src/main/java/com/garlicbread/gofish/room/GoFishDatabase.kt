package com.garlicbread.gofish.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.garlicbread.gofish.room.dao.FishDao
import com.garlicbread.gofish.room.entity.FishEntity

@Database(entities = [FishEntity::class], version = 1, exportSchema = false)
abstract class GoFishDatabase : RoomDatabase() {
    abstract fun fishDao(): FishDao

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