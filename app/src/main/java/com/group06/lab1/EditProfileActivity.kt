package com.group06.lab1

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap


class EditProfileActivity : AppCompatActivity() {
    val REQUEST_IMAGE_CAPTURE = 1
    val REQUEST_IMAGE_GALLERY = 2


    private lateinit var etFullName : EditText
    private lateinit var etNickName : EditText
    private lateinit var etEmail : EditText
    private lateinit var etLocation : EditText
    private lateinit var imgProfile : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val btnImageEdit = findViewById<ImageButton>(R.id.imageButtonEdit)
        registerForContextMenu(btnImageEdit)

        etFullName = findViewById<EditText>(R.id.etFullName)
        etNickName = findViewById<EditText>(R.id.etNickName)
        etEmail = findViewById<EditText>(R.id.etEmail)
        etLocation =  findViewById<EditText>(R.id.etLocation)
        imgProfile = findViewById<ImageView>(R.id.imgProfile)

        etFullName.setText(intent.getStringExtra("fullName"))
        etNickName.setText(intent.getStringExtra("nickName"))
        etEmail.setText(intent.getStringExtra("email"))
        etLocation.setText(intent.getStringExtra("location"))
        imgProfile.setImageBitmap(intent?.extras?.getParcelable("profile"))

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.activity_edit_profile_menu, menu)
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
        return when (item.itemId) {
            R.id.addGallery -> {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                startActivityForResult(intent, REQUEST_IMAGE_GALLERY)
                true
            }
            R.id.addCamera -> {
                dispatchTakePictureIntent()
                true
            }
            else -> false
        }
//        return super.onContextItemSelected(item)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        imgProfile.invalidate()
        return when (item.itemId) {
            R.id.btnSubmit -> {
                setResult(Activity.RESULT_OK, Intent().also {
                    it.putExtra("result", etFullName.text.toString())
                    it.putExtra("fullName", etFullName.text.toString())
                    it.putExtra("nickName", etNickName.text.toString())
                    it.putExtra("email", etEmail.text.toString())
                    it.putExtra("location", etLocation.text.toString())
                    it.putExtra("profile", imgProfile.drawable.toBitmap())
                })
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("fullName", etFullName.text.toString())
        outState.putString("nickName", etNickName.text.toString())
        outState.putString("email", etEmail.text.toString())
        outState.putString("location", etLocation.text.toString())
        outState.putParcelable("image", imgProfile.drawable.toBitmap())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        etFullName.setText(savedInstanceState.getString("fullName"))
        etNickName.setText(savedInstanceState.getString("nickName"))
        etEmail.setText(savedInstanceState.getString("email"))
        etLocation.setText(savedInstanceState.getString("location"))
        imgProfile.setImageBitmap(savedInstanceState.getParcelable("image"))
    }

    // ---------------- Use the camera to take a picture
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

        } else if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK) {
//            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, data?.data)
//            imgProfile.setImageBitmap(bitmap)
            imgProfile.setImageURI(data?.data)
        }
    }
// ---------------- Use the camera to take a picture

    // TODO moving data btw two activities

}


