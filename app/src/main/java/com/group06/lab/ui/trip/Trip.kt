package com.group06.lab.ui.trip

import java.util.*

class Trip(var id: String, val imageUrl: String, val departure:String, val arrival:String,
                val departureDate: Date, val estimatedDay: Int, val estimatedHour: Int, val estimatedMinute: Int,
                val availableSeats: Int, val price: Double, val description: String, val userEmail: String){
    constructor() : this("","","","",Date(),0,0,0,0,0.0,"", "")

}