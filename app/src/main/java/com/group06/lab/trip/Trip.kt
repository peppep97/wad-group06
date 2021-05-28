package com.group06.lab.trip

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import java.util.*

class Trip(var id: String, val imageUrl: String, val departure:String, val arrival:String, val completed: Boolean?,
                val departureDate: Date, val estimatedDay: Int, val estimatedHour: Int, val estimatedMinute: Int,
                val availableSeats: Int, val price: Double, val description: String, val userEmail: String){
    constructor() : this("","","","", false ,Date(),0,
        0,0,0,0.0,"", "")

    companion object {
        fun DocumentSnapshot.toTrip(): Trip? {
            try {
                val imageUrl = getString("imageUrl")!!
                val departure = getString("departure")!!
                val arrival = getString("arrival")!!
                val completed = getBoolean("completed")
                val departureDate = getDate("departureDate")!!
                val estimatedDay = getLong("estimatedDay")!!.toInt()
                val estimatedHour = getLong("estimatedHour")!!.toInt()
                val estimatedMinute = getLong("estimatedMinute")!!.toInt()
                val availableSeats = getLong("availableSeats")!!.toInt()
                val price = getDouble("price")!!
                val description = getString("description")!!
                val userEmail = getString("userEmail")!!
                return Trip(id, imageUrl, departure, arrival, completed , departureDate, estimatedDay,
                estimatedHour, estimatedMinute, availableSeats, price, description, userEmail)
            } catch (e: Exception) {
                Log.e(TAG, "Error converting trip", e)
                return null
            }
        }
        private const val TAG = "User"
    }

}