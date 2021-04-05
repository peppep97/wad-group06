package com.group06.lab1

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
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

    private lateinit var etFullName: EditText
    private lateinit var etNickName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etLocation: EditText
    private lateinit var imgProfile: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

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

        etFullName.setText(intent.getStringExtra("group06.lab1.fullName"))
        etNickName.setText(intent.getStringExtra("group06.lab1.nickName"))
        etEmail.setText(intent.getStringExtra("group06.lab1.email"))
        etLocation.setText(intent.getStringExtra("group06.lab1.location"))
        val img: Bitmap? = intent.getParcelableExtra("group06.lab1.profile")
        img?.let { imgProfile.setImageBitmap(img) }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
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
                setResult(Activity.RESULT_OK, Intent().also {
                    it.putExtra("group06.lab1.result", etFullName.text.toString())
                    it.putExtra("group06.lab1.fullName", etFullName.text.toString())
                    it.putExtra("group06.lab1.nickName", etNickName.text.toString())
                    it.putExtra("group06.lab1.email", etEmail.text.toString())
                    it.putExtra("group06.lab1.location", etLocation.text.toString())
                    it.putExtra(
                        "group06.lab1.profile",
                        scaleDownBitmap(imgProfile.drawable.toBitmap(), 100, this)
                    )
                })
                finish()
                true

            }
            else -> false
        }
    }

    // function to scale down the BitMap Image for transferring it to another activity
    private fun scaleDownBitmap(photo: Bitmap, newHeight: Int, context: Context): Bitmap? {
        var photo = photo
        val densityMultiplier: Float = context.getResources().getDisplayMetrics().density
        val h = (newHeight * densityMultiplier).toInt()
        val w = (h * photo.width / photo.height.toDouble()).toInt()
        photo = Bitmap.createScaledBitmap(photo, w, h, true)
        return photo
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("group06.lab1.fullName", etFullName.text.toString())
        outState.putString("group06.lab1.nickName", etNickName.text.toString())
        outState.putString("group06.lab1.email", etEmail.text.toString())
        outState.putString("group06.lab1.location", etLocation.text.toString())
        outState.putParcelable("group06.lab1.image", imgProfile.drawable.toBitmap())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        etFullName.setText(savedInstanceState.getString("group06.lab1.fullName"))
        etNickName.setText(savedInstanceState.getString("group06.lab1.nickName"))
        etEmail.setText(savedInstanceState.getString("group06.lab1.email"))
        etLocation.setText(savedInstanceState.getString("group06.lab1.location"))
        imgProfile.setImageBitmap(savedInstanceState.getParcelable("group06.lab1.image"))
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

        } else if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK) {
            imgProfile.setImageURI(data?.data)
        }
    }

}


