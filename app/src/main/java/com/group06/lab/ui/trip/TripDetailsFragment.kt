package com.group06.lab.ui.trip

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import coil.load
import coil.request.CachePolicy
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.group06.lab.utils.Database
import com.group06.lab.R
import java.lang.StringBuilder
import com.group06.lab.extensions.toString
import java.text.DecimalFormat
import java.util.*

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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_trip_detail, container, false)
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
        val tvDepTime: TextView = view.findViewById<TextView>(R.id.tvDepTime)
        val tvArrTime: TextView = view.findViewById<TextView>(R.id.tvArrTime)

        tvDepartureLocation.text = t.departure
        tvArrivalLocation.text = t.arrival
        tvDepartureDate.text = t.departureDate.toString("MMMM - dd")
        tvAvailableSeats.text = t.availableSeats.toString()

        tvDepTime.text = t.departureDate.toString("HH:mm")
        val calendar = Calendar.getInstance()
        calendar.time = t.departureDate
        calendar.add(Calendar.DAY_OF_MONTH, t.estimatedDay)
        calendar.add(Calendar.HOUR, t.estimatedHour)
        calendar.add(Calendar.MINUTE, t.estimatedMinute)
        tvArrTime.text = calendar.time.toString("HH:mm")

        val format = DecimalFormat()
        format.isDecimalSeparatorAlwaysShown = false
        tvPrice.text = String.format("%s €", format.format(t.price).toString())
        tvDescription.text = t.description

        val sBuilder = StringBuilder()
        if (t.estimatedDay > 0)
            sBuilder.append(String.format("%dd", t.estimatedDay))
        if (t.estimatedHour > 0)
            sBuilder.append(String.format(" %dh", t.estimatedHour))
        if (t.estimatedMinute > 0)
            sBuilder.append(String.format(" %dm", t.estimatedMinute))
        tvEstimatedDuration.text = "(${sBuilder.toString()} )"

        if (t.imageUrl == "") {
            imgTrip.setImageResource(R.drawable.ic_no_photo)
        } else {
            Firebase.storage.reference.child(t.imageUrl)
                .downloadUrl.addOnSuccessListener {
                        uri -> imgTrip.load(uri.toString()){
                        memoryCachePolicy(CachePolicy.DISABLED) //to force reloading when image changes
                    }
                }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.fragment_trip_details, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.edit -> {
                findNavController().navigate(R.id.action_trip_details_to_trip_edit, Bundle().apply {
                    putBoolean("edit", true)
                    putInt("index", index!!)
                })
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