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


class EditProfileActivity : AppCompatActivity() {
    val REQUEST_IMAGE_CAPTURE = 1


    private lateinit var etFullName : EditText
    private lateinit var etNickName : EditText
    private lateinit var etEmail : EditText
    private lateinit var etLocation : EditText
    private lateinit var imgView : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val btnImageEdit = findViewById<ImageButton>(R.id.imageButtonEdit)
        registerForContextMenu(btnImageEdit)

        etFullName = findViewById<EditText>(R.id.etFullName)
        etNickName = findViewById<EditText>(R.id.etNickName)
        etEmail = findViewById<EditText>(R.id.etEmail)
        etLocation =  findViewById<EditText>(R.id.etLocation)
        imgView = findViewById<ImageView>(R.id.imgProfile)

        etFullName.setText(intent.getStringExtra("fullName"))
        etNickName.setText(intent.getStringExtra("nickName"))
        etEmail.setText(intent.getStringExtra("email"))
        etLocation.setText(intent.getStringExtra("location"))





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
                Toast.makeText(applicationContext, "Option 1 Selected", Toast.LENGTH_SHORT).show()
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
        return when (item.itemId) {
            R.id.btnSubmit -> {
                setResult(Activity.RESULT_OK, Intent().also {
                    it.putExtra("result", etFullName.text.toString())
                    it.putExtra("fullName", etFullName.text.toString())
                    it.putExtra("nickName", etNickName.text.toString())
                    it.putExtra("email", etEmail.text.toString())
                    it.putExtra("location", etLocation.text.toString())
                })
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
                imgView.setImageBitmap(imageBitmap)

        }
    }
// ---------------- Use the camera to take a picture


    // TODO fixing rotation of the Image
    // TODO moving data btw tow activities

}


