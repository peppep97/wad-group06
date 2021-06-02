package com.group06.lab.trip

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.Debug
import android.os.StrictMode
import android.preference.PreferenceManager
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import coil.request.CachePolicy
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.group06.lab.MainActivity
import com.group06.lab.R
import com.group06.lab.extensions.toString
import com.group06.lab.utils.Dialog
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
private var tripId: String? = ""
private var caller: String? = ""
private var showEditButton: Boolean = false
private lateinit var snackBar: Snackbar
private lateinit var client: FusedLocationProviderClient

class TripDetailsFragment : Fragment() {
    private lateinit var fabFav: FloatingActionButton
    private lateinit var btnShowFavoredList: Button
    private lateinit var btnDeleteTrip: Button
    private lateinit var btnCompleteTrip : Button
    private lateinit var map : MapView;
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private lateinit var ratingBar : RatingBar


    private val vm by viewModels<TripViewModel>()

    var myMenu: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(activity,
            PreferenceManager.getDefaultSharedPreferences(activity))
        client = LocationServices.getFusedLocationProviderClient(requireActivity())
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        btnDeleteTrip = view.findViewById(R.id.DeleteTrip)

        ratingBar = view.findViewById(R.id.RatingBar)

        /*val t: Trip = when (caller) {
            "UserTrips" -> {
                ArrayList<Trip>(Database.getInstance(context).myTripList.filter { it.id == tripId })[0]
            }
            "OtherTrips" -> {
                ArrayList<Trip>(Database.getInstance(context).tripList.filter { it.id == tripId })[0]
            }
            else -> {
                ArrayList<Trip>(Database.getInstance(context).tripList.filter { it.id == tripId })[0]
            }
        }*/

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
        map = view.findViewById<MapView>(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        val mapController = map.controller
        mapController.setZoom(9.5)
        map.parent.requestDisallowInterceptTouchEvent(true)

        vm.getTripById(tripId!!).observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            val t : Trip = it

            showEditButton = t.userEmail == MainActivity.mAuth.currentUser!!.email!!
            setHasOptionsMenu(true)
            fabFav.visibility = if (t.userEmail == MainActivity.mAuth.currentUser!!.email!!) View.GONE else View.VISIBLE
            btnDeleteTrip.visibility = if (t.userEmail == MainActivity.mAuth.currentUser!!.email!!) View.VISIBLE else View.GONE
            btnCompleteTrip.visibility = if (t.userEmail == MainActivity.mAuth.currentUser!!.email!!) View.VISIBLE else View.GONE
            if (t.userEmail == MainActivity.mAuth.currentUser!!.email!!) fabFav.hide()
            btnShowFavoredList.visibility = if (t.userEmail == MainActivity.mAuth.currentUser!!.email!!) View.VISIBLE else View.GONE

            btnCompleteTrip.visibility = if(t.completed == true) View.GONE else View.VISIBLE
            btnDeleteTrip.visibility = if(t.completed == true) View.GONE else View.VISIBLE
            btnShowFavoredList.visibility = if(t.completed == true) View.GONE else View.VISIBLE

            ratingBar.visibility = if(t.completed == true) View.VISIBLE else View.GONE
            ratingBar.visibility = if(t.userEmail == MainActivity.mAuth.currentUser!!.email!!) View.GONE else View.VISIBLE

            myMenu?.findItem(R.id.edit)?.isVisible = showEditButton

            tvDepartureLocation.text = t.departure
            tvArrivalLocation.text = t.arrival
            tvDepartureDate.text = t.departureDate.toString("MMMM - dd")
            tvAvailableSeats.text = t.availableSeats.toString()


            val origin = GeoPoint(t.depPosition.latitude, t.depPosition.longitude)
            val destination = GeoPoint(t.arrPosition.latitude, t.arrPosition.longitude)

            if (origin.latitude != 0.0 && origin.longitude != 0.0 &&
                destination.latitude != 0.0 && destination.longitude != 0.0) {

                val gcd = Geocoder(context, Locale.getDefault())
                var addresses: List<Address> =
                    gcd.getFromLocation(origin.latitude, origin.longitude, 1)

                val startMarker = Marker(map)
                startMarker.position = GeoPoint(origin.latitude, origin.longitude)
                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                if (addresses.isNotEmpty())
                    startMarker.title = "${addresses[0].locality} - ${addresses[0].countryName}\n${addresses[0].thoroughfare ?: ""} ${addresses[0].subThoroughfare ?: ""}"
                else
                    startMarker.title = "Departure"
                startMarker.id = "dep"

                map.overlays?.add(startMarker)

                val arrAddresses: List<Address> = gcd.getFromLocation(destination.latitude, destination.longitude, 1)
                val endMarker = Marker(map)
                endMarker.position = GeoPoint(destination.latitude, destination.longitude)
                endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                if (arrAddresses.isNotEmpty())
                    endMarker.title = "${arrAddresses[0].locality} - ${arrAddresses[0].countryName}\n${arrAddresses[0].thoroughfare ?: ""} ${arrAddresses[0].subThoroughfare ?: ""}"
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
                    //
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
                findNavController().navigate(
                    R.id.action_trip_details_to_favored_trip_list,
                    Bundle().apply {
                        putString("tripId", tripId)
                        putInt("AvailableSeats", t.availableSeats)
                    })
            }

            btnDeleteTrip.setOnClickListener {

                Dialog().show(parentFragmentManager, "dialog")

                //Delete trip from database
                val db = FirebaseFirestore.getInstance()
                val batch = db.batch()

                db.collection("trips").document(tripId!!).collection("confirmedUsers").get().addOnSuccessListener {

                        res ->
                    res.documents.forEach { batch.delete(it.reference) }
                    //Works only for small collection I guess?
                    batch.commit()
                    db.collection("trips")
                        .document(tripId!!)
                        .delete()
                }

                findNavController().navigate(
                    R.id.action_trip_details_to_trip_list,
                    Bundle().apply {
                        putString("tripId", tripId)
                        putInt("AvailableSeats", t.availableSeats)
                    })
            }
        })


        fabFav.setOnClickListener {

            vm.isAlreadyFavored(tripId!!).observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                val f = FavoriteTrip(MainActivity.mAuth.currentUser!!.email!!, tripId!!)

                if (it){ //if it is not favorite
                    val favoredList = FirebaseFirestore.getInstance().collection("favored_trips")
                    val doc = favoredList.document()
                    doc.set(f)
                        .addOnSuccessListener {
                            snackBar.show()
                        }
                }else{
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

    override fun onResume() {
        super.onResume();
        Configuration.getInstance().load(activity,
            PreferenceManager.getDefaultSharedPreferences(activity))
        map.onResume();
    }

    override fun onPause() {
        super.onPause();
        Configuration.getInstance().load(activity,
            PreferenceManager.getDefaultSharedPreferences(activity))
        map.onPause();
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
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
                REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_trip_details, menu)
        menu.findItem(R.id.edit).isVisible = showEditButton
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
        }
        return false
    }
}