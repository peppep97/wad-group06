package com.group06.lab.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.group06.lab.R


class RatingListFragment : Fragment() {

    private lateinit var tvEmpty: TextView
    private lateinit var rvTripList: RecyclerView
    private lateinit var progressBar : ProgressBar

    var adapter: RatingAdapter? = null

    private val vm by viewModels<ProfileViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rating_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val userMail: String? = arguments?.getString("email")
        val role: String? = arguments?.getString("role")
        val number = arguments?.getInt("number", 0)

        (activity as AppCompatActivity?)!!.supportActionBar!!.title = String.format("%d Ratings", number)

        tvEmpty = view.findViewById(R.id.tvEmpty)
        rvTripList = view.findViewById(R.id.rvTripList)
        progressBar = view.findViewById(R.id.progressBar)

        progressBar.visibility = View.VISIBLE

        rvTripList.layoutManager = LinearLayoutManager(context)

        vm.getRatingsByRole(userMail!!, role!!).observe(viewLifecycleOwner, Observer {ratings ->
            adapter = RatingAdapter(ratings)
            rvTripList.adapter = adapter

            showList(ratings.size)
        })
    }

    private fun showList(size: Int){
        if(size == 0){
            rvTripList.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
        } else {
            rvTripList.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE
        }
        progressBar.visibility = View.GONE
    }
}