package com.garlicbread.gofish.data

import com.google.gson.annotations.SerializedName

data class FishDetails(
    @SerializedName("Confidence") val confidence: Double,
    @SerializedName("Details") val details: FishDetailsInfo
)

data class FishDetailsInfo(
    @SerializedName("appearance_and_anatomy") val appearanceAndAnatomy: String,
    @SerializedName("color") val color: Color,
    @SerializedName("common_names") val commonNames: List<String>,
    @SerializedName("feeding_and_food_value") val feedingAndFoodValue: FeedingAndFoodValue,
    @SerializedName("habitat_and_range") val habitatAndRange: HabitatAndRange,
    @SerializedName("handling_and_conservation") val handlingAndConservation: HandlingAndConservation,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("name") val name: String,
    @SerializedName("record_catch") val recordCatch: RecordCatch,
    @SerializedName("scientific_name") val scientificName: String,
    @SerializedName("size_and_lifespan") val sizeAndLifespan: SizeAndLifespan,
    @SerializedName("taxonomy") val taxonomy: Taxonomy
)

data class Color(
    @SerializedName("coloration") val coloration: String,
    @SerializedName("dominant_color") val dominantColor: String,
)

data class FeedingAndFoodValue(
    @SerializedName("diet") val diet: String,
    @SerializedName("food_value") val foodValue: String,
    @SerializedName("health_warnings") val healthWarnings: String
)

data class HabitatAndRange(
    @SerializedName("depth_range") val depthRange: String,
    @SerializedName("distribution") val distribution: String,
    @SerializedName("habitat") val habitat: String
)

data class HandlingAndConservation(
    @SerializedName("conservation_status") val conservationStatus: String,
    @SerializedName("handling_tip") val handlingTip: String
)

data class RecordCatch(
    @SerializedName("angler") val angler: String,
    @SerializedName("date") val date: String,
    @SerializedName("location") val location: String,
    @SerializedName("type") val type: String,
    @SerializedName("weight") val weight: String
)

data class SizeAndLifespan(
    @SerializedName("common_length") val commonLength: String,
    @SerializedName("lifespan") val lifespan: String,
    @SerializedName("maximum_length") val maximumLength: String,
    @SerializedName("reproduction") val reproduction: String,
    @SerializedName("weight_record") val weightRecord: String
)

data class Taxonomy(
    @SerializedName("class") val className: String,
    @SerializedName("family") val family: String,
    @SerializedName("genus") val genus: String,
    @SerializedName("kingdom") val kingdom: String,
    @SerializedName("order") val order: String,
    @SerializedName("phylum") val phylum: String
)