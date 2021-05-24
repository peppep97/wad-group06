package com.group06.lab.trip

import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.firestore.FirebaseFirestore
import com.group06.lab.trip.Trip.Companion.toTrip
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TripViewModel : ViewModel() {
    private val trips = MutableLiveData<List<Trip>>()

    init {
        viewModelScope.launch {
            trips.value = loadUsers()
        }
    }

    fun getTrips() : LiveData<List<Trip>> {
        return trips
    }

    fun getTripById(id : String) : LiveData<Trip> {
        val data = MutableLiveData<Trip>()
        FirebaseFirestore.getInstance().collection("trips")
            .document(id)
            .addSnapshotListener { value, error ->
                if (error != null) Log.d("success", error?.message)
                Log.d("success", "success")
                data.value = value?.toTrip()
            }
        return data
    }

    private suspend fun loadUsers() : List<Trip> {
        return FirebaseFirestore.getInstance().collection("trips")
            .get().await()
            .documents.mapNotNull {
                it.toTrip()
            }
    }
}