package com.garlicbread.gofish.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.garlicbread.gofish.room.entity.CheckpointEntity

@Dao
interface CheckpointDao {

    @Insert
    suspend fun insert(checkpoint: CheckpointEntity)

    @Query("SELECT * FROM checkpoints ORDER BY title ASC")
    suspend fun getAll(): List<CheckpointEntity>
}
