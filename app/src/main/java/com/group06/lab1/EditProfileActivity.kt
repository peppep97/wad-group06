package com.group06.lab1

import android.app.Activity
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
import java.io.File
import java.io.FileOutputStream

class EditProfileActivity : AppCompatActivity() {
    val REQUEST_IMAGE_CAPTURE = 1
    val REQUEST_IMAGE_GALLERY = 2
    private var profileChanged = false

    private lateinit var etFullName: EditText
    private lateinit var etNickName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etLocation: EditText
    private lateinit var imgProfile: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val btnImageEdit = findViewById<ImageButton>(R.id.imageButtonEdit)
        // open context menu by clicking on btnImage
        btnImageEdit.setOnClickListener {
            registerForContextMenu(btnImageEdit)
            openContextMenu(btnImageEdit)
            unregisterForContextMenu(btnImageEdit);
        }

        etFullName = findViewById(R.id.etFullName)
        etNickName = findViewById(R.id.etNickName)
        etEmail = findViewById(R.id.etEmail)
        etLocation = findViewById(R.id.etLocation)
        imgProfile = findViewById(R.id.imgProfile)

//        imgProfile.setImageBitmap(imgProfile.drawable.toBitmap())

        etFullName.setText(intent.getStringExtra("group06.lab1.fullName"))
        etNickName.setText(intent.getStringExtra("group06.lab1.nickName"))
        etEmail.setText(intent.getStringExtra("group06.lab1.email"))
        etLocation.setText(intent.getStringExtra("group06.lab1.location"))
        val fileName: String = intent.getStringExtra("group06.lab1.profile") ?: ""
        File(filesDir, fileName).let {
            if (it.exists()) imgProfile.setImageBitmap(BitmapFactory.decodeFile(it.absolutePath))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_edit_save, menu)
        return true
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.activity_edit_profile_photo_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        imgProfile.invalidate()
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.saveEdit -> {
                if (profileChanged)
                    saveImageOnStorage(imgProfile.drawable.toBitmap())
                setResult(Activity.RESULT_OK, Intent().also {
                    it.putExtra("group06.lab1.result", etFullName.text.toString())
                    it.putExtra("group06.lab1.fullName", etFullName.text.toString())
                    it.putExtra("group06.lab1.nickName", etNickName.text.toString())
                    it.putExtra("group06.lab1.email", etEmail.text.toString())
                    it.putExtra("group06.lab1.location", etLocation.text.toString())
                    it.putExtra("group06.lab1.profile", "profilepic.jpg")
                })
                finish()
                true
            }
            16908332 -> { // the back button on action bar
                finish()
                true
            }

            else -> false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("group06.lab1.fullName", etFullName.text.toString())
        outState.putString("group06.lab1.nickName", etNickName.text.toString())
        outState.putString("group06.lab1.email", etEmail.text.toString())
        outState.putString("group06.lab1.location", etLocation.text.toString())
        outState.putString("group06.lab1.image", "profilepictemp.jpg")
        outState.putBoolean("group06.lab1.profileChanged", profileChanged)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        etFullName.setText(savedInstanceState.getString("group06.lab1.fullName"))
        etNickName.setText(savedInstanceState.getString("group06.lab1.nickName"))
        etEmail.setText(savedInstanceState.getString("group06.lab1.email"))
        etLocation.setText(savedInstanceState.getString("group06.lab1.location"))
        profileChanged = savedInstanceState.getBoolean("group06.lab1.profileChanged")
        if (profileChanged)
            File(filesDir, savedInstanceState.getString("group06.lab1.image") ?: "").let {
                if (it.exists()) imgProfile.setImageBitmap(BitmapFactory.decodeFile(it.absolutePath))
            }
    }

    // ---------------- function for activating the camera to take a picture
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
            Toast.makeText(applicationContext, "error $e", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imgProfile.setImageBitmap(imageBitmap)
            profileChanged = true
            saveImageOnStorage(imageBitmap, "profilepictemp.jpg")
        } else if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK) {
            imgProfile.setImageURI(data?.data)
            profileChanged = true
            saveImageOnStorage(imgProfile.drawable.toBitmap(), "profilepictemp.jpg")
        }
    }

    private fun saveImageOnStorage(bitmap: Bitmap, tempName: String = "profilepic.jpg") {
        val file = File(filesDir, tempName)
        val out = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        out.flush()
        out.close()
    }

}


