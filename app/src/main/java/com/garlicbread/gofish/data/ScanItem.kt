package com.garlicbread.gofish.data

data class ScanItem(
    val id: Long,
    val imageUrl: String,
    val name: String,
    val confidence: Double
)
