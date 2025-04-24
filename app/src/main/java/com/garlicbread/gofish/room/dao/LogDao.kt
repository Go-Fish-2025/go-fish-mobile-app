package com.garlicbread.gofish.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.garlicbread.gofish.room.entity.LogEntity

@Dao
interface LogDao {
    @Insert
    suspend fun insert(log: LogEntity)

    @Query("SELECT * FROM fish_logs ORDER BY timestamp DESC")
    suspend fun getAll(): List<LogEntity>

    @Query("SELECT * FROM fish_logs WHERE id = :id")
    suspend fun getLogById(id: Long): LogEntity?

    @Query("SELECT COUNT(*) FROM fish_logs")
    suspend fun getLogCount(): Int
}