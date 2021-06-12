package com.group06.lab.trip

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.group06.lab.MainActivity
import com.group06.lab.R
import com.group06.lab.profile.Rating
import java.lang.Error
import java.util.*


class FavoredTripsFragment : Fragment() {

    private lateinit var tvEmpty: TextView
    private lateinit var rvTripList: RecyclerView

    private val vm by viewModels<TripViewModel>()

    var tripId: String? = null
    var availableSeats: Int? = null
    var tripIsComplete: Boolean? = null

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
        tripIsComplete = arguments?.getBoolean("TripIsComplete")

        tvEmpty = view.findViewById(R.id.tvEmpty)
        rvTripList = view.findViewById(R.id.rvTripList)
        if (!tripIsComplete!!){

            vm.getFavoredUsersByTrip(tripId!!)
                .observe(viewLifecycleOwner, androidx.lifecycle.Observer {

                    val favoredTripsUsers = it.map { t -> t.userEmail }.distinct()

                    val usersList: ArrayList<User> = ArrayList()

                    val db = FirebaseFirestore.getInstance()
                    db.collection("users")
                        .addSnapshotListener { value, error ->
                            if (error != null) throw error
                            usersList.clear()
                            for (doc in value!!) {
                                val u = doc.toObject(User::class.java)
                                u.id = doc.id
                                if (favoredTripsUsers.contains(u.email))
                                    usersList.add(u)
                            }
                            showList(usersList.size)

                            val adapter = FavUsersAdapter(requireContext(), usersList, tripId!!, tripIsComplete!!)
                            rvTripList.layoutManager = LinearLayoutManager(context)
                            rvTripList.adapter = adapter
                        }
                })
        }
        else{
            vm.getConfirmedUsersByTrip(tripId!!).observe(viewLifecycleOwner, androidx.lifecycle.Observer {

                val usersList: ArrayList<User> = ArrayList()

                it.forEach {
                    usersList.add(it)
                }

                showList(usersList.size)
                val adapter = FavUsersAdapter(requireContext(), usersList, tripId!!, tripIsComplete!!)
                rvTripList.layoutManager = LinearLayoutManager(context)
                rvTripList.adapter = adapter

            })
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

    class FavUsersAdapter(val context: Context, private val data: List<User>, private val tripId : String, private val tripIsComplete:Boolean) :
        RecyclerView.Adapter<FavUsersAdapter.FavUsersViewHolder>() {

        class FavUsersViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            private val tvNickName: TextView = v.findViewById(R.id.tvNickName)
            val cardUser: CardView = v.findViewById(R.id.cardUser)
            private val imgProfile: ImageView = v.findViewById(R.id.imgProfile)
            val acceptButton: Button = v.findViewById(R.id.acceptUserButton)
            val confirmedTextView: TextView = v.findViewById(R.id.AcceptedTextView)!!

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

            if(tripIsComplete){
                //check if user was already rated
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(data[position].email)
                    .collection("Ratings")
                    .whereEqualTo("role", "Passenger")
                    .whereEqualTo("tripId", tripId)
                    .whereEqualTo("userMail", FirebaseAuth.getInstance().currentUser!!.email!!)
                    .addSnapshotListener { value, error ->
                        if (error != null) throw error
                        if (value != null) {
                            if (value.isEmpty){
                                holder.acceptButton.visibility = View.VISIBLE
                                holder.acceptButton.isEnabled = true

                                holder.acceptButton.text = "Rate"
                            }else{
                                holder.acceptButton.visibility = View.INVISIBLE
                                holder.confirmedTextView.visibility = View.VISIBLE
                                holder.confirmedTextView.text = "Rated"
                            }
                        }
                    }
            }

            else {
                FirebaseFirestore.getInstance().collection("trips").document(tripId)
                    .collection("confirmedUsers").addSnapshotListener { res, error ->
                        if (error != null) throw error
                        if (res != null) {

                            if (res.documents.filter { it.get("email") == data[position].email }
                                    .isEmpty()) {

                                holder.acceptButton.visibility = View.VISIBLE
                                holder.acceptButton.isEnabled = true
                            } else {
                                holder.confirmedTextView.visibility = View.VISIBLE
                            }
                        }
                    }
            }

            holder.acceptButton.setOnClickListener {
                if (tripIsComplete){ //rate
                    showRateDialog(data[position].email)
                }else{
                    Toast.makeText(holder.itemView.context, "Confirmed", Toast.LENGTH_LONG).show()
                    val db = FirebaseFirestore.getInstance()

                    db.collection("trips").document(tripId).collection("confirmedUsers")
                        .add(data[position])

                    db.collection("trips")
                        .document(tripId).get().addOnSuccessListener { res ->
                            val trip = res.toObject(Trip::class.java)

                            if (trip?.availableSeats!! > 0) {
                                val db = FirebaseFirestore.getInstance()
                                db.collection("trips")
                                    .document(tripId)
                                    .update("availableSeats", trip.availableSeats.minus(1))
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
                            } else {
                                Toast.makeText(
                                    holder.itemView.context,
                                    "No more seats available",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                }
            }

            holder.cardUser.setOnClickListener {
                holder.cardUser.findNavController()
                    .navigate(R.id.action_favored_trip_list_to_show_profile, Bundle().apply {
                        putString("email", data[position].email)
                    })
            }
        }

        override fun getItemCount(): Int {
            return data.size
        }

        private fun showRateDialog(userEmail: String){
            val builder = AlertDialog.Builder(context)

            val mView: View = LayoutInflater.from(context)
                .inflate(R.layout.rating_dialog, null)

            val messageInput: TextInputLayout = mView.findViewById(R.id.commentRating)
            val ratingBar: RatingBar = mView.findViewById(R.id.ratingbarProfile)

            builder.setTitle("Rate User")
            builder.setView(mView)

            builder.setPositiveButton("Rate") { _, _ ->
                //Send Rating
                val db = FirebaseFirestore.getInstance()

                val dataToUser = Rating("Passenger", ratingBar.rating, com.group06.lab.trip.tripId!!,
                    messageInput.editText?.text.toString(), MainActivity.mAuth.currentUser!!.email!!, Date())

                db.collection("users").document(userEmail)
                    .collection("Ratings").add(dataToUser)
                    .addOnSuccessListener {
                        Toast.makeText(context, "User rated successfully", Toast.LENGTH_SHORT).show()
                    }
            }
            builder.show()
        }
    }
}