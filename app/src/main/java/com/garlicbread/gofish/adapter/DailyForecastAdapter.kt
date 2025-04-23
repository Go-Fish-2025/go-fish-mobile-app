package com.garlicbread.gofish.adapter

import DailyForecast
import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.garlicbread.gofish.R
import com.garlicbread.gofish.util.Utils.Companion.formatPrecipitation
import com.garlicbread.gofish.util.Utils.Companion.formatTemperature
import com.garlicbread.gofish.util.Utils.Companion.formatTime
import com.garlicbread.gofish.util.Utils.Companion.formatWind
import com.garlicbread.gofish.util.Utils.Companion.getStormIndicator
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale


class DailyForecastAdapter(
    private var forecast: List<DailyForecast>,
    private val resources: Resources
) : RecyclerView.Adapter<DailyForecastAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.main_icon)
        val date: TextView = view.findViewById(R.id.date)
        val temp: TextView = view.findViewById(R.id.temp)
        val sunrise: TextView = view.findViewById(R.id.sunrise)
        val sunset: TextView = view.findViewById(R.id.sunset)
        val rain: TextView = view.findViewById(R.id.rain)
        val wind: TextView = view.findViewById(R.id.wind)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.daily_forecast_item, parent, false)
        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = forecast[position]

        holder.icon.setImageDrawable(getStormIndicator(data.stormAlert, resources))
        holder.date.text = formatDate(data.date)

        val minF = formatTemperature(data.temperatureMin)
        val maxF = formatTemperature(data.temperatureMax)
        holder.temp.text = "${maxF}\n${minF}"

        holder.sunrise.text = formatTime(data.sunrise)
        holder.sunset.text = formatTime(data.sunset)
        holder.rain.text = formatPrecipitation(data.precipitationSum)

        holder.wind.text = formatWind(data.windSpeedMax, data.windDirectionDominant)
    }

    override fun getItemCount(): Int = forecast.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newForecast: MutableList<DailyForecast>) {
        newForecast.removeAt(0)
        forecast = newForecast
        notifyDataSetChanged()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun formatDate(dateStr: String): String {
        val inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val outputFormat = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)
        val date = LocalDate.parse(dateStr, inputFormat)
        return date.format(outputFormat)
    }

}