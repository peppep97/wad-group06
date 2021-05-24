package com.group06.lab.utils

import android.content.Context
import com.group06.lab.trip.FavoriteTrip
import com.group06.lab.trip.Trip

class Database private constructor (context: Context?) {

    companion object {
        private var INSTANCE: Database? = null

        fun getInstance(context: Context?) =
            INSTANCE
                ?: Database(context)
                    .also { INSTANCE = it }
    }

    var tripList: ArrayList<Trip> = ArrayList()
    var myTripList: ArrayList<Trip> = ArrayList()
    var favoredList: ArrayList<FavoriteTrip> = ArrayList()

    /*init {
        val json = sharedPreferences.getString(KEY_JSON_PREF, null)
        if (json == null) {
            // initialize your list contents for the first time
        } else {
            // convert your json and fill the data into your lists
        }
    }*/
    /*fun load(){
        val data = sharedPreferences?.getString("database", null)
        if (data != null){
            val sType = object : TypeToken<List<Trip>>() { }.type
            tripList = Gson().fromJson<ArrayList<Trip>>(data, sType)
        }
    }

    fun save(){
        with(sharedPreferences?.edit()) {
            this?.putString("database", Gson().toJson(tripList))
            this?.apply()
        }
    }*/
}
