package com.garlicbread.gofish.data

import com.garlicbread.gofish.room.entity.FishEntity

fun FishDetails.asFishEntity(): FishEntity {
    return FishEntity(
        confidence = confidence,
        name = details.name,
        scientificName = details.scientificName,
        appearanceAndAnatomy = details.appearanceAndAnatomy,
        coloration = details.color.coloration,
        dominantColour = details.color.dominantColor,
        commonNames = details.commonNames.joinToString(","),
        diet = details.feedingAndFoodValue.diet,
        foodValue = details.feedingAndFoodValue.foodValue,
        healthWarnings = details.feedingAndFoodValue.healthWarnings,
        depthRange = details.habitatAndRange.depthRange,
        distribution = details.habitatAndRange.distribution,
        habitat = details.habitatAndRange.habitat,
        conservationStatus = details.handlingAndConservation.conservationStatus,
        handlingTip = details.handlingAndConservation.handlingTip,
        imageUrl = details.imageUrl,
        recordCatchAngler = details.recordCatch.angler,
        recordCatchDate = details.recordCatch.date,
        recordCatchLocation = details.recordCatch.location,
        recordCatchType = details.recordCatch.type,
        recordCatchWeight = details.recordCatch.weight,
        commonLength = details.sizeAndLifespan.commonLength,
        lifespan = details.sizeAndLifespan.lifespan,
        maximumLength = details.sizeAndLifespan.maximumLength,
        reproduction = details.sizeAndLifespan.reproduction,
        weightRecord = details.sizeAndLifespan.weightRecord,
        taxonomyClass = details.taxonomy.className,
        taxonomyFamily = details.taxonomy.family,
        taxonomyGenus = details.taxonomy.genus,
        taxonomyKingdom = details.taxonomy.kingdom,
        taxonomyOrder = details.taxonomy.order,
        taxonomyPhylum = details.taxonomy.phylum
    )
}