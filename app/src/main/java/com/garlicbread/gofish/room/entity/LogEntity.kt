package com.garlicbread.gofish.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fish_logs")
data class LogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val weight: Double,
    val length: Double,
    val desc: String,
    val tags: String,
    val imageUri: String,
    val latitude: Double?,
    val longitude: Double?,
    val timestamp: Long
)