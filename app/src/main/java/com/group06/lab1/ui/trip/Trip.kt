package com.group06.lab1.ui.trip

import java.util.*

class Trip(val imageUrl: String, val departure:String, val arrival:String,
                val departureDate: Date, val estimatedDay: Int, val estimatedHour: Int, val estimatedMinute: Int,
                val availableSeats: Int, val price: Double, val description: String){
    constructor() : this("","","",Date(),0,0,0,0,0.0,"")

}