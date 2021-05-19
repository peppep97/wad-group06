package com.group06.lab.ui.trip

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.group06.lab.MainActivity
import com.group06.lab.R
import com.group06.lab.utils.Database
import java.text.DecimalFormat
import java.util.*
import com.group06.lab.extensions.toString


/**
 * A simple [Fragment] subclass.
 * Use the [TripListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class TripListFragment : Fragment() {

    private lateinit var tvEmpty: TextView
    private lateinit var rvTripList: RecyclerView

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
        val adapter = TripAdapter(Database.getInstance(activity).myTripList,"UserTrips", parentFragmentManager)

        rvTripList.layoutManager = LinearLayoutManager(context)
        rvTripList.adapter = adapter

        //tripList = Database.getInstance(context).tripList

        val db = FirebaseFirestore.getInstance()
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
            }

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