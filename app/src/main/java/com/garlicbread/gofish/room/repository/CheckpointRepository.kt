package com.garlicbread.gofish.room.repository

import com.garlicbread.gofish.room.dao.CheckpointDao
import com.garlicbread.gofish.room.entity.CheckpointEntity
import com.garlicbread.gofish.util.Utils.Companion.haversineMiles

class CheckpointRepository(private val dao: CheckpointDao) {

    suspend fun insertCheckpoint(checkpoint: CheckpointEntity) {
        dao.insert(checkpoint)
    }

    suspend fun getAllCheckpoints(): List<CheckpointEntity> {
        return dao.getAll()
    }

    suspend fun getNearestCheckpoint(currentLat: Double, currentLon: Double): CheckpointEntity? {
        val allCheckpoints = dao.getAll()

        return allCheckpoints
            .map { it to haversineMiles(currentLat, currentLon, it.latitude, it.longitude) }
            .filter { it.second <= 10.0 }
            .minByOrNull { it.second }
            ?.first
    }
}
