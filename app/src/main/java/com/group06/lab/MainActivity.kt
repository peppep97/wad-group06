package com.group06.lab

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import coil.load
import coil.request.CachePolicy
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.group06.lab.profile.ShowProfileFragment
import com.group06.lab.trip.OthersTripListFragment
import com.group06.lab.trip.TripListFragment

class MainActivity : AppCompatActivity() {
    companion object {
        lateinit var mAuth: FirebaseAuth
    }

    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var tvHeaderName: TextView
    private lateinit var ivHeaderProfileImage: ImageView
    private lateinit var tvHeaderEmail: TextView
    private lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

        mAuth = FirebaseAuth.getInstance()

        loadData()

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.trip_list,
                R.id.my_trip_list,
                R.id.interest_trip_list,
                R.id.bought_trip_list,
                R.id.show_profile
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private fun loadData() {
        val drawer = navView.getHeaderView(0)

        tvHeaderName = drawer.findViewById(R.id.headerName)
        ivHeaderProfileImage = drawer.findViewById(R.id.headerProfileImage)
        tvHeaderEmail = drawer.findViewById(R.id.headermail)

        val db = FirebaseFirestore.getInstance()
        val userDoc = db.collection("users").document(mAuth.currentUser!!.email!!)

        userDoc
            .get()
            .addOnCompleteListener { value ->
                if (value.isSuccessful) {
                    if (!value.result?.exists()!!) {
                        userDoc.set(HashMap<String, Any>()) //add empty document
                    }
                }
            }
        userDoc.addSnapshotListener { value, error ->
            if (error != null) throw error
            if (value != null) {
                if (value["name"] != null)
                    tvHeaderName.text = value["name"].toString()
                tvHeaderEmail.text = mAuth.currentUser!!.email!!
            }
        }

        Firebase.storage.reference
            .child(FirebaseAuth.getInstance().currentUser!!.email!!).downloadUrl
            .addOnSuccessListener { uri ->
                ivHeaderProfileImage.load(uri.toString()) {
                    memoryCachePolicy(CachePolicy.DISABLED) //to force reloading when image changes
                }
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        loadData()
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        //prevent going back to login activity
        val navFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        val mapFragment = navFragment!!.childFragmentManager.primaryNavigationFragment
        if (mapFragment !is OthersTripListFragment && mapFragment !is TripListFragment && mapFragment !is ShowProfileFragment){
            super.onBackPressed()
        }
    }
}