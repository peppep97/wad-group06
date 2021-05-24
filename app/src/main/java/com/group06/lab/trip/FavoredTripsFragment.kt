package com.group06.lab.trip

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.group06.lab.MainActivity
import com.group06.lab.R
import com.group06.lab.utils.Database
import java.util.*

class FavoredTripsFragment : Fragment() {

    private lateinit var tvEmpty: TextView
    private lateinit var rvTripList: RecyclerView

    var tripId: String? = null
    var availableSeats: Int? = null

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

        val favoredTripsUsers =
            Database.getInstance(activity).favoredList.filter { f -> f.tripId == tripId }.filter{ f -> f.userEmail != MainActivity.mAuth.currentUser!!.email!! }.map {t -> t.userEmail}.distinct()

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
                val adapter =
                    FavUsersAdapter(
                        usersList
                    )
                rvTripList.layoutManager = LinearLayoutManager(context)
                rvTripList.adapter = adapter
            }
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
            val cardUser = v.findViewById<CardView>(R.id.cardUser)
            val imgProfile = v.findViewById<ImageView>(R.id.imgProfile)
            val acceptButton = v.findViewById<Button>(R.id.acceptUserButton)
            val confirmedTextView = v.findViewById<TextView>(R.id.AcceptedTextView)

            fun bind(u: User) {
                tvNickName.text = u.nickName

                acceptButton.visibility = View.GONE
                acceptButton.isEnabled = false

                confirmedTextView.visibility = View.GONE

                Firebase.storage.reference
                    .child(u.email).downloadUrl
                    .addOnSuccessListener { uri ->
                        imgProfile.load(uri.toString()) {
                            memoryCachePolicy(CachePolicy.DISABLED) //to force reloading when image changes
                        }
                    }.addOnFailureListener {
                        imgProfile.setImageResource(R.drawable.ic_baseline_no_photography)
                    }
            }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): FavUsersViewHolder {
            return FavUsersViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.favored_trips_users_item, parent, false)
            )
        }

        override fun onBindViewHolder(holder: FavUsersViewHolder, position: Int) {
            holder.bind(data[position])

            var acceptButtonHide = false

            FirebaseFirestore.getInstance().collection("trips").document(tripId!!)
                .collection("confirmedUsers").addSnapshotListener  {
                        res, error ->
                    if(error != null) throw error
                    if(res != null) {
                        println("CONFIRMEDUSERS" + res.documents.filter { it.get("email") == data[position].email })
                        println("CHECKISNULL" + res.documents.filter { it.get("email") == data[position].email }
                            .isEmpty())
                        if (res.documents.filter { it.get("email") == data[position].email }
                                .isEmpty()) {
                            println("HIDEBUTTON")

                            holder.acceptButton.visibility = View.VISIBLE
                            holder.acceptButton.isEnabled = true

                            acceptButtonHide = true
                        }
                        else{
                            holder.confirmedTextView.visibility = View.VISIBLE
                        }
                    }
                }

            holder.acceptButton.setOnClickListener {
                Toast.makeText( holder.itemView.context , "Confirmed", Toast.LENGTH_LONG).show()
                val db = FirebaseFirestore.getInstance()

                db.collection("trips").document(tripId!!).collection("confirmedUsers").add(  data[position] )

                var currentSeatsAvailability = db.collection("trips")
                    .document(tripId!!).get().addOnSuccessListener {
                            res ->
                        val Trip = res.toObject(Trip::class.java)
                        println("HERE IS THE TRIP " + Trip?.availableSeats)


                        if(Trip?.availableSeats!! > 0) {
                            val db = FirebaseFirestore.getInstance()
                            db.collection("trips")
                                .document(tripId!!)
                                .update("availableSeats", Trip?.availableSeats?.minus(1))
                                .addOnSuccessListener {

                                    Toast.makeText(
                                        holder.itemView.context,
                                        "Confirmed",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    holder.acceptButton.visibility = View.GONE
                                    holder.acceptButton.isEnabled = false
                                    holder.confirmedTextView.visibility = View.VISIBLE

                                }
                        }
                        else{
                            Toast.makeText(
                                holder.itemView.context,
                                "No more seats available",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

            }

            holder.cardUser.setOnClickListener {
                // TODO: go to the profile details of this user

                holder.cardUser.findNavController()
                    .navigate(R.id.action_favored_trip_list_to_show_profile, Bundle().apply {
                        putString("email", data[position].email )
                    })
            }
        }

        override fun getItemCount(): Int {
            return data.size
        }
    }
}

