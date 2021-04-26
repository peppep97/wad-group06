package com.group06.lab1.ui.trip

import android.graphics.BitmapFactory
import android.media.Image
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.group06.lab1.utils.Database
import com.group06.lab1.R
import kotlinx.android.synthetic.main.fragment_trip_edit.*
import java.io.File
import java.lang.StringBuilder
import com.group06.lab1.extensions.toString

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private var index: Int? = null

/**
 * A simple [Fragment] subclass.
 * Use the [TripDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TripDetailsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        //Override action of the back button, otherwise the transition defined in mobile_navigation does not occur
        val callback = requireActivity().onBackPressedDispatcher.addCallback(this,
            object: OnBackPressedCallback(true){
                override fun handleOnBackPressed() {
                    findNavController().navigate(R.id.action_trip_details_to_trip_list)
                }
            }
            )






    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_trip_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        index = arguments?.getInt("index")

        val t = Database.getInstance(context).tripList[index!!]

        val tvDepartureLocation = view.findViewById<TextView>(R.id.tvDepartureLocation)
        val tvArrivalLocation = view.findViewById<TextView>(R.id.tvArrivalLocation)
        val tvDepartureDate = view.findViewById<TextView>(R.id.tvDepartureDate)
        val tvEstimatedDuration = view.findViewById<TextView>(R.id.tvEstimatedDuration)
        val tvAvailableSeats = view.findViewById<TextView>(R.id.tvAvailableSeats)
        val tvPrice = view.findViewById<TextView>(R.id.tvPrice)
        val tvDescription = view.findViewById<TextView>(R.id.tvDescription)
        val imgTrip = view.findViewById<ImageView>(R.id.imgTrip)

        tvDepartureLocation.text = t.departure
        tvArrivalLocation.text = t.arrival
        tvDepartureDate.text = t.departureDate.toString("yyyy/MM/dd - HH:mm")
        tvAvailableSeats.text = t.availableSeats.toString()
        tvPrice.text = t.price.toString()
        tvDescription.text = t.description

        val sBuilder = StringBuilder()
        if (t.estimatedDay > 0)
            sBuilder.append(String.format("%dd", t.estimatedDay))
        if (t.estimatedHour > 0)
            sBuilder.append(String.format(" %dh", t.estimatedHour))
        if (t.estimatedMinute > 0)
            sBuilder.append(String.format(" %dm", t.estimatedMinute))
        tvEstimatedDuration.text = sBuilder.toString()

        File(context?.filesDir, t.imageUrl).let {
            if (it.exists()) imgTrip.setImageBitmap(BitmapFactory.decodeFile(it.absolutePath))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.fragment_trip_details, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.edit -> {
                findNavController().navigate(R.id.action_trip_details_to_trip_edit, Bundle().apply {
                    putBoolean("edit", true)
                    putInt("index", index!!)
                })
                return true
            }
            android.R.id.home -> {
                //Handling the toolbar back button
                findNavController().navigate(R.id.action_trip_details_to_trip_list)

                return true


            }
        }

        return false
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TripDetailsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TripDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }





}