package com.garlicbread.gofish.room.repository

import com.garlicbread.gofish.room.dao.CheckpointDao
import com.garlicbread.gofish.room.entity.CheckpointEntity

class CheckpointRepository(private val dao: CheckpointDao) {

    suspend fun insertCheckpoint(checkpoint: CheckpointEntity) {
        dao.insert(checkpoint)
    }

    suspend fun getAllCheckpoints(): List<CheckpointEntity> {
        return dao.getAll()
    }
}
