package com.garlicbread.gofish.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.garlicbread.gofish.FishDetailsActivity
import com.garlicbread.gofish.R
import com.garlicbread.gofish.data.ScanItem
import com.garlicbread.gofish.util.Utils.Companion.getConfidenceColor
import com.garlicbread.gofish.util.Utils.Companion.toTitleCase


class FishScanAdapter(
    private var scans: List<ScanItem>,
    private val context: Context,
    private val resources: Resources
) : RecyclerView.Adapter<FishScanAdapter.ViewHolder>() {

    private val FISH_ID = "FISH_ID"

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val image: ImageView = view.findViewById(R.id.imageView)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
        val card: ConstraintLayout = view.findViewById(R.id.card)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.aqua_scan_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = scans[position].name.toTitleCase()

        holder.progressBar.progress = (scans[position].confidence * 100).toInt()
        holder.progressBar.progressTintList =
            ColorStateList.valueOf(getConfidenceColor(scans[position].confidence * 100, resources))

        Glide.with(context)
            .load(scans[position].imageUrl)
            .apply(RequestOptions().transform(RoundedCorners(100)))
            .placeholder(R.drawable.fish_dp)
            .error(R.drawable.fish_dp)
            .into(holder.image)

        holder.card.setOnClickListener {
            val intent = Intent(context, FishDetailsActivity::class.java)
            intent.putExtra(FISH_ID, scans[position].id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = scans.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newFishScans: List<ScanItem>) {
        scans = newFishScans
        notifyDataSetChanged()
    }
}