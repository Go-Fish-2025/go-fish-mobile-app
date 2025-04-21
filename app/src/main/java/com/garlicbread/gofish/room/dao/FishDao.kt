package com.garlicbread.gofish.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.garlicbread.gofish.room.entity.FishEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FishDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(fish: FishEntity): Long

    @Query("SELECT * FROM fish")
    fun getAllFish(): Flow<List<FishEntity>>

    @Query("SELECT * FROM fish WHERE id = :id")
    fun getFishById(id: Long): Flow<FishEntity?>

    @Query("DELETE FROM fish WHERE id = :id")
    suspend fun deleteById(id: Long)
}