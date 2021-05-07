package com.group06.lab.profile


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.Visibility
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.provider.FirebaseInitProvider
import com.group06.lab.R
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream


class ShowProfileFragment : Fragment() {
    private lateinit var tvFullName: TextView
    private lateinit var tvNickName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvLocation: TextView
    private lateinit var imgProfile: ImageView

    private lateinit var snackbar: Snackbar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // enable option edit
        setHasOptionsMenu(true)


        setFragmentResultListener("requestKeyEditToShow") { requestKey, bundle ->

            val fileName: String = bundle.getString("group06.lab.profile") ?: ""
            File(context?.filesDir, fileName).let {
                if (it.exists()) imgProfile
                    .setImageBitmap(BitmapFactory.decodeFile(it.absolutePath))
            }
        }


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_activity_show_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvFullName = view.findViewById(R.id.tvFullName)
        tvNickName = view.findViewById(R.id.tvNickName)
        tvEmail = view.findViewById(R.id.tvEmail)
        tvLocation = view.findViewById(R.id.tvLocation)
        imgProfile = view.findViewById(R.id.imgProfile)

        val fullNameLayout: LinearLayout = view.findViewById(R.id.fullNameLayout)
        val nicknameLayout: LinearLayout = view.findViewById(R.id.nicknameLayout)
        val locationLayout: LinearLayout = view.findViewById(R.id.locationLayout)

        snackbar = Snackbar.make( requireView().getRootView().findViewById(
            R.id.coordinatorLayout
        ), "Your profile seems empty, update it!", Snackbar.LENGTH_INDEFINITE)

        snackbar.setAction("Update"){
            findNavController().navigate(R.id.action_showProfileActivity_to_editProfileActivity)
        }

        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(FirebaseAuth.getInstance().currentUser!!.email!!)
            .addSnapshotListener{
                    value, error ->
                if (error != null) throw error
                if (value != null){
                    if (value.data?.isEmpty()!!){ //profile is empty
                        fullNameLayout.visibility = View.GONE
                        nicknameLayout.visibility = View.GONE
                        locationLayout.visibility = View.GONE

                        snackbar.show()
                    }else{
                        fullNameLayout.visibility = View.VISIBLE
                        nicknameLayout.visibility = View.VISIBLE
                        locationLayout.visibility = View.VISIBLE

                        tvFullName.text = value["name"].toString()
                        tvNickName.text = value["nickName"].toString()
                        tvLocation.text = value["location"].toString()
                    }

                    tvEmail.text = FirebaseAuth.getInstance().currentUser!!.email!!
                }
            }

        File(context?.filesDir, "profilepic.jpg").let {
            if (it.exists()) imgProfile.setImageBitmap(BitmapFactory.decodeFile(it.absolutePath))
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.activity_show_profile_menu, menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.editMenu -> {
                editProfile()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            tvFullName.text = data?.getStringExtra("group06.lab.fullName")
            tvNickName.text = data?.getStringExtra("group06.lab.nickName")
            tvEmail.text = data?.getStringExtra("group06.lab.email")
            tvLocation.text = data?.getStringExtra("group06.lab.location")
            File(context?.filesDir, "profilepic.jpg").let {
                if (it.exists()) {
                    imgProfile.setImageBitmap(BitmapFactory.decodeFile(it.absolutePath))
                    val file = File(context?.filesDir, "profilepic.jpg")
                    val out = FileOutputStream(file)
                    imgProfile.drawable.toBitmap().compress(Bitmap.CompressFormat.JPEG, 85, out)
                    out.flush()
                    out.close()
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("group06.lab.fullName", tvFullName.text.toString())
        outState.putString("group06.lab.nickName", tvNickName.text.toString())
        outState.putString("group06.lab.email", tvEmail.text.toString())
        outState.putString("group06.lab.location", tvLocation.text.toString())
        outState.putString("group06.lab.profile", "profilepic.jpg")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            tvFullName.text = savedInstanceState.getString("group06.lab.fullName")
            tvNickName.text = savedInstanceState.getString("group06.lab.nickName")
            tvEmail.text = savedInstanceState.getString("group06.lab.email")
            tvLocation.text = savedInstanceState.getString("group06.lab.location")
            File(
                context?.filesDir,
                savedInstanceState.getString("group06.lab.profile") ?: ""
            ).let {
                if (it.exists()) imgProfile.setImageBitmap(BitmapFactory.decodeFile(it.absolutePath))
            }
        }
    }

    private fun editProfile() {
        setFragmentResult(
            "requestKeyShowToEdit", bundleOf(
                "group06.lab.fullName" to tvFullName.text.toString(),
                "group06.lab.nickName" to tvNickName.text.toString(),
                "group06.lab.email" to tvEmail.text.toString(),
                "group06.lab.location" to tvLocation.text.toString(),
                "group06.lab.profile" to "profilepic.jpg"
            )
        )
        if (snackbar.isShown)
            snackbar.dismiss()
        findNavController().navigate(R.id.action_showProfileActivity_to_editProfileActivity)
    }
}