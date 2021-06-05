package com.group06.lab.trip

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.StrictMode
import android.preference.PreferenceManager
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import coil.request.CachePolicy
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.group06.lab.MainActivity
import com.group06.lab.R
import com.group06.lab.extensions.toString
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.Polyline
import java.text.DecimalFormat
import java.util.*

private var index: Int? = null
var tripId: String? = ""
private var caller: String? = ""
private lateinit var snackBar: Snackbar
private lateinit var client: FusedLocationProviderClient

class TripDetailsFragment : Fragment() {
    private lateinit var fabFav: FloatingActionButton
    private lateinit var btnShowFavoredList: Button
    private lateinit var btnCompleteTrip: Button

    private lateinit var map: MapView;

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private var PERMISSION_ALL = 2

    private var tripOwner: Boolean = false

    private val vm by viewModels<TripViewModel>()

    var myMenu: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(
            activity,
            PreferenceManager.getDefaultSharedPreferences(activity)
        )
        client = LocationServices.getFusedLocationProviderClient(requireActivity())
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_trip_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        index = arguments?.getInt("index")
        tripId = arguments?.getString("tripId")
        caller = arguments?.getString("caller")

        fabFav = view.findViewById(R.id.fabFav)
        btnShowFavoredList = view.findViewById(R.id.btnShowFavoredList)
        btnCompleteTrip = view.findViewById(R.id.CompleteTrip)

        snackBar = Snackbar.make(
            requireActivity().findViewById(android.R.id.content),
            "Added to favorite trips list",
            Snackbar.LENGTH_SHORT
        )

        val tvDepartureLocation = view.findViewById<TextView>(R.id.tvDepartureLocation)
        val tvArrivalLocation = view.findViewById<TextView>(R.id.tvArrivalLocation)
        val tvDepartureDate = view.findViewById<TextView>(R.id.tvDepartureDate)
        val tvEstimatedDuration = view.findViewById<TextView>(R.id.tvEstimatedDuration)
        val tvAvailableSeats = view.findViewById<TextView>(R.id.tvAvailableSeats)
        val tvPrice = view.findViewById<TextView>(R.id.tvPrice)
        val tvDescription = view.findViewById<TextView>(R.id.tvDescription)
        val imgTrip = view.findViewById<ImageView>(R.id.imgTrip)
        val tvDepTime = view.findViewById<TextView>(R.id.tvDepTime)
        val tvArrTime = view.findViewById<TextView>(R.id.tvArrTime)

        map = view.findViewById(R.id.mapRoute)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        val mapController = map.controller
        mapController.setZoom(9.5)
        map.parent.requestDisallowInterceptTouchEvent(true)

