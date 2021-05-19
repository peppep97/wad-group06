package com.group06.lab.ui.trip

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.group06.lab.R
import com.group06.lab.extensions.isInteger
import com.group06.lab.extensions.toString
import java.text.DecimalFormat
import java.util.*

class TripAdapter() :
    RecyclerView.Adapter<TripAdapter.TripViewHolder>(), Filterable {

    var fm: FragmentManager? = null
    var data: MutableList<Trip> = ArrayList()
    var dataFull: MutableList<Trip> = ArrayList()
    var caller: String = ""

    constructor(
        allData: MutableList<Trip>,
        caller: String,
        fragmentManager: FragmentManager?
    ) : this() {
        this.data = allData
        this.dataFull.addAll(data)
        this.fm = fragmentManager
        this.caller = caller
    }

    class TripViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvDepTime: TextView = v.findViewById(R.id.tvDepTime)
        val tvArrTime: TextView = v.findViewById(R.id.tvArrTime)
        val tvOrigin: TextView = v.findViewById(R.id.tvOrigin)
        val tvDestination: TextView = v.findViewById(R.id.tvDestination)
        val tvPrice: TextView = v.findViewById(R.id.tvPrice)
        val tvDepDate: TextView = v.findViewById(R.id.tvDepDate)
        val tvSeat: TextView = v.findViewById(R.id.tvSeat)
        val cardTrip = v.findViewById<CardView>(R.id.cardTrip)
        val imgCar = v.findViewById<ImageView>(R.id.imgCar)
        val btnEdit = v.findViewById<Button>(R.id.btnEdit)

        fun bind(t: Trip) {
            tvDepTime.text = t.departureDate.toString("HH:mm")
            val calendar = Calendar.getInstance()
            calendar.time = t.departureDate
            calendar.add(Calendar.DAY_OF_MONTH, t.estimatedDay)
            calendar.add(Calendar.HOUR, t.estimatedHour)
            calendar.add(Calendar.MINUTE, t.estimatedMinute)
            tvArrTime.text = calendar.time.toString("HH:mm")
            tvOrigin.text = t.departure
            tvDestination.text = t.arrival
            tvDepDate.text = t.departureDate.toString("MMMM - dd")
            val format = DecimalFormat()
            format.isDecimalSeparatorAlwaysShown = false
            tvPrice.text = String.format("%s €", format.format(t.price).toString())
            tvSeat.text = t.availableSeats.toString()
            if (t.imageUrl == "") {
                imgCar.setImageResource(R.drawable.ic_baseline_no_photography)
            } else {
                Firebase.storage.reference.child(t.imageUrl)
                    .downloadUrl.addOnSuccessListener { uri ->
                        imgCar.load(uri.toString()) {
                            memoryCachePolicy(CachePolicy.DISABLED) //to force reloading when image changes
                        }
                    }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TripAdapter.TripViewHolder {
        return TripViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.trip_list_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        holder.bind(data[position])
        holder.cardTrip.setOnClickListener {
            if (caller == "UserTrips")
                holder.cardTrip.findNavController()
                    .navigate(R.id.action_trip_list_to_trip_details, Bundle().apply {
                        putInt("index", position)
                        putString("tripId", data[position].id)
                        putString("caller", caller)
                    })
            else if (caller == "OtherTrips")
                holder.cardTrip.findNavController()
                    .navigate(R.id.action_othersTripListFragment_to_trip_details, Bundle().apply {
                        putInt("index", position)
                        putString("tripId", data[position].id)
                        putString("caller", caller)
                    })
        }

        if (caller == "UserTrips") {
            holder.btnEdit.visibility = View.VISIBLE
            holder.btnEdit.setOnClickListener {
                holder.cardTrip.findNavController()
                    .navigate(R.id.action_trip_list_to_trip_edit, Bundle().apply {
                        putInt("index", position)
                        putBoolean("edit", true)
                        putString("tripId", data[position].id)
                        putString("caller", caller)
                    })
            }
        } else if (caller == "OtherTrips")
            holder.btnEdit.visibility = View.GONE
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getFilter(): Filter {
        return myFilter
    }

    private var myFilter: Filter = object : Filter() {
        @SuppressLint("DefaultLocale")
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val dataFiltered = mutableListOf<Trip>()
            if (constraint == null || constraint.isEmpty()) {
                dataFiltered.addAll(dataFull)
            } else {
                val filterPattern = constraint.toString().toLowerCase().trim()
                for (item: Trip in dataFull) {
                    if (item.departure.toLowerCase().contains(filterPattern) ||
                        item.arrival.toLowerCase().contains(filterPattern)
                    ) {
                        dataFiltered.add(item)
                    }
                    if (filterPattern.matches(Regex(".*\\d.*")))
                        if (item.departureDate.toString("MMMM dd").toLowerCase()
                                .contains(filterPattern.toLowerCase()) ||
                            item.departureDate.toString("MMMM-dd").toLowerCase()
                                .contains(filterPattern.toLowerCase()) ||
                            item.departureDate.toString("MMMM - dd").toLowerCase()
                                .contains(filterPattern.toLowerCase()) ||
                            item.departureDate.toString("MMMM- dd").toLowerCase()
                                .contains(filterPattern.toLowerCase()) ||
                            item.departureDate.toString("MMMM -dd").toLowerCase()
                                .contains(filterPattern.toLowerCase())
                                ) {
                            dataFiltered.add(item)
                        }

                    if (filterPattern.isInteger(filterPattern)) {
                        if (item.price == filterPattern.toDouble()) {
                            dataFiltered.add(item)
                        }
                    } else if (item.departureDate.toString("MMMM").toLowerCase()
                            .contains(filterPattern.toLowerCase())
                    ) {
                        dataFiltered.add(item)
                    }
                }
            }

            val results = FilterResults()
            results.values = dataFiltered
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            data.clear()
            data.addAll(results?.values as MutableList<Trip>)
            notifyDataSetChanged()
        }
    }
}

