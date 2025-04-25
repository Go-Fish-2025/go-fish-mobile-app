package com.garlicbread.gofish.room.repository

import com.garlicbread.gofish.room.dao.LogDao
import com.garlicbread.gofish.room.entity.LogEntity
import kotlinx.coroutines.flow.Flow

class LogRepository(private val dao: LogDao) {

    suspend fun insertLog(log: LogEntity) {
        dao.insert(log)
    }

    suspend fun getLogById(id: Long): LogEntity? {
        return dao.getLogById(id)
    }

    suspend fun getAllLogs(): List<LogEntity> {
        return dao.getAll()
    }

    fun getLogCount(): Flow<Int> {
        return dao.getLogCount()
    }
}
