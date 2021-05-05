package com.group06.lab1.profile


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.group06.lab1.R
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream


class ShowProfileFragment : Fragment() {
    private lateinit var tvFullName: TextView
    private lateinit var tvNickName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvLocation: TextView
    private lateinit var imgProfile: ImageView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // enable option edit
        setHasOptionsMenu(true)


        setFragmentResultListener("requestKeyEditToShow") { requestKey, bundle ->
            //tvFullName.text = bundle.getString("group06.lab1.fullName")
            //tvNickName.text = bundle.getString("group06.lab1.nickName")
            //tvEmail.text = bundle.getString("group06.lab1.email")
            //tvLocation.text = bundle.getString("group06.lab1.location")

            val fileName: String = bundle.getString("group06.lab1.profile") ?: ""
            File(context?.filesDir, fileName).let {
                if (it.exists()) imgProfile
                    .setImageBitmap(BitmapFactory.decodeFile(it.absolutePath))
            }

            val profileData = JSONObject().also {
                //it.put("fullName", tvFullName.text)
                //it.put("nickName", tvNickName.text)
                it.put("email", tvEmail.text)
                //it.put("location", tvLocation.text)
            }

            //store data persistently
            val sharedPref = requireActivity().getSharedPreferences(
                getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            )

            with(sharedPref.edit()) {
                putString("profile", profileData.toString())
                apply()
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

        val sharedPref = requireActivity().getSharedPreferences(
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
                        tvFullName.text = value["name"].toString()
                        tvNickName.text = value["nickName"].toString()
                        tvEmail.text = value["email"].toString()
                        tvLocation.text = value["location"].toString()
                    }
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
            tvFullName.text = data?.getStringExtra("group06.lab1.fullName")
            tvNickName.text = data?.getStringExtra("group06.lab1.nickName")
            tvEmail.text = data?.getStringExtra("group06.lab1.email")
            tvLocation.text = data?.getStringExtra("group06.lab1.location")
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
            //serialize data into a JSON object
            val profileData = JSONObject().also {
                it.put("fullName", tvFullName.text)
                it.put("nickName", tvNickName.text)
                it.put("email", tvEmail.text)
                it.put("location", tvLocation.text)
            }

            //store data persistently
            val sharedPref = requireActivity().getSharedPreferences(
                getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            )

            with(sharedPref.edit()) {
                putString("profile", profileData.toString())
                apply()
            }


        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("group06.lab1.fullName", tvFullName.text.toString())
        outState.putString("group06.lab1.nickName", tvNickName.text.toString())
        outState.putString("group06.lab1.email", tvEmail.text.toString())
        outState.putString("group06.lab1.location", tvLocation.text.toString())
        outState.putString("group06.lab1.profile", "profilepic.jpg")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            tvFullName.text = savedInstanceState.getString("group06.lab1.fullName")
            tvNickName.text = savedInstanceState.getString("group06.lab1.nickName")
            tvEmail.text = savedInstanceState.getString("group06.lab1.email")
            tvLocation.text = savedInstanceState.getString("group06.lab1.location")
            File(
                context?.filesDir,
                savedInstanceState.getString("group06.lab1.profile") ?: ""
            ).let {
                if (it.exists()) imgProfile.setImageBitmap(BitmapFactory.decodeFile(it.absolutePath))
            }
        }
    }


    private fun editProfile() {
        setFragmentResult(
            "requestKeyShowToEdit", bundleOf(
                "group06.lab1.fullName" to tvFullName.text.toString(),
                "group06.lab1.nickName" to tvNickName.text.toString(),
                "group06.lab1.email" to tvEmail.text.toString(),
                "group06.lab1.location" to tvLocation.text.toString(),
                "group06.lab1.profile" to "profilepic.jpg"
            )

        )
        findNavController().navigate(R.id.action_showProfileActivity_to_editProfileActivity)
    }
}