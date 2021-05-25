package com.group06.lab.trip

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.group06.lab.R
import com.group06.lab.extensions.toString
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*

class TripEditFragment : Fragment() {
    val REQUEST_IMAGE_CAPTURE = 1
    val REQUEST_IMAGE_GALLERY = 2

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var etDeparture: TextInputLayout
    private lateinit var etArrival: TextInputLayout
    private lateinit var etDepartureDate: TextInputLayout
    private lateinit var etDuration: TextInputLayout
    private lateinit var etAvailableSeats: TextInputLayout
    private lateinit var etPrice: TextInputLayout
    private lateinit var etDescription: TextInputLayout
    private lateinit var imgTrip: ImageView

    private lateinit var snackBar: Snackbar;

    private var dateValue: Date = Date()
    private var dateOk: Boolean = false

    private var day: Int = 0
    private var hour: Int = 0
    private var minute: Int = 0

    private var imgChanged = false
    private var imgName: String = ""

    private var edit: Boolean? = false
    private var index = -1
    private var tripId: String? = ""
    private var id: String = ""

    private lateinit var mAuth: FirebaseAuth

    private val vm by viewModels<TripViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
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

        imgTrip = view.findViewById(R.id.imgTrip)
        val imageButtonEdit = view.findViewById<ImageButton>(R.id.imageButtonEdit)
        imageButtonEdit.setOnClickListener {
            registerForContextMenu(imageButtonEdit)
            activity?.openContextMenu(imageButtonEdit)
            unregisterForContextMenu(imageButtonEdit);
        }

