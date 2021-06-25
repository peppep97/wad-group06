package com.group06.lab.trip

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.StrictMode
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.group06.lab.R
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

class TripRouteFragment : Fragment() {
    private lateinit var map: MapView
    private lateinit var client: FusedLocationProviderClient

    private var depLat: Double? = 0.0
    private var arrLat: Double? = 0.0
    private var depLon: Double? = 0.0
    private var arrLon: Double? = 0.0
    private var stopList: List<IntermediateStop>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(
            activity,
            PreferenceManager.getDefaultSharedPreferences(activity)
        )

        depLat = arguments?.getDouble("depLat")
        depLon = arguments?.getDouble("depLon")
        arrLat = arguments?.getDouble("arrLat")
        arrLon = arguments?.getDouble("arrLon")
        stopList = arguments?.getParcelableArrayList("stops")

        client = LocationServices.getFusedLocationProviderClient(requireActivity())
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_trip_route, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        map = view.findViewById(R.id.mapRoute)
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true)
        val mapController = map.controller
        mapController.setZoom(13)
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

                if (depLat != 0.0 && depLon != 0.0 &&
                    arrLat != 0.0 && arrLon != 0.0
                ) {
                    val origin = GeoPoint(depLat!!, depLon!!)
                    val destination = GeoPoint(arrLat!!, arrLon!!)

                    if (origin.latitude != 0.0 && origin.longitude != 0.0 &&
                        destination.latitude != 0.0 && destination.longitude != 0.0
                    ) {

                        val gcd = Geocoder(context, Locale.getDefault())
                        val addresses: List<Address> =
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

                        startMarker.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_location_green)

                        map.overlays?.add(startMarker)

                        val wayPoints = ArrayList<GeoPoint>()
                        wayPoints.add(origin)

                        if (stopList?.isNotEmpty()!!){
                            stopList!!.forEach {
                                val stop = it

                                wayPoints.add(GeoPoint(stop.lat, stop.lon))

                                val addresses: List<Address> =
                                    gcd.getFromLocation(stop.lat, stop.lon, 1)

                                val marker = Marker(map)
                                marker.position = GeoPoint(stop.lat, stop.lon)
                                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                if (addresses.isNotEmpty())
                                    marker.title =
                                        "${addresses[0].locality} - ${addresses[0].countryName}\n${addresses[0].thoroughfare ?: ""} ${addresses[0].subThoroughfare ?: ""}"
                                else
                                    marker.title = "Intermediate Stop"

                                marker.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_location_blue)

                                map.overlays?.add(marker)
                            }
                        }

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

                        endMarker.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_location_red_48)

                        map.overlays?.add(startMarker)
                        map.overlays?.add(endMarker)

                        mapController.setCenter(origin)

                        val roadManager: RoadManager =
                            OSRMRoadManager(requireContext(), "OBP_Tuto/1.0")

                        map.overlays
                            .forEach { o -> if (o is Polyline) map.overlays.remove(o as Overlay) }


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
                                    mapController.setCenter(
                                        GeoPoint(
                                            location.latitude,
                                            location.longitude
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
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
}