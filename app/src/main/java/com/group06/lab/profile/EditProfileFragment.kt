package com.group06.lab.profile

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import coil.load
import coil.request.CachePolicy
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.group06.lab.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class EditProfileFragment : Fragment() {
    val REQUEST_IMAGE_CAPTURE = 1
    val REQUEST_IMAGE_GALLERY = 2
    private var profileChanged = false

    private lateinit var etFullName: TextInputLayout
    private lateinit var etNickName: TextInputLayout
    private lateinit var etEmail: TextInputLayout
    private lateinit var etLocation: TextInputLayout
    private lateinit var imgProfile: ImageView
    private lateinit var snackBar: Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            unregisterForContextMenu(btnImageEdit)
        }

        etFullName = view.findViewById(R.id.etFullName)
        etNickName = view.findViewById(R.id.etNickName)
        etEmail = view.findViewById(R.id.etEmail)
        etLocation = view.findViewById(R.id.etLocation)
        imgProfile = view.findViewById(R.id.imgProfile)

        etEmail.isEnabled = false
        etEmail.editText?.setText(FirebaseAuth.getInstance().currentUser!!.email!!)

        snackBar = Snackbar.make(requireActivity().findViewById(android.R.id.content),
            "Profile updated correctly", Snackbar.LENGTH_LONG)
        snackBar.setAction("Dismiss"){
            snackBar.dismiss()
        }

        setFragmentResultListener("requestKeyShowToEdit") { _, bundle ->
            etFullName.editText?.setText(bundle.getString("group06.lab.fullName"))
            etNickName.editText?.setText(bundle.getString("group06.lab.nickName"))
            //etEmail.setText(bundle.getString("group06.lab.email"))
            etLocation.editText?.setText(bundle.getString("group06.lab.location"))
        }

        if (savedInstanceState != null){
            etFullName.editText?.setText(savedInstanceState.getString("group06.lab.fullName"))
            etNickName.editText?.setText(savedInstanceState.getString("group06.lab.nickName"))
            //etEmail.setText(savedInstanceState.getString("group06.lab.email"))
            etLocation.editText?.setText(savedInstanceState.getString("group06.lab.location"))

            profileChanged = savedInstanceState.getBoolean("group06.lab.profileChanged")
            if (profileChanged){
                File(context?.filesDir, savedInstanceState.getString("group06.lab.image") ?: "").let {
                    if (it.exists()) imgProfile.setImageBitmap(BitmapFactory.decodeFile(it.absolutePath))
                }
            }else{
                Firebase.storage.reference
                    .child(FirebaseAuth.getInstance().currentUser!!.email!!).downloadUrl
                    .addOnSuccessListener { uri ->
                        imgProfile.load(uri.toString()) {
                            memoryCachePolicy(CachePolicy.DISABLED) //to force reloading when image changes
                        }
                    }.addOnFailureListener {
                        imgProfile.setImageResource(R.drawable.ic_baseline_no_photography)
                    }
            }
        }else{
            Firebase.storage.reference
                .child(FirebaseAuth.getInstance().currentUser!!.email!!).downloadUrl
                .addOnSuccessListener { uri ->
                    imgProfile.load(uri.toString()) {
                        memoryCachePolicy(CachePolicy.DISABLED) //to force reloading when image changes
                    }
                }.addOnFailureListener {
                    imgProfile.setImageResource(R.drawable.ic_baseline_no_photography)
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

                val user = User(etFullName.editText?.text.toString(), etNickName.editText?.text.toString(),
                etEmail.editText?.text.toString(), etLocation.editText?.text.toString())

                val db = FirebaseFirestore.getInstance()
                db.collection("users")
                    .document(FirebaseAuth.getInstance().currentUser!!.email!!) //use the specified email as document id
                    .set(user)

                /*setFragmentResult(
                    "requestKeyEditToShow", bundleOf(
                        "group06.lab.fullName" to etFullName.text.toString(),
                        "group06.lab.nickName" to etNickName.text.toString(),
                        "group06.lab.email" to etEmail.text.toString(),
                        "group06.lab.location" to etLocation.text.toString(),
                        "group06.lab.profile" to "profilepic.jpg"
                    )
                )*/

                if (profileChanged){
                    saveImageOnStorage(imgProfile.drawable.toBitmap())
                    saveImageOnCloudStorage(imgProfile.drawable.toBitmap(), FirebaseAuth.getInstance().currentUser!!.email!!)
                        .addOnSuccessListener { taskSnapshot ->
                            snackBar.show()
                            findNavController().navigate(R.id.action_editProfileActivity_to_showProfileActivity)
                            view?.hideKeyboard()
                        }
                }else{
                    findNavController().navigate(R.id.action_editProfileActivity_to_showProfileActivity)
                    view?.hideKeyboard()
                }
                true
            }

            else -> false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("group06.lab.fullName", etFullName.editText?.text.toString())
        outState.putString("group06.lab.nickName", etNickName.editText?.text.toString())
        outState.putString("group06.lab.email", etEmail.editText?.text.toString())
        outState.putString("group06.lab.location", etLocation.editText?.text.toString())
        outState.putString("group06.lab.image", "profilepictemp.jpg")
        outState.putBoolean("group06.lab.profileChanged", profileChanged)
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
            saveImageOnStorage(imageBitmap, "profilepictemp.jpg")
        } else if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == AppCompatActivity.RESULT_OK) {
            imgProfile.setImageURI(data?.data)
            profileChanged = true
            saveImageOnStorage(imgProfile.drawable.toBitmap(), "profilepictemp.jpg")
        }
    }

    private fun saveImageOnStorage(bitmap: Bitmap, tempName: String = "profilepic.jpg") {
        val file = File(context?.filesDir, tempName)
        val out = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        out.flush()
        out.close()
    }

    private fun saveImageOnCloudStorage(bitmap: Bitmap, tempName: String): UploadTask {
        val storage = Firebase.storage
        val storageRef = storage.reference
        val imgRef = storageRef.child(tempName)

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        return imgRef.putBytes(data)
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

}