        edit = arguments?.getBoolean("edit")
        if (edit!!){
            index = arguments?.getInt("index")!!
            tripId = arguments?.getString("tripId")

            vm.getTripById(tripId!!).observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                val t: Trip = it

                etDeparture.editText?.setText(t.departure)
                etArrival.editText?.setText(t.arrival)
                etDepartureDate.editText?.setText(t.departureDate.toString("yyyy/MM/dd - HH:mm"))
                etAvailableSeats.editText?.setText(t.availableSeats.toString())
                etPrice.editText?.setText(t.price.toString())
                etDescription.editText?.setText(t.description)

                imgName = t.imageUrl
                dateValue = t.departureDate

                dateOk = true
                day = t.estimatedDay
                hour = t.estimatedHour
                minute = t.estimatedMinute

                val sBuilder = StringBuilder()
                if (t.estimatedDay > 0)
                    sBuilder.append(String.format("%dd", t.estimatedDay))
                if (t.estimatedHour > 0)
                    sBuilder.append(String.format(" %dh", t.estimatedHour))
                if (t.estimatedMinute > 0)
                    sBuilder.append(String.format(" %dm", t.estimatedMinute))

                etDuration.editText?.setText(sBuilder.toString())
                if (t.imageUrl == "") {
                    imgTrip.setImageResource(R.drawable.ic_baseline_no_photography)
                } else {
                    Firebase.storage.reference.child(t.imageUrl)
                        .downloadUrl.addOnSuccessListener {
                                uri -> imgTrip.load(uri.toString())
                        }
                }
            })


        }

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
            dateOk = true
            dateValue.time += (timePicker.hour*60*60 + timePicker.minute*60)*1000
            etDepartureDate.editText?.setText(dateValue.toString("yyyy/MM/dd - HH:mm"))
        }

        etDuration.editText?.setOnClickListener {
            showDialog()
        }

        if (savedInstanceState != null){
            etDeparture.editText?.setText(savedInstanceState.getString("departure"))
            etArrival.editText?.setText(savedInstanceState.getString("arrival"))
            dateValue.time = savedInstanceState.getLong("date", Date().time)
            dateOk = savedInstanceState.getBoolean("dateok", false)
            etDuration.editText?.setText(savedInstanceState.getString("duration"))
            etAvailableSeats.editText?.setText(savedInstanceState.getString("availableseats"))
            etPrice.editText?.setText(savedInstanceState.getString("price"))
            etDescription.editText?.setText(savedInstanceState.getString("description"))
            File(context?.filesDir, savedInstanceState.getString("imgTrip") ?: "").let {
                if (it.exists()) imgTrip.setImageBitmap(BitmapFactory.decodeFile(it.absolutePath))
            }
            if (dateOk)
                etDepartureDate.editText?.setText(dateValue.toString("yyyy/MM/dd - HH:mm"))
        }

        snackBar = Snackbar.make(requireActivity().findViewById(android.R.id.content), "Trip correctly saved", Snackbar.LENGTH_LONG)
        snackBar.setAction("Dismiss"){
            snackBar.dismiss()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("departure", etDeparture.editText?.text.toString())
        outState.putString("arrival", etArrival.editText?.text.toString())
        outState.putLong("date", dateValue.time)
        outState.putBoolean("dateok", dateOk)
        outState.putString("duration", etDuration.editText?.text.toString())
        outState.putString("availableseats", etAvailableSeats.editText?.text.toString())
        outState.putString("price", etPrice.editText?.text.toString())
        outState.putString("description", etDescription.editText?.text.toString())
        outState.putString("imgTrip", "trippictemp.png")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.fragment_trip_edit, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.save -> {
                //save data
                if (validateForm()){
                    val t = Trip(
                        "",
                        imgName,
                        etDeparture.editText?.text.toString(),
                        etArrival.editText?.text.toString(),
                        dateValue,
                        day,
                        hour,
                        minute,
                        etAvailableSeats.editText?.text.toString().toInt(),
                        etPrice.editText?.text.toString().toDouble(),
                        etDescription.editText?.text.toString(),
                        mAuth.currentUser!!.email!!
                    )

                    val trips = FirebaseFirestore.getInstance().collection("trips")
                    val doc: DocumentReference = if (edit!!){
                        trips.document(tripId!!)
                    }else{
                        trips.document()
                    }
                    doc.set(t)
                        .addOnSuccessListener {
                            snackBar.show()
                        }

                    if (imgChanged){
                        saveImageOnCloudStorage(imgTrip.drawable.toBitmap(), imgName)
                            .addOnFailureListener {
                                // Handle unsuccessful uploads
                            }.addOnSuccessListener {
                                findNavController().navigate(R.id.action_trip_edit_to_othersTripListFragment)
                            }
                    }else{
                        findNavController().navigate(R.id.action_trip_edit_to_othersTripListFragment)
                    }
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

    private fun showDialog() {
        val builder = AlertDialog.Builder(context)
        val inflater = layoutInflater
        builder.setTitle("Select duration")
        val dialogLayout = inflater.inflate(R.layout.dialog_set_duration, null)
        val daySpinner = dialogLayout.findViewById<Spinner>(R.id.daySpinner)
        val hourSpinner = dialogLayout.findViewById<Spinner>(R.id.hourSpinner)
        val minuteSpinner = dialogLayout.findViewById<Spinner>(R.id.minuteSpinner)

        val adapter1 = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, (0..30).toList())
        val adapter2 = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, (0..23).toList())
        val adapter3 = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, (0..59).toList())

        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        daySpinner.run {
            adapter = adapter1
            setSelection(day)
        }
        hourSpinner.run {
            adapter = adapter2
            setSelection(hour)
        }
        minuteSpinner.run {
            adapter = adapter3
            setSelection(minute)
        }


        builder.setView(dialogLayout)

        builder.setPositiveButton("OK") { _, _ ->
            if (daySpinner.selectedItem != null)
                day = daySpinner.selectedItem.toString().toInt()
            if (hourSpinner.selectedItem != null)
                hour = hourSpinner.selectedItem.toString().toInt()
            if (minuteSpinner.selectedItem != null)
                minute = minuteSpinner.selectedItem.toString().toInt()

            if (day > 0 || hour > 0 || minute > 0){
                val sBuilder = StringBuilder()
                if (day > 0)
                    sBuilder.append(String.format("%dd", day))
                if (hour > 0)
                    sBuilder.append(String.format(" %dh", hour))
                if (minute > 0)
                    sBuilder.append(String.format(" %dm", minute))
                etDuration.editText?.setText(sBuilder.toString())
            }
        }
        builder.show()
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        activity?.menuInflater?.inflate(R.menu.activity_edit_profile_photo_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        imgTrip.invalidate()
        return when (item.itemId) {
            R.id.addGallery -> {
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                startActivityForResult(intent, REQUEST_IMAGE_GALLERY)
                true
            }
            R.id.addCamera -> {
                dispatchTakePictureIntent()
                true
            }
            else -> false
        }
    }
    // ---------------- function for activating the camera to take a picture
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
            Toast.makeText(context, "error $e", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == AppCompatActivity.RESULT_OK) {
            if (imgName == "")
                imgName = genRandomString()
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imgTrip.setImageBitmap(imageBitmap)
            imgChanged = true
            saveImageOnStorage(imageBitmap, "trippictemp.png")
        } else if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == AppCompatActivity.RESULT_OK) {
            if (imgName == "")
                imgName = genRandomString()
            imgTrip.setImageURI(data?.data)
            imgChanged = true
            saveImageOnStorage(imgTrip.drawable.toBitmap(), "trippictemp.png")
        }
    }

    private fun saveImageOnCloudStorage(bitmap: Bitmap, tempName: String): UploadTask {
        val storage = Firebase.storage
        val storageRef = storage.reference
        val imgRef = storageRef.child(tempName)

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        return imgRef.putBytes(data)
    }

    private fun saveImageOnStorage(bitmap: Bitmap, tempName: String) {
        val file = File(context?.filesDir, tempName)
        val out = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        out.flush()
        out.close()
    }

    private fun genRandomString(): String{
        val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

        return (1..16)
            .map { i -> kotlin.random.Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }
}