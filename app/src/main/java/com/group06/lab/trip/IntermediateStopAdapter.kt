package com.group06.lab.trip

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.group06.lab.R
import com.group06.lab.extensions.toString
import java.util.*

class IntermediateStopAdapter() :
    RecyclerView.Adapter<IntermediateStopAdapter.TripViewHolder>() {

    var data: MutableList<IntermediateStop> = ArrayList()

    constructor(
        data: MutableList<IntermediateStop>
    ) : this() {
        this.data = data
    }

    class TripViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val placeTextView: TextView = v.findViewById(R.id.placeTextView)
        private val dateTextView: TextView = v.findViewById(R.id.dateTextView)
        val deleteButton: Button = v.findViewById(R.id.deleteButton)

        fun bind(stop: IntermediateStop) {
            placeTextView.text = stop.place
            dateTextView.text = Date(stop.date).toString("dd MMMM - HH:mm")
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TripViewHolder {
        return TripViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.intermediate_stop_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        holder.bind(data[position])

        holder.deleteButton.setOnClickListener {
            data.removeAt(position)
             notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}

