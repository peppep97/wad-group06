package com.group06.lab1.utils

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.group06.lab1.ui.trip.Trip

class Database private constructor (context: Context?) {

    companion object {
        private var INSTANCE: Database? = null

        fun getInstance(context: Context?) =
            INSTANCE
                ?: Database(context)
                    .also { INSTANCE = it }
    }

    private val sharedPreferences = context?.getSharedPreferences("database", Context.MODE_PRIVATE)

    var tripList: ArrayList<Trip> = ArrayList()

    /*init {
        val json = sharedPreferences.getString(KEY_JSON_PREF, null)
        if (json == null) {
            // initialize your list contents for the first time
        } else {
            // convert your json and fill the data into your lists
        }
    }*/
    fun load(){
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
    }
}
