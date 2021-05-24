package com.group06.lab.trip

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.group06.lab.R
import com.group06.lab.utils.Database


class TripListFragment : Fragment() {

    private lateinit var tvEmpty: TextView
    private lateinit var rvTripList: RecyclerView

    private val vm by viewModels<TripViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_trip_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val addFab = view.findViewById<FloatingActionButton>(R.id.addFab)

        addFab.setOnClickListener {
            findNavController().navigate(R.id.action_trip_list_to_trip_edit, Bundle().apply {
                putBoolean("edit", false)
            })
        }

        tvEmpty = view.findViewById(R.id.tvEmpty)
        rvTripList = view.findViewById(R.id.rvTripList)

        showList(Database.getInstance(activity).myTripList.size)

        val adapter = TripAdapter(Database.getInstance(activity).myTripList,
            "UserTrips",
            parentFragmentManager
        )

        rvTripList.layoutManager = LinearLayoutManager(context)
        rvTripList.adapter = adapter

        //tripList = Database.getInstance(context).tripList

       /* val db = FirebaseFirestore.getInstance()
        db.collection("trips")
            .addSnapshotListener { value, error ->
                if (error != null) throw error
                Database.getInstance(activity).myTripList.clear()
                for (doc in value!!){
                    val t = doc.toObject(Trip::class.java)
                    t.id = doc.id
                    if (t.userEmail == MainActivity.mAuth.currentUser!!.email!!)
                        Database.getInstance(activity).myTripList.add(t)
                }

                adapter.notifyDataSetChanged()
                showList(Database.getInstance(activity).myTripList.size)
            }*/


        vm.getTrips().observe(viewLifecycleOwner, Observer {
            trips -> Log.d("new", "new data " + trips.size)
        })

        rvTripList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && addFab.visibility == View.VISIBLE) {
                    addFab.hide()
                } else if (dy < 0 && addFab.visibility != View.VISIBLE) {
                    addFab.show()
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
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
}