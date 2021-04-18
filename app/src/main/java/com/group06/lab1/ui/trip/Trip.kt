package com.group06.lab1.ui.trip

import java.util.*

data class Trip(val imageUrl: String, val departure:String, val arrival:String,
                val departureDate: Date, val estimatedDuration: Int,
                val availableSeats: Int, val price: Double, val description: String)
