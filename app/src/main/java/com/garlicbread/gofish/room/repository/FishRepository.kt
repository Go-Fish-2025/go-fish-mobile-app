package com.garlicbread.gofish.room.repository

import com.garlicbread.gofish.data.ScanItem
import com.garlicbread.gofish.room.dao.FishDao
import com.garlicbread.gofish.room.entity.FishEntity
import kotlinx.coroutines.flow.Flow

class FishRepository(private val fishDao: FishDao) {

    suspend fun insertFish(fish: FishEntity): Long {
        return fishDao.insert(fish)
    }

    fun getAllFish(): Flow<List<FishEntity>> {
        return fishDao.getAllFish()
    }

    fun getHistory(): Flow<List<ScanItem>> {
        return fishDao.getHistory()
    }

    fun getFishById(id: Long): Flow<FishEntity?> {
        return fishDao.getFishById(id)
    }

    suspend fun deleteFish(id: Long) {
        fishDao.deleteById(id)
    }
}