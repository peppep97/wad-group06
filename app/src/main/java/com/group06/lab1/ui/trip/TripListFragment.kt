package com.group06.lab1.ui.trip

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.group06.lab1.R
import com.group06.lab1.utils.Database
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import com.group06.lab1.extensions.toString


/**
 * A simple [Fragment] subclass.
 * Use the [TripListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class TripListFragment : Fragment() {

    private var tripList: ArrayList<Trip> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_trip_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //TODO: set actionbar title

        val addFab = view.findViewById<FloatingActionButton>(R.id.addFab)

        addFab.setOnClickListener {
            findNavController().navigate(R.id.action_trip_list_to_trip_edit, Bundle().apply {
                putBoolean("edit", false)
            })
        }
        tripList = Database.getInstance(context).tripList

        val tvEmpty = view.findViewById<TextView>(R.id.tvEmpty)
        val rvTripList = view.findViewById<RecyclerView>(R.id.rvTripList)
        if(tripList.count() == 0){
            rvTripList.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
        } else {
            rvTripList.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE
        }
        rvTripList.layoutManager = LinearLayoutManager(context)
        rvTripList.adapter = TripAdapter(tripList, parentFragmentManager)

        //used just for testing
        /*addFab.setOnLongClickListener {
            findNavController().navigate(R.id.action_trip_list_to_trip_details, Bundle().apply {
                putInt("index", 0)
            })
            true
        }*/
    }

    class TripAdapter(private val data: List<Trip>, fragmentManager: FragmentManager?) :
        RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

        var fm: FragmentManager? = fragmentManager

        class TripViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val tvDepTime: TextView = v.findViewById<TextView>(R.id.tvDepTime)
            val tvArrTime: TextView = v.findViewById<TextView>(R.id.tvArrTime)
            val tvOrigin: TextView = v.findViewById<TextView>(R.id.tvOrigin)
            val tvDestination: TextView = v.findViewById<TextView>(R.id.tvDestination)
            val tvPrice: TextView = v.findViewById<TextView>(R.id.tvPrice)
            val tvDepDate: TextView = v.findViewById<TextView>(R.id.tvDepDate)
            val tvSeat: TextView = v.findViewById<TextView>(R.id.tvSeat)
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
                tvPrice.text = format.format(t.price).toString() + "€"
                tvSeat.text = t.availableSeats.toString()
                File(imgCar.context?.filesDir, t.imageUrl).let {
                    if (it.exists()) imgCar.setImageBitmap(BitmapFactory.decodeFile(it.absolutePath))
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

        override fun onBindViewHolder(holder: TripAdapter.TripViewHolder, position: Int) {
            holder.bind(data[position])
            holder.cardTrip.setOnClickListener {
                 holder.cardTrip.findNavController().navigate(R.id.action_trip_list_to_trip_details, Bundle().apply {
                    putInt("index", position)
                })
//                val tripDetailsFragment = TripDetailsFragment()
//                fm?.commit {
//                    replace(R.id.nav_host_fragment, tripDetailsFragment)
//                    addToBackStack(tripDetailsFragment.tag)
//                }
            }

            holder.btnEdit.setOnClickListener {
                holder.cardTrip.findNavController().navigate(R.id.action_trip_list_to_trip_edit, Bundle().apply {
                    putInt("index", position)
                    putBoolean("edit", true)
                })
            }
        }

        override fun getItemCount(): Int {
            return data.size
        }
    }

}