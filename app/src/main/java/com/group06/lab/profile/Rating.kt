package com.group06.lab.profile

import java.util.*

class Rating (var role: String, var score: Float, var tripId: String, var message: String, var userMail: String, var createdDate: Date){
    constructor(): this("", -1f, "", "", "", Date())
}
