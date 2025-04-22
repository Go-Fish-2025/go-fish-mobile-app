package com.garlicbread.gofish.components

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import kotlin.math.*
import androidx.core.graphics.withRotation

class CompassView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var currentAzimuth = 0f
    private val animator = ValueAnimator()

    private val paintCircle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 12f
        style = Paint.Style.STROKE
    }

    private val paintTickMajor = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 6f
    }

    private val paintTickMinor = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 2f
    }

    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 60f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private val paintNorthText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        textSize = 60f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private val paintDegreeText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 80f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private val paintArrow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 10f
    }

    private val directions = listOf("N", "E", "S", "W")

    fun updateAzimuth(newAzimuth: Float) {
        animator.cancel()
        animator.setFloatValues(currentAzimuth, newAzimuth)
        animator.duration = 500
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener {
            currentAzimuth = (it.animatedValue as Float) % 360
            invalidate()
        }
        animator.start()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.BLACK)

        val px = width / 2f
        val py = height / 2f
        val radius = min(px, py) - 100f

        canvas.withRotation(-currentAzimuth, px, py) {
            drawCircle(px, py, radius, paintCircle)

            for (i in 0 until 360 step 5) {
                val angle = Math.toRadians(i.toDouble())
                val sin = sin(angle)
                val cos = cos(angle)

                val startR = if (i % 30 == 0) radius - 40f else radius - 20f
                val endR = radius

                val sx = px + startR * sin
                val sy = py - startR * cos
                val ex = px + endR * sin
                val ey = py - endR * cos

                val paint = if (i % 30 == 0) paintTickMajor else paintTickMinor
                drawLine(sx.toFloat(), sy.toFloat(), ex.toFloat(), ey.toFloat(), paint)

                if (i % 90 == 0) {
                    val label = directions[i / 90]
                    val tx = px + (radius - 80f) * sin
                    val ty = py - (radius - 80f) * cos + 20f

                    val textPaint = if (label == "N") paintNorthText else paintText
                    drawText(label, tx.toFloat(), ty.toFloat(), textPaint)
                }
            }

        }

        val arrowLength = radius - 50f
        val baseWidth = 25f
        val path = Path().apply {
            moveTo(px, py - arrowLength)
            lineTo(px - baseWidth, py + 20f)
            lineTo(px + baseWidth, py + 20f)
            close()
        }
        canvas.drawPath(path, paintArrow)
    }
}