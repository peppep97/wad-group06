package com.group06.lab.trip

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.StrictMode
import android.preference.PreferenceManager
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.group06.lab.R
import org.osmdroid.api.IGeoPoint
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
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

class LocationSelectorFragment : Fragment() {
    private lateinit var map: MapView;
    private lateinit var startMarker: Marker

    private lateinit var client: FusedLocationProviderClient
    private var depLocation: IGeoPoint = GeoPoint(0.0, 0.0)
    private var arrLocation: IGeoPoint = GeoPoint(0.0, 0.0)

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

                if (depLat != null  && depLon != null && depLat != 0.0 && depLon != 0.0){
                    depLocation = GeoPoint(depLat!!, depLon!!)
                }

                if (arrLat != null  && arrLon != null && arrLat != 0.0 && arrLon != 0.0){
                    arrLocation = GeoPoint(arrLat!!, arrLon!!)
                }

                //set map start point to current location (dep or arr)
                startPoint = if (!isDeparture!!) {
                    if (arrLat != null && arrLat != 0.0) {
                        GeoPoint(arrLat!!, arrLon!!)
                    } else
                        GeoPoint(location.latitude, location.longitude)
                }else{
                    if (depLat != null && depLat != 0.0) {
                        GeoPoint(depLat!!, depLon!!)
                    } else
                        GeoPoint(location.latitude, location.longitude)
                }

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
        if (isDeparture!!)
            depLocation = position
        else
            arrLocation = position
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

        if (isDeparture!!) {
            startMarker.title = "Departure"
            startMarker.id = "dep"
        } else {
            startMarker.title = "Arrival"
            startMarker.id = "arr"
        }

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

                if (isDeparture!!){
                    if (depLocation.latitude != 0.0) {
                        gcd.getFromLocation(depLocation.latitude, depLocation.longitude, 1)
                            .stream().findFirst().ifPresent {
                                depCity = it.locality
                            }
                    }
                }else{
                    if (arrLocation.latitude != 0.0) {
                        gcd.getFromLocation(arrLocation.latitude, arrLocation.longitude, 1)
                            .stream().findFirst().ifPresent {
                                arrCity = it.locality
                            }
                    }
                }

                findNavController().navigate(
                    R.id.action_locationSelectorFragment_to_trip_edit,
                    Bundle().apply {
                        putBoolean("isDeparture", isDeparture!!)
                        putDouble("depLat", depLocation.latitude)
                        putDouble("depLon", depLocation.longitude)
                        putString("depCity", depCity)
                        putDouble("arrLat", arrLocation.latitude)
                        putDouble("arrLon", arrLocation.longitude)
                        putString("arrCity", arrCity)
                        putString("tripId", trip_id)
                        putBoolean("edit", edit!!)
                    })
                return true
            }
        }
        return false
    }
}