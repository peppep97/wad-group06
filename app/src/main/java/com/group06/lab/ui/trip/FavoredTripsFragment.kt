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
import com.group06.lab.extensions.toString
import com.group06.lab.utils.Database
import java.text.DecimalFormat
import java.util.*

class FavoredTripsFragment : Fragment() {

    private lateinit var tvEmpty: TextView
    private lateinit var rvTripList: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favored_trip_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        tvEmpty = view.findViewById(R.id.tvEmpty)
        rvTripList = view.findViewById(R.id.rvTripList)

        val fTrips = Database.getInstance(activity).myTripList.map { t -> t.id }
        val favoredTrips =
            Database.getInstance(activity).favoredList.filter { f -> fTrips.contains(f.tripId) }

//        val tripIds = favoredTrips.map{f -> f.tripId}
//        val allTrips = Database.getInstance(activity).tripList.filter { f -> tripIds.contains(f.id) }
        showList(favoredTrips.size)
        val adapter = FavUsersAdapter(favoredTrips)

        rvTripList.layoutManager = LinearLayoutManager(context)
        rvTripList.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.main, menu)
    }

    fun showList(size: Int) {
        if (size == 0) {
            rvTripList.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
        } else {
            rvTripList.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE
        }
    }

    class FavUsersAdapter(
        private val data: List<FavoriteTrip>
    ) :
        RecyclerView.Adapter<FavUsersAdapter.FavUsersViewHolder>() {

        class FavUsersViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val tvUserEmail: TextView = v.findViewById(R.id.tvUserEmail)
            val btnShowTrip: Button = v.findViewById(R.id.btnShowTrip)
            val cardTrip = v.findViewById<CardView>(R.id.cardTrip)


            fun bind(t: FavoriteTrip) {
                tvUserEmail.text = t.userEmail
                btnShowTrip.tag = t.tripId
            }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): FavUsersAdapter.FavUsersViewHolder {
            return FavUsersViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.favored_trips_users_item, parent, false)
            )
        }

        override fun onBindViewHolder(holder: FavUsersAdapter.FavUsersViewHolder, position: Int) {
            holder.bind(data[position])
            holder.btnShowTrip.setOnClickListener {
                holder.cardTrip.findNavController()
                    .navigate(R.id.action_favored_trip_list_to_trip_details, Bundle().apply {
                        putInt("index", position)
                        putString("tripId", data[position].tripId)
                        putString("caller", "favoredTrips")
                    })
            }
        }

        override fun getItemCount(): Int {
            return data.size
        }
    }
}

