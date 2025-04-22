package com.garlicbread.gofish.util

import android.content.res.Resources
import com.garlicbread.gofish.R

class Utils {

    companion object {
        fun getConfidenceColor(confidence: Double, resources: Resources): Int {
            return if (confidence > 80) resources.getColor(R.color.very_high_confidence, null)
            else if (confidence > 60) resources.getColor(R.color.high_confidence, null)
            else if (confidence > 40) resources.getColor(R.color.low_confidence, null)
            else resources.getColor(R.color.very_low_confidence, null)
        }

        fun String.toTitleCase(): String {
            return split(" ").joinToString(" ") {
                it.lowercase().replaceFirstChar { char -> char.titlecase() }
            }
        }
    }
}