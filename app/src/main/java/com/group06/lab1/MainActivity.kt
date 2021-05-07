package com.group06.lab1

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.fragment.app.commit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.group06.lab1.login.LoginActivity
import com.group06.lab1.ui.trip.TripListFragment
import com.group06.lab1.utils.Database
import org.json.JSONObject
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var mAuth : FirebaseAuth

    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var tvHeaderName: TextView
    private lateinit var ivHeaderProfileImage: ImageView
    private lateinit var tvHeaderEmail: TextView
    private lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // if user is not authenticated, send them to the LogInActivity
        mAuth = FirebaseAuth.getInstance()
        val user = mAuth.currentUser
        if (user == null){
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }


        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

        loadData()

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.trip_list
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    /*override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }*/

    fun loadData() {
        val drawer = navView.getHeaderView(0)

        tvHeaderName = drawer.findViewById(R.id.headerName)
        ivHeaderProfileImage = drawer.findViewById(R.id.headerProfileImage)
        tvHeaderEmail = drawer.findViewById(R.id.headermail)

        //TODO now it uses email stored in the device, it should be changed with the firebase account email
        val sharedPref = getSharedPreferences(
            getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        )

        val data = sharedPref.getString("profile", null)

        if (data != null){
            val db = FirebaseFirestore.getInstance()
            db.collection("users")
                .document(JSONObject(data).getString("email"))
                .addSnapshotListener{
                    value, error ->
                    if (error != null) throw error
                    if (value != null){
                        tvHeaderName.text = value["name"].toString()
                        tvHeaderEmail.text = value["email"].toString()
                    }
                }
        }

        File(filesDir, "profilepic.jpg").let {
            if (it.exists()) ivHeaderProfileImage.setImageBitmap(BitmapFactory.decodeFile(it.absolutePath))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        loadData()
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


}