package com.group06.lab.trip

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.vipulasri.timelineview.TimelineView
import com.group06.lab.R
import com.group06.lab.extensions.toString
import java.util.*


class TimeLineAdapter() :
    RecyclerView.Adapter<TimeLineAdapter.TripViewHolder>() {

    var data: List<IntermediateStop> = ArrayList()

    constructor(
        data: List<IntermediateStop>
    ) : this() {
        this.data = data.sortedBy { it.date }
    }

    class TripViewHolder(v: View, viewType: Int) : RecyclerView.ViewHolder(v) {
        val placeTextView: TextView = v.findViewById(R.id.placeTextView)
        val dateTextView: TextView = v.findViewById(R.id.dateTextView)
        val mTimelineView: TimelineView = v.findViewById(R.id.timeline)

        init {
            mTimelineView.initLine(viewType)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TripViewHolder {
        return TripViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.timeline_item, parent, false),
            viewType
        )
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val stop = data[position]

        holder.mTimelineView.marker = ContextCompat.getDrawable(holder.itemView.context, R.drawable.ic_marker)
        holder.placeTextView.text = stop.place
        if (stop.estimatedDay == -1 && stop.estimatedHour == -1 && stop.estimatedMinute == -1)
            holder.dateTextView.text = Date(stop.date).toString("dd MMM - HH:mm")
        else{
            if (stop.estimatedDay > 0 || stop.estimatedHour > 0 || stop.estimatedMinute > 0) {
                val sBuilder = StringBuilder()
                if (stop.estimatedDay > 0)
                    sBuilder.append(String.format("%dd", stop.estimatedDay))
                if (stop.estimatedHour > 0)
                    sBuilder.append(String.format(" %dh", stop.estimatedHour))
                if (stop.estimatedMinute > 0)
                    sBuilder.append(String.format(" %dm", stop.estimatedMinute))
                holder.dateTextView.text = "Estimated arrival in ${sBuilder.toString()}"
            }
        }

    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemViewType(position: Int): Int {
        return TimelineView.getTimeLineViewType(position, itemCount)
    }
}

