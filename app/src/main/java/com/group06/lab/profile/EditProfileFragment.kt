package com.group06.lab.profile

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.group06.lab.R
import java.io.File
import java.io.FileOutputStream

class EditProfileFragment : Fragment() {
    val REQUEST_IMAGE_CAPTURE = 1
    val REQUEST_IMAGE_GALLERY = 2
    private var profileChanged = false

    private lateinit var etFullName: EditText
    private lateinit var etNickName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etLocation: EditText
    private lateinit var imgProfile: ImageView
    private lateinit var snackBar: Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this,
            object: OnBackPressedCallback(true){
                override fun handleOnBackPressed() {
                    findNavController().navigate(R.id.action_editProfileActivity_to_showProfileActivity)
                }
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_activity_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val btnImageEdit = view.findViewById<ImageButton>(R.id.imageButtonEdit)
        // open context menu by clicking on btnImage
        btnImageEdit?.setOnClickListener {
            registerForContextMenu(btnImageEdit)
            activity?.openContextMenu(btnImageEdit)
            unregisterForContextMenu(btnImageEdit);
        }

        etFullName = view.findViewById(R.id.etFullName)
        etNickName = view.findViewById(R.id.etNickName)
        etEmail = view.findViewById(R.id.etEmail)
        etLocation = view.findViewById(R.id.etLocation)
        imgProfile = view.findViewById(R.id.imgProfile)

        etEmail.isEnabled = false
        etEmail.setText(FirebaseAuth.getInstance().currentUser!!.email!!)

        setFragmentResultListener("requestKeyShowToEdit") { _, bundle ->
            etFullName.setText(bundle.getString("group06.lab.fullName"))
            etNickName.setText(bundle.getString("group06.lab.nickName"))
            //etEmail.setText(bundle.getString("group06.lab.email"))
            etLocation.setText(bundle.getString("group06.lab.location"))
            val fileName: String = bundle.getString("group06.lab.profile") ?: ""
            File(context?.filesDir, fileName).let {
                if (it.exists()) imgProfile
                    .setImageBitmap(BitmapFactory.decodeFile(it.absolutePath))
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.activity_edit_save, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        activity?.menuInflater?.inflate(R.menu.activity_edit_profile_photo_menu, menu)
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
                if (profileChanged){
                    saveImageOnStorage(imgProfile.drawable.toBitmap())
                }

                var user = User(etFullName.text.toString(), etNickName.text.toString(),
                etEmail.text.toString(), etLocation.text.toString())

                val db = FirebaseFirestore.getInstance()
                db.collection("users")
                    .document(FirebaseAuth.getInstance().currentUser!!.email!!) //use the specified email as document id
                    .set(user)

                setFragmentResult(
                    "requestKeyEditToShow", bundleOf(
                        "group06.lab.fullName" to etFullName.text.toString(),
                        "group06.lab.nickName" to etNickName.text.toString(),
                        "group06.lab.email" to etEmail.text.toString(),
                        "group06.lab.location" to etLocation.text.toString(),
                        "group06.lab.profile" to "profilepic.jpg"
                    )
                )
                snackBar = Snackbar.make(requireView().getRootView().findViewById(
                    R.id.coordinatorLayout
                ), "Profile updated correctly", Snackbar.LENGTH_LONG)
                snackBar.setAction("Dismiss"){
                    snackBar.dismiss()
                }
                snackBar.show()
                findNavController().navigate(R.id.action_editProfileActivity_to_showProfileActivity)
                true
            }
            android.R.id.home -> { // the back button on action bar
                findNavController().navigate(R.id.action_editProfileActivity_to_showProfileActivity)
                true
            }

            else -> false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("group06.lab.fullName", etFullName.text.toString())
        outState.putString("group06.lab.nickName", etNickName.text.toString())
        outState.putString("group06.lab.email", etEmail.text.toString())
        outState.putString("group06.lab.location", etLocation.text.toString())
        outState.putString("group06.lab.image", "profilepictemp.jpg")
        outState.putBoolean("group06.lab.profileChanged", profileChanged)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            etFullName.setText(savedInstanceState.getString("group06.lab.fullName"))
            etNickName.setText(savedInstanceState.getString("group06.lab.nickName"))
            //etEmail.setText(savedInstanceState.getString("group06.lab.email"))
            etLocation.setText(savedInstanceState.getString("group06.lab.location"))
            profileChanged = savedInstanceState.getBoolean("group06.lab.profileChanged")
//            if (profileChanged)
            File(context?.filesDir, savedInstanceState.getString("group06.lab.image") ?: "").let {
                if (it.exists()) imgProfile.setImageBitmap(BitmapFactory.decodeFile(it.absolutePath))
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
            Toast.makeText(context, "error $e", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == AppCompatActivity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imgProfile.setImageBitmap(imageBitmap)
            profileChanged = true
            //saveImageOnStorage(imageBitmap, "profilepictemp.jpg")
        } else if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == AppCompatActivity.RESULT_OK) {
            imgProfile.setImageURI(data?.data)
            profileChanged = true
            //saveImageOnStorage(imgProfile.drawable.toBitmap(), "profilepictemp.jpg")
        }
    }

    private fun saveImageOnStorage(bitmap: Bitmap, tempName: String = "profilepic.jpg") {
        val file = File(context?.filesDir, tempName)
        val out = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        out.flush()
        out.close()
    }

}


