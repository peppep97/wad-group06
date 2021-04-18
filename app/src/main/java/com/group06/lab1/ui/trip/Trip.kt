package com.group06.lab1.ui.trip

import java.util.*

data class Trip(val imageUrl: String, val departure:Int, val arrival:String,
                val departureDate: Date, val estimatedDuration: Int,
                val availableSeats: Int, val price: Int, val description: String)