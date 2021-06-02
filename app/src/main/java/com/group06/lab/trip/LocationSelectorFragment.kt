package com.group06.lab.trip

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.StrictMode
import android.preference.PreferenceManager
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.group06.lab.R
import org.osmdroid.api.IGeoPoint
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.Polyline
import java.util.*


private var isDeparture: Boolean? = true
private var edit: Boolean? = false
private var index = -1
private var tripId: String? = ""
private var depLat: Double? = 0.0
private var arrLat: Double? = 0.0
private var depLon: Double? = 0.0
private var arrLon: Double? = 0.0

class LocationSelectorFragment : Fragment() {
    private lateinit var map: MapView;
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private lateinit var point: IGeoPoint
    private lateinit var startMarker: Marker

    private lateinit var client: FusedLocationProviderClient
    private var depLocation: IGeoPoint = GeoPoint(0.0, 0.0)
    private var arrLocation: IGeoPoint = GeoPoint(0.0, 0.0)

    private val vm by viewModels<TripViewModel>()

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
        index = arguments?.getInt("index")!!
        tripId = arguments?.getString("tripId")

        map = view.findViewById<MapView>(R.id.mapLocationSelector)
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
                    startPoint = if (depLat != 0.0 && depLat != null) {
                        depLocation = GeoPoint(depLat!!, depLon!!)
                        GeoPoint(depLat!!, depLon!!)
                    } else
                        GeoPoint(location.latitude, location.longitude)

                if (!isDeparture!!) {
                    arrLat = arguments?.getDouble("arrLat")
                    arrLon = arguments?.getDouble("arrLon")
                }

                mapController.setCenter(startPoint)

                if (!isDeparture!!) {

                    depLat = arguments?.getDouble("depLat")
                    depLon = arguments?.getDouble("depLon")
                    if (depLat != 0.0 && depLat != null) {
                        depLocation = GeoPoint(depLat!!, depLon!!)
                    }

                    point = startPoint
                    startMarker = Marker(map)
                    startMarker.position = if (depLat != 0.0 && depLat != null) {
                        GeoPoint(depLat!!, depLon!!)
                    } else {
                        GeoPoint(startPoint.latitude, startPoint.longitude)
                    }

                    startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                    map.overlays
                        .forEach { (it as? Marker)?.remove(map) }
                    startMarker.title = "Departure"
                    startMarker.id = "dep"
                    map.overlays.add(startMarker)
                }

                val gcd = Geocoder(context, Locale.getDefault())
                val addresses: List<Address> =
                    gcd.getFromLocation(location.latitude, location.longitude, 1)
                if (addresses.isNotEmpty()) {
                    println(addresses[0].locality)
                }
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

                point = geoPoint
                startMarker = Marker(map)
                startMarker.position = GeoPoint(geoPoint.latitude, geoPoint.longitude)
                if (isDeparture!!)
                    depLocation = geoPoint
                else
                    arrLocation = geoPoint
                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                if (isDeparture!!) {
                    mapView.overlays
                        .forEach { (it as? Marker)?.remove(mapView) }
                    startMarker.title = "Departure"
                    startMarker.id = "dep"
                } else {
                    mapView.overlays
                        .forEach {
                            if ((it as? Marker)?.id != "dep") (it as? Marker)?.remove(
                                mapView
                            )
                        }
                    startMarker.title = "Arrival"
                    startMarker.id = "arr"
                }

                map.overlays.add(startMarker)
                mapController.setCenter(startMarker.position)

                if (depLocation.latitude != 0.0 && arrLocation.latitude != 0.0) {
                    val origin = GeoPoint(depLocation.latitude, depLocation.longitude)
                    val destination = GeoPoint(arrLocation.latitude, arrLocation.longitude)
                    mapController.setCenter(destination)

                    val roadManager: RoadManager =
                        OSRMRoadManager(requireContext(), "OBP_Tuto/1.0")

                    mapView.overlays
                        .forEach { if(it is Polyline)  mapView.overlays.remove(it as Overlay) }

                    val wayPoints = ArrayList<GeoPoint>()
                    wayPoints.add(origin)
                    wayPoints.add(destination)
                    val road = roadManager.getRoad(wayPoints)
                    val roadOverlay = RoadManager.buildRoadOverlay(road)
                    roadOverlay.id = "path"
                    map.overlays.add(roadOverlay);
                    map.invalidate();
                }

                return true
            }
        })
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
                var depCity = ""
                var arrCity = ""
                var addresses: List<Address> =
                    gcd.getFromLocation(depLocation.latitude, depLocation.longitude, 1)
                if (addresses.isNotEmpty()) {
                    depCity = addresses[0].locality
                }
                if (arrLocation.latitude != 0.0) {
                    addresses =
                        gcd.getFromLocation(arrLocation.latitude, arrLocation.longitude, 1)
                    if (addresses.isNotEmpty()) {
                        arrCity = addresses[0].locality
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
                        putInt("index", index)
                        putString("tripId", tripId)
                        putBoolean("edit", true)
                    })
                return true
            }
        }
        return false
    }
}