package com.group06.lab1.ui.trip

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.group06.lab1.R
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TripEditFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TripEditFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var etDeparture : TextInputLayout
    private lateinit var etArrival : TextInputLayout
    private lateinit var etDepartureDate : TextInputLayout
    private lateinit var etDuration : TextInputLayout
    private lateinit var etAvailableSeats : TextInputLayout
    private lateinit var etPrice : TextInputLayout
    private lateinit var etDescription : TextInputLayout

    private var dateValue: Date = Date()
    private var dateOk: Boolean = false

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
        return inflater.inflate(R.layout.fragment_trip_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val datePicker =
            MaterialDatePicker.Builder.datePicker().setTitleText("Select date").build()

        val timePicker =
            MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(12)
                .setMinute(10)
                .setTitleText("Select Appointment time")
                .build()

        etDeparture = view.findViewById(R.id.etDeparture)
        etArrival = view.findViewById(R.id.etArrival)
        etDepartureDate = view.findViewById(R.id.etDepartureDate)
        etDuration = view.findViewById(R.id.etDuration)
        etAvailableSeats = view.findViewById(R.id.etAvailableSeats)
        etPrice = view.findViewById(R.id.etPrice)
        etDescription = view.findViewById(R.id.etDescription)

        val imageButtonEdit = view.findViewById<ImageButton>(R.id.imageButtonEdit)

        etDepartureDate.editText?.setOnClickListener {
            dateOk = false
            activity?.supportFragmentManager?.let {
                    it1 -> datePicker.show(it1, "selectdate")
            }
        }

        datePicker.addOnPositiveButtonClickListener {
            dateValue.time = it
            activity?.supportFragmentManager?.let {
                    it1 -> timePicker.show(it1, "selecttime")
            }
        }

        timePicker.addOnPositiveButtonClickListener {
            dateOk = false
            dateValue.time += (timePicker.hour*60*60 + timePicker.minute*60)*1000
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.fragment_trip_edit, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.save -> {
                //save data
                if (validateForm()){
                    val t = Trip("",
                        etDeparture.editText?.text.toString(),
                        etArrival.editText?.text.toString(),
                        dateValue,
                        etDuration.editText?.text.toString().toInt(),
                        etAvailableSeats.editText?.text.toString().toInt(),
                        etPrice.editText?.text.toString().toDouble(),
                        etDescription.editText?.text.toString()
                    )
                }else{
                    //show a snackbar
                    //activity.supportFragmentManager.findFragmentById(R.)
                    findNavController().navigateUp()
                }
                return true
            }
        }
        return false
    }

    private fun validateForm(): Boolean{
        var res = true

        if (etDeparture.editText?.text.toString() == ""){
            etDeparture.error = "Provide a value"
            res = false
        }
        if (etArrival.editText?.text.toString() == ""){
            etArrival.error = "Provide a value"
            res = false
        }
        if (etDuration.editText?.text.toString() == ""){
            etDuration.error = "Provide a value"
            res = false
        }
        if (!dateOk) {
            etDepartureDate.error = "Provide a value"
            res = false
        }
        if (etAvailableSeats.editText?.text.toString() == ""){
            etAvailableSeats.error = "Provide a value"
            res = false
        }
        if (etPrice.editText?.text.toString() == ""){
            etPrice.error = "Provide a value"
            res = false
        }
        return res
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TripEditFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TripEditFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}