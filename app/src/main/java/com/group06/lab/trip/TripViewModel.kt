package com.group06.lab.trip

import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.group06.lab.trip.Trip.Companion.toTrip
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TripViewModel : ViewModel() {
    private val trips = MutableLiveData<List<Trip>>()
    private val myTrips = MutableLiveData<List<Trip>>()
    private val favoredTrips = MutableLiveData<List<Trip>>()
    private val boughtTrips = MutableLiveData<List<Trip>>()

    init {
        viewModelScope.launch {
            trips.value = loadTrips()
            myTrips.value = loadMyTrips()
            favoredTrips.value = loadFavoredTrips()
            boughtTrips.value = loadBoughtTrips()
        }
    }

    fun getTrips() : LiveData<List<Trip>> {
        return trips
    }

    fun getMyTrips() : LiveData<List<Trip>> {
        return myTrips
    }

    fun getFavoredTrips() : LiveData<List<Trip>> {
        return favoredTrips
    }

    fun getBoughtTrips() : LiveData<List<Trip>> {
        return boughtTrips
    }


    fun getTripById(id : String) : LiveData<Trip> {
        val data = MutableLiveData<Trip>()
        FirebaseFirestore.getInstance().collection("trips")
            .document(id)
            .addSnapshotListener { value, error ->
                if (error != null) throw error
                data.value = value?.toTrip()
            }
        return data
    }

    fun isAlreadyFavored(tripId : String) : LiveData<Boolean> {
        val data = MutableLiveData<Boolean>()
        FirebaseFirestore.getInstance().collection("favored_trips")
            .whereEqualTo("tripId", tripId)
            .whereEqualTo("userEmail", FirebaseAuth.getInstance().currentUser!!.email!!)
            .get()
            .addOnSuccessListener{
                    value ->
                Log.d("success", value?.isEmpty.toString())
                data.value = value?.isEmpty
            }
        return data
    }


    fun getConfirmedUsersByTrip(tripId: String) : LiveData<List<User>> {

        val data = MutableLiveData<List<User>>()

        FirebaseFirestore.getInstance().collection("trips")
            .document(tripId)
            .collection("confirmedUsers")
            .addSnapshotListener { value, error ->

                if(error != null) throw error
                data.value = value?.toObjects(User::class.java)

            }

        return data

    }




    fun getFavoredUsersByTrip(tripId : String) : LiveData<List<FavoriteTrip>> {
        val data = MutableLiveData<List<FavoriteTrip>>()
        FirebaseFirestore.getInstance().collection("favored_trips")
            .whereEqualTo("tripId", tripId)
            .whereNotEqualTo("userEmail", FirebaseAuth.getInstance().currentUser!!.email!!)
            .addSnapshotListener { value, error ->
                if (error != null) throw error
                data.value = value?.toObjects(FavoriteTrip::class.java)
            }
        return data
    }

    private suspend fun loadFavoredTrips() : List<Trip> {
        val tripsIds: List<String>?

        Log.d("size", "start")
        tripsIds = FirebaseFirestore.getInstance().collection("favored_trips")
            .whereEqualTo("userEmail", FirebaseAuth.getInstance().currentUser!!.email!!)
            .get()
            .await()
            .documents
            .mapNotNull {
                it.toObject(FavoriteTrip::class.java)?.tripId
            }

        if(tripsIds.isEmpty()) return emptyList()
        else
            return FirebaseFirestore.getInstance().collection("trips")
                .whereIn(FieldPath.documentId(), tripsIds)
                .get()
                .await()
                .documents.mapNotNull {
                    it.toTrip()
                }
    }

    private suspend fun loadTrips() : List<Trip> {
        return FirebaseFirestore.getInstance().collection("trips")
            .get().await()
            .documents.mapNotNull {
                it.toTrip()
            }
    }

    private suspend fun loadMyTrips() : List<Trip> {
        return FirebaseFirestore.getInstance().collection("trips")
            .whereEqualTo("userEmail", FirebaseAuth.getInstance().currentUser!!.email!!)
            .get().await()
            .documents.mapNotNull {
                it.toTrip()
            }
    }

    private suspend fun loadBoughtTrips() : List<Trip> {
        val tripList = arrayListOf<Trip>()
        FirebaseFirestore.getInstance().collectionGroup("confirmedUsers")
            .whereEqualTo("email", FirebaseAuth.getInstance().currentUser!!.email!!)
            .get().await()
            .documents.forEach {
                val t = it.reference.parent.parent
                    ?.get()
                    ?.await()
                    ?.toTrip()

                if (t != null) tripList.add(t)
            }
            /*.documents.mapNotNull {
                it.toTrip()
            }*/
        return tripList
    }
}