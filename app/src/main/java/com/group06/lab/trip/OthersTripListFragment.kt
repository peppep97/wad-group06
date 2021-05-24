package com.group06.lab.trip

import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.SearchView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.group06.lab.R

class OthersTripListFragment : Fragment() {

    private lateinit var tvEmpty: TextView
    private lateinit var rvTripList: RecyclerView

    var adapter: TripAdapter? = null

    private val vm by viewModels<TripViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)

        return inflater.inflate(R.layout.fragment_others_trip_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val addFab = view.findViewById<FloatingActionButton>(R.id.addFab)

        addFab.setOnClickListener {
            findNavController().navigate(R.id.action_trip_list_to_trip_edit2, Bundle().apply {
                putBoolean("edit", false)
            })
        }

        tvEmpty = view.findViewById(R.id.tvEmpty)
        rvTripList = view.findViewById(R.id.rvTripList)

        rvTripList.layoutManager = LinearLayoutManager(context)

        //showList(Database.getInstance(activity).tripList.size)

        /* val db = FirebaseFirestore.getInstance()
         db.collection("trips")
             .addSnapshotListener { value, error ->
                 if (error != null) throw error
                 Database.getInstance(activity).tripList.clear()
                 for (doc in value!!) {
                     val t = doc.toObject(Trip::class.java)
                     t.id = doc.id
                     Database.getInstance(activity).tripList.add(t)
                 }

                 adapter = TripAdapter(
                     Database.getInstance(activity).tripList,
                     "OtherTrips",
                     parentFragmentManager
                 )

                 showList(Database.getInstance(activity).tripList.size)
             }*/

        vm.getTrips().observe(viewLifecycleOwner, Observer {trips ->
            Log.d("new", "new data " + trips.size)

            adapter = TripAdapter(trips, "OtherTrips", parentFragmentManager)
            rvTripList.adapter = adapter

            showList(trips.size)
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
        inflater.inflate(R.menu.options_menu, menu)
        val searchItem: MenuItem = menu.findItem(R.id.actionSearch)
        val searchView: SearchView = searchItem.actionView as SearchView

        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.queryHint = "Location, Date, Price, ...";

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter?.filter?.filter(newText)
                return false
            }
        })
    }

    private fun showList(size: Int) {
        if (size == 0) {
            rvTripList.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
        } else {
            rvTripList.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE
        }
    }
}