package com.group06.lab.trip

import android.util.Log
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import java.util.*


class Trip(@DocumentId val id: String, val imageUrl: String, val departure:String, val arrival:String, val completed: Boolean,
                val departureDate: Date, val estimatedDay: Int, val estimatedHour: Int, val estimatedMinute: Int,
                val availableSeats: Int, val price: Double, val description: String, val userEmail: String, val depPosition: GeoPoint, val arrPosition: GeoPoint,
                val intermediateStops: List<IntermediateStop>){

    constructor() : this("","","","", false ,Date(),0,
        0,0,0,0.0,"", "", GeoPoint(0.0, 0.0), GeoPoint(0.0, 0.0),
                    listOf())
}