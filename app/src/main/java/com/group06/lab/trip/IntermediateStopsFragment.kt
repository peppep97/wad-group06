package com.group06.lab.trip

import android.location.Geocoder
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.group06.lab.R

class IntermediateStopsFragment : Fragment() {

    private lateinit var tvEmpty: TextView
    private lateinit var rvTripList: RecyclerView
    private var edit: Boolean? = false
    private var tripId: String? = ""
    private var depLat: Double? = 0.0
    private var arrLat: Double? = 0.0
    private var depLon: Double? = 0.0
    private var arrLon: Double? = 0.0
    private var depCity: String? = ""
    private var arrCity: String? = ""
    private var stopList: MutableList<IntermediateStop>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_intermediate_stops, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        edit = arguments?.getBoolean("edit")
        tripId = arguments?.getString("tripId")
        depLat = arguments?.getDouble("depLat")
        arrLat = arguments?.getDouble("arrLat")
        depLon = arguments?.getDouble("depLon")
        arrLon = arguments?.getDouble("arrLon")
        depCity = arguments?.getString("depCity")
        arrCity = arguments?.getString("arrCity")

        val add = arguments?.getBoolean("add", false)
        val city = arguments?.getString("city")
        val lat = arguments?.getDouble("lat")
        val lon = arguments?.getDouble("lon")
        val date = arguments?.getLong("date")

        stopList = arguments?.getParcelableArrayList<IntermediateStop>("stops")
        if (stopList == null){
            stopList = ArrayList()
        }

        val addFab = view.findViewById<FloatingActionButton>(R.id.addFab)

        addFab.setOnClickListener {
            findNavController().navigate(
                R.id.action_intermediateStopsFragment_to_intermediateLocationSelectorFragment,
                Bundle().apply {
                    putBoolean("isDeparture", true)
                    putBoolean("edit", edit!!)
                    putString("tripId", tripId)
                    depLat?.let { putDouble("depLat", it) }
                    depLon?.let { putDouble("depLon", it) }
                    depCity?.let { putString("depCity", it) }
                    arrLat?.let { putDouble("arrLat", it) }
                    arrLon?.let { putDouble("arrLon", it) }
                    arrCity?.let { putString("arrCity", it) }
                    putParcelableArrayList("stops", stopList as java.util.ArrayList<out Parcelable>?)

                })
        }

        tvEmpty = view.findViewById(R.id.tvEmpty)
        rvTripList = view.findViewById(R.id.rvTripList)

        rvTripList.layoutManager = LinearLayoutManager(context)

        val adapter = IntermediateStopAdapter(stopList!!)
        rvTripList.adapter = adapter

        showList(stopList!!.size)

        if (add!!){
            stopList!!.add(IntermediateStop(city, lat!!, lon!!, date!!))

            showList(stopList!!.size)
            adapter.notifyDataSetChanged()
        }
    }

    private fun showList(size: Int){
        if(size == 0){
            rvTripList.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
        } else {
            rvTripList.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_location_select, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.selectLocation -> {
                findNavController().navigate(
                    R.id.action_intermediateStopsFragment_to_trip_edit,
                    Bundle().apply {
                        putBoolean("isDeparture", true)
                        putBoolean("edit", edit!!)
                        putString("tripId", tripId)
                        depLat?.let { putDouble("depLat", it) }
                        depLon?.let { putDouble("depLon", it) }
                        depCity?.let { putString("depCity", it) }
                        arrLat?.let { putDouble("arrLat", it) }
                        arrLon?.let { putDouble("arrLon", it) }
                        arrCity?.let { putString("arrCity", it) }
                        putParcelableArrayList("stops", stopList as java.util.ArrayList<out Parcelable>?)

                    })
            }
        }
        return false
    }
}