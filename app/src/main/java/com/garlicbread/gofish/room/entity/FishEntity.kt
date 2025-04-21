package com.garlicbread.gofish.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fish")
data class FishEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val confidence: Double,
    val name: String,
    val scientificName: String,
    val appearanceAndAnatomy: String,
    val coloration: String,
    val dominantColour: String,
    val commonNames: String, // Store as comma-separated string
    val diet: String,
    val foodValue: String,
    val healthWarnings: String,
    val depthRange: String,
    val distribution: String,
    val habitat: String,
    val conservationStatus: String,
    val handlingTip: String,
    val imageUrl: String,
    val recordCatchAngler: String,
    val recordCatchDate: String,
    val recordCatchLocation: String,
    val recordCatchType: String,
    val recordCatchWeight: String,
    val commonLength: String,
    val lifespan: String,
    val maximumLength: String,
    val reproduction: String,
    val weightRecord: String,
    val taxonomyClass: String,
    val taxonomyFamily: String,
    val taxonomyGenus: String,
    val taxonomyKingdom: String,
    val taxonomyOrder: String,
    val taxonomyPhylum: String
)