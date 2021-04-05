package com.group06.lab1

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream


class ShowProfileActivity : AppCompatActivity() {
    private lateinit var tvFullName: TextView
    private lateinit var tvNickName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvLocation: TextView
    private lateinit var imgProfile: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_profile)

        tvFullName = findViewById(R.id.tvFullName)
        tvNickName = findViewById(R.id.tvNickName)
        tvEmail = findViewById(R.id.tvEmail)
        tvLocation = findViewById(R.id.tvLocation)
        imgProfile = findViewById(R.id.imgProfile)

        val sharedPref = getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )

        val data = sharedPref.getString("profile", null)
        if (data != null)
            with(JSONObject(data)) {
                tvFullName.text = getString("fullName")
                tvNickName.text = getString("nickName")
                tvEmail.text = getString("email")
                tvLocation.text = getString("location")
            }

        File(filesDir, "profilepic.jpg").let {
            if (it.exists()) imgProfile.setImageBitmap(BitmapFactory.decodeFile(it.absolutePath))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.activity_show_profile_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.editMenu -> {
                editProfile()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            tvFullName.text = data?.getStringExtra("group06.lab1.fullName")
            tvNickName.text = data?.getStringExtra("group06.lab1.nickName")
            tvEmail.text = data?.getStringExtra("group06.lab1.email")
            tvLocation.text = data?.getStringExtra("group06.lab1.location")
            val img: Bitmap? = data?.getParcelableExtra("group06.lab1.profile")
            img?.let { imgProfile.setImageBitmap(img) }

            //serialize data into a JSON object
            val profileData = JSONObject().also {
                it.put("fullName", tvFullName.text)
                it.put("nickName", tvNickName.text)
                it.put("email", tvEmail.text)
                it.put("location", tvLocation.text)
            }

            //store data persistently
            val sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE
            )

            with(sharedPref.edit()) {
                putString("profile", profileData.toString())
                apply()
            }

            //var file = it.getDir("Images", Context.MODE_PRIVATE)
            val file = File(filesDir, "profilepic.jpg")
            val out = FileOutputStream(file)
            imgProfile.drawable.toBitmap().compress(Bitmap.CompressFormat.JPEG, 85, out)
            out.flush()
            out.close()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("group06.lab1.fullName", tvFullName.text.toString())
        outState.putString("group06.lab1.nickName", tvNickName.text.toString())
        outState.putString("group06.lab1.email", tvEmail.text.toString())
        outState.putString("group06.lab1.location", tvLocation.text.toString())
        outState.putParcelable("group06.lab1.profile", imgProfile.drawable.toBitmap())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        tvFullName.text = savedInstanceState.getString("group06.lab1.fullName")
        tvNickName.text = savedInstanceState.getString("group06.lab1.nickName")
        tvEmail.text = savedInstanceState.getString("group06.lab1.email")
        tvLocation.text = savedInstanceState.getString("group06.lab1.location")
        imgProfile.setImageBitmap(savedInstanceState.getParcelable("group06.lab1.profile"))
    }

    private fun editProfile() {
        Intent(this, EditProfileActivity::class.java).also {
            it.putExtra("group06.lab1.fullName", tvFullName.text.toString())
            it.putExtra("group06.lab1.nickName", tvNickName.text.toString())
            it.putExtra("group06.lab1.email", tvEmail.text.toString())
            it.putExtra("group06.lab1.location", tvLocation.text.toString())
            it.putExtra("group06.lab1.profile", imgProfile.drawable.toBitmap())
            startActivityForResult(it, 1)
        }
    }
}
