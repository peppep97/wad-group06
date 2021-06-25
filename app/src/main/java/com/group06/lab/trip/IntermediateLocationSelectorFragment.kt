package com.group06.lab.trip

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.os.Parcelable
import android.os.StrictMode
import android.preference.PreferenceManager
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.group06.lab.R
import org.osmdroid.api.IGeoPoint
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import java.text.SimpleDateFormat
import java.util.*

private var isDeparture: Boolean? = true
private var edit: Boolean? = false
private var trip_id: String? = ""
private var depLat: Double? = 0.0
private var arrLat: Double? = 0.0
private var depLon: Double? = 0.0
private var arrLon: Double? = 0.0
private var depCity: String? = ""
private var arrCity: String? = ""

class IntermediateLocationSelectorFragment : Fragment() {
    private lateinit var map: MapView;
    private lateinit var startMarker: Marker

    private lateinit var client: FusedLocationProviderClient
    private var location: IGeoPoint = GeoPoint(0.0, 0.0)
    private lateinit var city: String
    private var stopList: List<IntermediateStop>? = null

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
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_location_selector, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        isDeparture = arguments?.getBoolean("isDeparture")
        edit = arguments?.getBoolean("edit")
        trip_id = arguments?.getString("tripId")
        stopList = arguments?.getParcelableArrayList("stops")

        map = view.findViewById(R.id.mapLocationSelector)
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true)

        val mapController = map.controller
        mapController.setZoom(16.0)
        var startPoint: GeoPoint
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        client.lastLocation.addOnSuccessListener(
            requireActivity()
        ) { location ->
            run {
                depLat = arguments?.getDouble("depLat")
                depLon = arguments?.getDouble("depLon")
                depCity = arguments?.getString("depCity", "")
                arrLat = arguments?.getDouble("arrLat")
                arrLon = arguments?.getDouble("arrLon")
                arrCity = arguments?.getString("arrCity", "")

                //set map start point to current location (dep or arr)
                startPoint = GeoPoint(location.latitude, location.longitude)

                mapController.setCenter(startPoint)
                placeMarker(startPoint)
            }
        }

        map.overlays.add(object : Overlay() {
            override fun onSingleTapConfirmed(
                e: MotionEvent,
                mapView: MapView
            ): Boolean {
                val projection = mapView.projection
                val geoPoint = projection.fromPixels(
                    e.x.toInt(),
                    e.y.toInt()
                )

                startMarker.remove(mapView)
                placeMarker(GeoPoint(geoPoint.latitude, geoPoint.longitude))

                return true
            }
        })
    }

    fun placeMarker(position: GeoPoint){

        startMarker = Marker(map)
        startMarker.position = position

        location = position

        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

        startMarker.title = "Stop"
        startMarker.id = "stop"

        startMarker.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_location_blue)

        map.overlays.add(startMarker)
        map.controller.setCenter(startMarker.position)
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_location_select, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.selectLocation -> {
                val gcd = Geocoder(context, Locale.getDefault())

                if (location.latitude != 0.0) {
                    gcd.getFromLocation(location.latitude, location.longitude, 1)
                        .stream().findFirst().ifPresent {
                            city = it.locality
                        }
                }

                var date = Date()

                val constraintsBuilder =
                    CalendarConstraints.Builder()
                        .setValidator(DateValidatorPointForward.now())

                val datePicker =
                    MaterialDatePicker.Builder.datePicker().setCalendarConstraints(constraintsBuilder.build()).setTitleText("Select date").build()

                activity?.supportFragmentManager?.let { it1 ->
                    datePicker.show(it1, "selectdate")
                }

                datePicker.addOnPositiveButtonClickListener {

                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = it
                    calendar.set(Calendar.HOUR, 0)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)

                    date.time = calendar.timeInMillis


                    val timePicker = MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_24H)
                        .setHour(12)
                        .setMinute(10)
                        .setTitleText("Select Appointment time")
                        .build()

                    timePicker.addOnPositiveButtonClickListener {
                        date.time += (timePicker.hour * 60 * 60 + timePicker.minute * 60) * 1000

                        findNavController().navigate(
                            R.id.action_intermediateLocationSelectorFragment_to_intermediateStopsFragment,
                            Bundle().apply {
                                putBoolean("isDeparture", isDeparture!!)
                                putDouble("depLat", depLat!!)
                                putDouble("depLon", depLon!!)
                                putString("depCity", depCity)
                                putDouble("arrLat", arrLat!!)
                                putDouble("arrLon", arrLon!!)
                                putString("arrCity", arrCity)
                                putString("tripId", trip_id)
                                putBoolean("edit", edit!!)

                                putString("city", city)
                                putDouble("lat", location.latitude)
                                putDouble("lon", location.longitude)
                                putLong("date", date.time)
                                putBoolean("add", true)
                                putParcelableArrayList("stops", stopList as ArrayList<out Parcelable>?)
                            })

                    }

                    activity?.supportFragmentManager?.let { it1 ->
                        timePicker.show(it1, "selecttime")
                    }
                }
                return true
            }
        }
        return false
    }
}