        val PERMISSIONS = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
        } else {
            ActivityCompat.requestPermissions(requireActivity(), PERMISSIONS, PERMISSION_ALL);
        }

        vm.getTripById(tripId!!).observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            val t: Trip = it

            tripOwner = t.userEmail == FirebaseAuth.getInstance().currentUser!!.email!!

            btnCompleteTrip.visibility = if (tripOwner && !t.completed) View.VISIBLE else View.GONE
            btnShowFavoredList.visibility = if (tripOwner) View.VISIBLE else View.GONE

            if (tripOwner) fabFav.hide()

            if (tripOwner && t.completed) {
                btnShowFavoredList.text = "Rate Passengers"
            }else{
                btnShowFavoredList.text = "Show interested users"
            }

            //show edit menu if owner
            myMenu?.findItem(R.id.edit)?.isVisible = tripOwner
            myMenu?.findItem(R.id.delete)?.isVisible = tripOwner

            tvDepartureLocation.text = t.departure
            tvArrivalLocation.text = t.arrival
            tvDepartureDate.text = t.departureDate.toString("MMMM - dd")
            tvAvailableSeats.text = t.availableSeats.toString()

            val origin = GeoPoint(t.depPosition.latitude, t.depPosition.longitude)
            val destination = GeoPoint(t.arrPosition.latitude, t.arrPosition.longitude)

            if (origin.latitude != 0.0 && origin.longitude != 0.0 &&
                destination.latitude != 0.0 && destination.longitude != 0.0
            ) {

                val gcd = Geocoder(context, Locale.getDefault())
                var addresses: List<Address> =
                    gcd.getFromLocation(origin.latitude, origin.longitude, 1)

                val startMarker = Marker(map)
                startMarker.position = GeoPoint(origin.latitude, origin.longitude)
                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                if (addresses.isNotEmpty())
                    startMarker.title =
                        "${addresses[0].locality} - ${addresses[0].countryName}\n${addresses[0].thoroughfare ?: ""} ${addresses[0].subThoroughfare ?: ""}"
                else
                    startMarker.title = "Departure"
                startMarker.id = "dep"

                map.overlays?.add(startMarker)

                val arrAddresses: List<Address> =
                    gcd.getFromLocation(destination.latitude, destination.longitude, 1)
                val endMarker = Marker(map)
                endMarker.position = GeoPoint(destination.latitude, destination.longitude)
                endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                if (arrAddresses.isNotEmpty())
                    endMarker.title =
                        "${arrAddresses[0].locality} - ${arrAddresses[0].countryName}\n${arrAddresses[0].thoroughfare ?: ""} ${arrAddresses[0].subThoroughfare ?: ""}"
                else
                    endMarker.title = "Arrival"
                endMarker.id = "arr"

                map.overlays?.add(startMarker)
                map.overlays?.add(endMarker)

                mapController.setCenter(origin)

                val roadManager: RoadManager =
                    OSRMRoadManager(requireContext(), "OBP_Tuto/1.0")

                map.overlays
                    .forEach { o -> if (o is Polyline) map.overlays.remove(o as Overlay) }

                val wayPoints = ArrayList<GeoPoint>()
                wayPoints.add(origin)
                wayPoints.add(destination)
                val road = roadManager.getRoad(wayPoints)
                val roadOverlay = RoadManager.buildRoadOverlay(road)
                roadOverlay.id = "path"
                map.overlays.add(roadOverlay);
                map.invalidate();
            } else {
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {

                } else {
                    client.lastLocation.addOnSuccessListener(
                        requireActivity()
                    ) { location ->
                        run {
                            mapController.setCenter(GeoPoint(location.latitude, location.longitude))
                        }
                    }
                }
            }

            map.overlays.add(object : Overlay() {
                override fun onSingleTapConfirmed(
                    e: MotionEvent,
                    mapView: MapView
                ): Boolean {
                    findNavController().navigate(
                        R.id.action_trip_details_to_tripRouteFragment,
                        Bundle().apply {
                            putDouble("depLat", t.depPosition.latitude)
                            putDouble("depLon", t.depPosition.longitude)
                            putDouble("arrLat", t.arrPosition.latitude)
                            putDouble("arrLon", t.arrPosition.longitude)
                        })
                    return true
                }
            })

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
            tvEstimatedDuration.text = "(${sBuilder})"

            if (t.imageUrl == "") {
                imgTrip.setImageResource(R.drawable.ic_baseline_no_photography)
            } else {
                Firebase.storage.reference.child(t.imageUrl)
                    .downloadUrl.addOnSuccessListener { uri ->
                        imgTrip.load(uri.toString()) {
                            memoryCachePolicy(CachePolicy.DISABLED) //to force reloading when image changes
                        }
                    }
            }

            btnShowFavoredList.setOnClickListener {

                if (t.completed) {
                    findNavController().navigate(
                        R.id.action_trip_details_to_favored_trip_list,
                        Bundle().apply {
                            putString("tripId", tripId)
                            putInt("AvailableSeats", t.availableSeats)
                            putBoolean("TripIsComplete", true)
                        })
                } else {
                    findNavController().navigate(
                        R.id.action_trip_details_to_favored_trip_list,
                        Bundle().apply {
                            putString("tripId", tripId)
                            putInt("AvailableSeats", t.availableSeats)
                            putBoolean("TripIsComplete", false)
                        })
                }
            }

            vm.getConfirmedUsersByTrip(tripId!!).observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                it.stream().filter { u -> u.email ==  MainActivity.mAuth.currentUser!!.email!!}.findFirst().ifPresent {
                    if (t.completed){ //if I was confirmed and trip is completed, I can rate the driver

                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(t.userEmail)
                            .collection("Ratings")
                            .whereEqualTo("Role", "Driver")
                            .whereEqualTo("TripId", tripId)
                            .whereEqualTo("userMail", FirebaseAuth.getInstance().currentUser!!.email!!)
                            .addSnapshotListener { value, error ->
                                if (error != null) throw error
                                if (value != null) {
                                    if (value.isEmpty){
                                        fabFav.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_star))
                                        fabFav.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                                        fabFav.show()
                                    }else{
                                        fabFav.hide()
                                    }
                                }
                            }

                        fabFav.setOnClickListener {
                            showRateDialog(t.userEmail)
                        }
                    }
                }
            })
        })


        fabFav.setOnClickListener {

            vm.isAlreadyFavored(tripId!!).observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                val f = FavoriteTrip(MainActivity.mAuth.currentUser!!.email!!, tripId!!)

                if (it) { //if it is not favorite
                    val favoredList = FirebaseFirestore.getInstance().collection("favored_trips")
                    val doc = favoredList.document()
                    doc.set(f)
                        .addOnSuccessListener {
                            snackBar.show()
                        }
                } else {
                    Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        "You have already liked this trip.",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            })
        }

        btnCompleteTrip.setOnClickListener {
            //Complete the trip
            val db = FirebaseFirestore.getInstance()

            db.collection("trips").document(tripId!!)
                .update("completed", true)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val permissionsToRequest = ArrayList<String>();
        var i = 0;
        while (i < grantResults.size) {
            permissionsToRequest.add(permissions[i]);
            i++;
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                permissionsToRequest.toTypedArray(),
                REQUEST_PERMISSIONS_REQUEST_CODE
            );
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        myMenu = menu
        inflater.inflate(R.menu.fragment_trip_details, menu)
        menu.findItem(R.id.edit).isVisible = tripOwner
        menu.findItem(R.id.delete).isVisible = tripOwner
    }

    override fun onResume() {
        super.onResume();
        Configuration.getInstance().load(
            activity,
            PreferenceManager.getDefaultSharedPreferences(activity)
        )
        map.onResume();
    }

    override fun onPause() {
        super.onPause();
        Configuration.getInstance().load(
            activity,
            PreferenceManager.getDefaultSharedPreferences(activity)
        )
        map.onPause();
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.edit -> {
                findNavController().navigate(R.id.action_trip_details_to_trip_edit, Bundle().apply {
                    putBoolean("edit", true)
                    putInt("index", index!!)
                    putString("tripId", tripId)
                })
                return true
            }
            R.id.delete -> {

                val alertBuilder = AlertDialog.Builder(requireContext())

                alertBuilder.setTitle("Delete Trip")
                alertBuilder.setMessage("If you want to delete the trip, please click OK.")

                alertBuilder.setPositiveButton("OK") { _, _ ->
                    //Delete trip from database
                    val db = FirebaseFirestore.getInstance()

                    db.collection("trips")
                        .document(tripId!!)
                        .delete()


                    /* val batch = db.batch()

                     db.collection("trips").document(tripId!!).collection("confirmedUsers").get()
                         .addOnSuccessListener { res ->
                             res.documents.forEach { batch.delete(it.reference) }
                             //Works only for small collection I guess?
                             batch.commit()
                             db.collection("trips")
                                 .document(tripId!!)
                                 .delete()
                         }

                     db.collection("trips").document(tripId!!).collection("Ratings").get()
                         .addOnSuccessListener { res ->
                             res.documents.forEach { batch.delete(it.reference) }
                             //Works only for small collection I guess?
                             batch.commit()
                             db.collection("trips")
                                 .document(tripId!!)
                                 .delete()
                         }*/

                    findNavController().navigate(
                        R.id.action_trip_details_to_trip_list)
                }.setNeutralButton("Cancel") { _, _ -> }

                alertBuilder.create()
                alertBuilder.show()
            }
        }
        return false
    }

    private fun showRateDialog(userEmail: String){
        val builder = android.app.AlertDialog.Builder(context)

        val mView: View = LayoutInflater.from(context)
            .inflate(R.layout.rating_dialog, null)

        val messageInput: TextInputLayout = mView.findViewById(R.id.commentRating)
        val ratingBar: RatingBar = mView.findViewById(R.id.ratingbarProfile)

        builder.setTitle("Rate Driver")
        builder.setView(mView)

        builder.setPositiveButton("Rate") { _, _ ->
            //Send Rating
            val db = FirebaseFirestore.getInstance()

            var dataToUser = hashMapOf(
                "userMail" to MainActivity.mAuth.currentUser!!.email!!,
                "Score" to ratingBar.rating,
                "TripId" to tripId,
                "Role" to "Driver",
                "message" to messageInput!!.editText?.text.toString()
            )

            db.collection("users")
                .document(userEmail)
                .collection("Ratings")
                .add(dataToUser)
                .addOnSuccessListener {
                    Toast.makeText(context, "Driver rated successfully", Toast.LENGTH_SHORT).show()
                }
        }
        builder.show()
    }
}