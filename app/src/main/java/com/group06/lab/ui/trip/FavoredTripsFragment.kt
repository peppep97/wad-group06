package com.group06.lab.ui.trip

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
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

    public var tripId: String? = null
    public var availableSeats: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favored_trip_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        tripId = arguments?.getString("tripId")
        availableSeats = arguments?.getInt("AvailableSeats")

        tvEmpty = view.findViewById(R.id.tvEmpty)
        rvTripList = view.findViewById(R.id.rvTripList)




//        val fTrips = Database.getInstance(activity).myTripList.map { t -> t.id }
        val favoredTripsUsers =
            Database.getInstance(activity).favoredList.filter { f -> f.tripId == tripId }.map {t -> t.userEmail}.distinct()

        var usersList: ArrayList<User> = ArrayList()

        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .addSnapshotListener { value, error ->
                if (error != null) throw error
                usersList.clear()
                for (doc in value!!){
                    val u = doc.toObject(User::class.java)
                    u.id = doc.id
                    if (favoredTripsUsers.contains(u.email))
                        usersList.add(u)
                }
                showList(usersList.size)
                val adapter = FavUsersAdapter(usersList)
                rvTripList.layoutManager = LinearLayoutManager(context)
                rvTripList.adapter = adapter
            }
//        val tripIds = favoredTrips.map{f -> f.tripId}
//        val allTrips = Database.getInstance(activity).tripList.filter { f -> tripIds.contains(f.id) }




    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.main, menu)
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

    class FavUsersAdapter(
        private val data: List<User>
    ) :
        RecyclerView.Adapter<FavUsersAdapter.FavUsersViewHolder>() {

        class FavUsersViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val tvNickName: TextView = v.findViewById(R.id.tvNickName)
//            val tvEmail: TextView = v.findViewById(R.id.tvEmail)
            val cardUser = v.findViewById<CardView>(R.id.cardUser)
            val imgProfile = v.findViewById<ImageView>(R.id.imgProfile)
            val acceptButton = v.findViewById<Button>(R.id.acceptUserButton)


            fun bind(u: User) {
                tvNickName.text = u.nickName
//                tvEmail.text = u.email

                Firebase.storage.reference
                    .child(u.email).downloadUrl
                    .addOnSuccessListener { uri ->
                        imgProfile.load(uri.toString()) {
                            memoryCachePolicy(CachePolicy.DISABLED) //to force reloading when image changes
                        }
                    }.addOnFailureListener {
                        imgProfile.setImageResource(R.drawable.ic_no_photo)
                    }
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

            holder.acceptButton.setOnClickListener {

//
//
//
//                val db = FirebaseFirestore.getInstance()
//
//                var currentSeatsAvailability = db.collection("trips")
//                    .document(tripId!!).addSnapshotListener{
//                        data, error ->
//                        if(error != null) throw error
//                        if(data != null) {
//                            println("DAI FAMMI VEDERE " + data["availableSeats"].toString().toInt())
//
//                            db.collection("trips")
//                                .document(tripId!!)
//                                .update( "availableSeats" , data["availableSeats"].toString().toInt() - 1 )
//                                .addOnSuccessListener {
//
//                                    Toast.makeText( holder.itemView.context , "Confirmed", Toast.LENGTH_LONG).show()
//
//                                }
//                        }
//                    }
//
//
//
//
//                //Update availability, if the availability != 0
//                db.collection("trips")
//                    .document(tripId!!)
//                    .update( "availableSeats" ,    )
//
            }


            holder.cardUser.setOnClickListener {
                // TODO: go to the profile details of this user



                holder.cardUser.findNavController()
                    .navigate(R.id.action_favored_trip_list_to_show_profile, Bundle().apply {
                        putString("email", data[position].email )


//                        putInt("index", position)
//                        putString("tripId", data[position].id)
//                        putString("caller", caller)
                    })
            }
        }

        override fun getItemCount(): Int {
            return data.size
        }
    }
}

