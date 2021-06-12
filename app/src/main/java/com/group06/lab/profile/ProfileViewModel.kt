package com.group06.lab.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ProfileViewModel: ViewModel(){
    fun getRatingsByRole(userMail: String, role: String) : LiveData<List<Rating>> {

        val data = MutableLiveData<List<Rating>>()

        FirebaseFirestore.getInstance().collection("users")
            .document(userMail)
            .collection("Ratings")
            .whereEqualTo("role", role)
            .orderBy("createdDate", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->

                if(error != null) throw error
                data.value = value?.toObjects(Rating::class.java)

            }
        return data
    }
}