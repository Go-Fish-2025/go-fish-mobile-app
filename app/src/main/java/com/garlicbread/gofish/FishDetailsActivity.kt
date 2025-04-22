package com.garlicbread.gofish

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.garlicbread.gofish.room.GoFishDatabase
import com.garlicbread.gofish.room.repository.FishRepository
import com.garlicbread.gofish.util.Utils.Companion.getConfidenceColor
import com.garlicbread.gofish.util.Utils.Companion.toTitleCase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FishDetailsActivity : AppCompatActivity() {

    private val TAG = "FishDetailsActivity"

    private lateinit var fishRepository: FishRepository

    @SuppressLint("SetTextI18n", "CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fish_details)

        val fishDao = GoFishDatabase.getDatabase(applicationContext).fishDao()
        fishRepository = FishRepository(fishDao)

        val fishId = intent.getLongExtra("FISH_ID", -1L)
        if (fishId == -1L) {
            Log.e(TAG, "No fish_id provided in intent")
            Toast.makeText(this, "Error: No data found !!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupSectionToggleListeners()

        lifecycleScope.launch {
            fishRepository.getFishById(fishId).collectLatest { fish ->
                if (fish != null) {
                    Log.d(TAG, "Loaded fish: ${fish.name}, ID: ${fish.id}")

                    Glide.with(this@FishDetailsActivity)
                        .load(fish.imageUrl)
                        .apply(RequestOptions().transform(RoundedCorners(60)))
                        .placeholder(R.drawable.fishing)
                        .error(R.drawable.fishing)
                        .into(findViewById(R.id.img_fish))

                    findViewById<ProgressBar>(R.id.progressBar).progress = (fish.confidence * 100).toInt()
                    findViewById<ProgressBar>(R.id.progressBar).progressTintList =
                        ColorStateList.valueOf(getConfidenceColor(fish.confidence * 100, resources))

                    findViewById<TextView>(R.id.tv_name).text = fish.name.toTitleCase()

                    findViewById<TextView>(R.id.tv_scientific_name).text = fish.scientificName
                    findViewById<TextView>(R.id.tv_appearance).apply {
                        text = Html.fromHtml(fish.appearanceAndAnatomy, Html.FROM_HTML_MODE_COMPACT)
                    }

                    findViewById<TextView>(R.id.tv_common_names).text = fish.commonNames.replace(",", "\n")

                    findViewById<View>(R.id.tv_dominant_colour).backgroundTintList =
                        ColorStateList.valueOf(fish.dominantColour.toColorInt())
                    findViewById<TextView>(R.id.tv_desc).apply {
                        text = Html.fromHtml(fish.coloration, Html.FROM_HTML_MODE_COMPACT)
                    }

                    findViewById<TextView>(R.id.tv_diet).apply {
                        text = Html.fromHtml(fish.diet, Html.FROM_HTML_MODE_COMPACT)
                    }
                    findViewById<TextView>(R.id.tv_food_value).apply {
                        text = Html.fromHtml(fish.foodValue, Html.FROM_HTML_MODE_COMPACT)
                    }
                    findViewById<TextView>(R.id.tv_health_warnings).apply {
                        text = Html.fromHtml(fish.healthWarnings, Html.FROM_HTML_MODE_COMPACT)
                    }

                    findViewById<TextView>(R.id.tv_depth_range).text = fish.depthRange
                    findViewById<TextView>(R.id.tv_distribution).apply {
                        text = Html.fromHtml(fish.distribution, Html.FROM_HTML_MODE_COMPACT)
                    }
                    findViewById<TextView>(R.id.tv_habitat).apply {
                        text = Html.fromHtml(fish.habitat, Html.FROM_HTML_MODE_COMPACT)
                    }

                    findViewById<TextView>(R.id.tv_conservation_status).apply {
                        text = Html.fromHtml(fish.conservationStatus, Html.FROM_HTML_MODE_COMPACT)
                    }
                    findViewById<TextView>(R.id.tv_handling_tip).apply {
                        text = Html.fromHtml(fish.handlingTip, Html.FROM_HTML_MODE_COMPACT)
                    }

                    findViewById<TextView>(R.id.tv_record_catch_angler).text = fish.recordCatchAngler
                    findViewById<TextView>(R.id.tv_record_catch_date).text = fish.recordCatchDate
                    findViewById<TextView>(R.id.tv_record_catch_location).text = fish.recordCatchLocation
                    findViewById<TextView>(R.id.tv_record_catch_type).text = fish.recordCatchType
                    findViewById<TextView>(R.id.tv_record_catch_weight).text = fish.recordCatchWeight

                    findViewById<TextView>(R.id.tv_common_length).text = if (fish.commonLength != "Unknown") "${fish.commonLength} cm" else fish.commonLength
                    findViewById<TextView>(R.id.tv_lifespan).text = if (fish.lifespan != "Unknown") "${fish.lifespan} years" else fish.lifespan
                    findViewById<TextView>(R.id.tv_maximum_length).text = if (fish.maximumLength != "Unknown") "${fish.maximumLength} cm" else fish.maximumLength
                    findViewById<TextView>(R.id.tv_reproduction).apply {
                        text = Html.fromHtml(fish.reproduction, Html.FROM_HTML_MODE_COMPACT)
                    }
                    findViewById<TextView>(R.id.tv_weight_record).text = fish.weightRecord

                    findViewById<TextView>(R.id.tv_taxonomy_class).text = fish.taxonomyClass
                    findViewById<TextView>(R.id.tv_taxonomy_family).text = fish.taxonomyFamily
                    findViewById<TextView>(R.id.tv_taxonomy_genus).text = fish.taxonomyGenus
                    findViewById<TextView>(R.id.tv_taxonomy_kingdom).text = fish.taxonomyKingdom
                    findViewById<TextView>(R.id.tv_taxonomy_order).text = fish.taxonomyOrder
                    findViewById<TextView>(R.id.tv_taxonomy_phylum).text = fish.taxonomyPhylum
                } else {
                    Log.e(TAG, "Fish not found for ID: $fishId")
                    Toast.makeText(this@FishDetailsActivity, "Fish not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun setupSectionToggleListeners() {
        // General Information
        findViewById<TextView>(R.id.general_information_title)?.setOnClickListener {
            toggleSectionVisibility(
                listOf(
                    R.id.scientific_name_label, R.id.tv_scientific_name,
                    R.id.appearance_and_anatomy_label, R.id.tv_appearance,
                    R.id.common_names_label, R.id.tv_common_names
                )
            )
        }

        // Colouration
        findViewById<TextView>(R.id.colouration_title)?.setOnClickListener {
            toggleSectionVisibility(
                listOf(
                    R.id.dominant_colour_label, R.id.tv_dominant_colour,
                    R.id.description_label, R.id.tv_desc
                )
            )
        }

        // Feeding and Food Value
        findViewById<TextView>(R.id.feeding_and_food_value_title)?.setOnClickListener {
            toggleSectionVisibility(
                listOf(
                    R.id.diet_label, R.id.tv_diet,
                    R.id.food_value_label, R.id.tv_food_value,
                    R.id.health_warnings_label, R.id.tv_health_warnings
                )
            )
        }

        // Habitat and Range
        findViewById<TextView>(R.id.habitat_and_range_title)?.setOnClickListener {
            toggleSectionVisibility(
                listOf(
                    R.id.depth_range_label, R.id.tv_depth_range,
                    R.id.distribution_label, R.id.tv_distribution,
                    R.id.habitat_label, R.id.tv_habitat
                )
            )
        }

        // Handling and Conservation
        findViewById<TextView>(R.id.handling_and_conservation_title)?.setOnClickListener {
            toggleSectionVisibility(
                listOf(
                    R.id.conservation_status_label, R.id.tv_conservation_status,
                    R.id.handling_tip_label, R.id.tv_handling_tip
                )
            )
        }

        // Size and Lifespan
        findViewById<TextView>(R.id.size_and_lifespan_title)?.setOnClickListener {
            toggleSectionVisibility(
                listOf(
                    R.id.common_length_label, R.id.tv_common_length,
                    R.id.lifespan_label, R.id.tv_lifespan,
                    R.id.maximum_length_label, R.id.tv_maximum_length,
                    R.id.reproduction_label, R.id.tv_reproduction,
                    R.id.weight_record_label, R.id.tv_weight_record
                )
            )
        }

        // Record Catch
        findViewById<TextView>(R.id.record_catch_title)?.setOnClickListener {
            toggleSectionVisibility(
                listOf(
                    R.id.angler_label, R.id.tv_record_catch_angler,
                    R.id.date_label, R.id.tv_record_catch_date,
                    R.id.location_label, R.id.tv_record_catch_location,
                    R.id.type_label, R.id.tv_record_catch_type,
                    R.id.weight_label, R.id.tv_record_catch_weight
                )
            )
        }

        // Taxonomy
        findViewById<TextView>(R.id.taxonomy_title)?.setOnClickListener {
            toggleSectionVisibility(
                listOf(
                    R.id.class_label, R.id.tv_taxonomy_class,
                    R.id.family_label, R.id.tv_taxonomy_family,
                    R.id.genus_label, R.id.tv_taxonomy_genus,
                    R.id.kingdom_label, R.id.tv_taxonomy_kingdom,
                    R.id.order_label, R.id.tv_taxonomy_order,
                    R.id.phylum_label, R.id.tv_taxonomy_phylum
                )
            )
        }
    }

    private fun toggleSectionVisibility(viewIds: List<Int>) {
        val firstView = findViewById<View>(viewIds.first()) ?: return
        val newVisibility = if (firstView.isVisible) View.GONE else View.VISIBLE
        viewIds.forEach { id ->
            findViewById<View>(id)?.visibility = newVisibility
        }
    }
}