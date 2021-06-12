package com.group06.lab.profile

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.group06.lab.R
import com.group06.lab.extensions.isInteger
import com.group06.lab.extensions.toString
import java.text.DecimalFormat
import java.util.*

class RatingAdapter() : RecyclerView.Adapter<RatingAdapter.RatingViewHolder>() {

    var data: List<Rating> = ArrayList()

    constructor(data: List<Rating>) : this() {
        this.data = data
    }

    class RatingViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val ratingBar: RatingBar = v.findViewById(R.id.ratingBar)
        val emailText: TextView = v.findViewById(R.id.emailText)
        val dateText: TextView = v.findViewById(R.id.dateText)
        val messageText: TextView = v.findViewById(R.id.messageText)

        fun bind(r: Rating) {
            ratingBar.rating = r.score
            emailText.text = r.userMail
            dateText.text = r.createdDate.toString("dd/MM/yyyy hh:mm")
            messageText.text = r.message
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RatingViewHolder {
        return RatingViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.rating_list_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RatingViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }
}

