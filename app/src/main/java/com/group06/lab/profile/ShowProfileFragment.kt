package com.group06.lab.profile

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.graphics.drawable.toBitmap
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import coil.load
import coil.request.CachePolicy
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.group06.lab.MainActivity
import com.group06.lab.R
import com.group06.lab.login.LoginActivity
import com.group06.lab.trip.tripId
import java.io.File
import java.io.FileOutputStream

class ShowProfileFragment : Fragment() {
    private lateinit var tvFullName: TextView
    private lateinit var tvNickName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvLocation: TextView
    private lateinit var imgProfile: ImageView
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var fullNameLayout: LinearLayout
    private lateinit var emailLayout: LinearLayout
    private lateinit var db: FirebaseFirestore
    private lateinit var ratingBarProfile : RatingBar
    private lateinit var rateButton : Button


    private lateinit var snackbar: Snackbar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // enable option edit
        setHasOptionsMenu(true)

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_activity_show_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val logoutButton = view.findViewById<Button>(R.id.logout_button)

        val emailParameter : String? = arguments?.getString("email")
        val rating : Boolean? = arguments?.getBoolean("Rating")
        val userId : String? = arguments?.getString("userId")

        tvFullName = view.findViewById(R.id.tvFullName)
        tvNickName = view.findViewById(R.id.tvNickName)
        tvEmail = view.findViewById(R.id.tvEmail)
        tvLocation = view.findViewById(R.id.tvLocation)
        imgProfile = view.findViewById(R.id.imgProfile)
        fullNameLayout = view.findViewById(R.id.fullNameLayout)
        emailLayout = view.findViewById(R.id.emailLayout)
        ratingBarProfile = view.findViewById(R.id.ratingbarprofile)
        rateButton = view.findViewById(R.id.RateButtonProfile)


        val fullNameLayout: LinearLayout = view.findViewById(R.id.fullNameLayout)
        val nicknameLayout: LinearLayout = view.findViewById(R.id.nicknameLayout)
        val locationLayout: LinearLayout = view.findViewById(R.id.locationLayout)

        snackbar = Snackbar.make(requireActivity().findViewById(android.R.id.content),
            "Your profile seems empty, update it!", Snackbar.LENGTH_INDEFINITE)

        snackbar.setAction("Update"){
            findNavController().navigate(R.id.action_showProfileActivity_to_editProfileActivity)
        }

        //if( emailParameter == null ) println("EMAIL PARAMETER NULL")

        //Use this to distinguish owner
        val isOwner = emailParameter == null

        //Gets overwritten if the visitor is not the owner
        var getUserFromMail = MainActivity.mAuth.currentUser!!.email!!

        if(!isOwner){

            //println( "EMAIL FROM LAYOUT" + emailParameter)
            //println( "EMAIL " + MainActivity.mAuth.currentUser!!.email!!)

            fullNameLayout.visibility = View.GONE
            emailLayout.visibility = View.GONE
            logoutButton.visibility = View.GONE
            setHasOptionsMenu(false)

            getUserFromMail = emailParameter!!
        }




        ratingBarProfile.visibility = View.GONE
        rateButton.visibility = View.GONE

        if(rating!!){

            ratingBarProfile.visibility = View.VISIBLE
            rateButton.visibility = View.VISIBLE

        }


        FirebaseFirestore.getInstance().collection("users")
            .document(emailParameter!!).collection( "Ratings" )
            .addSnapshotListener { value, error ->
                if(error != null) throw error
                if(value != null )
                {




                    if (value?.documents.filter { it.get("userMail") == MainActivity.mAuth.currentUser!!.email!! }
                            .isNotEmpty()) {


                        ratingBarProfile.visibility = View.GONE
                        rateButton.visibility = View.GONE
                    }


                }




            }



        rateButton.setOnClickListener {

            //Rate
            //Send Rating
            val db = FirebaseFirestore.getInstance()



            var dataToUser = hashMapOf( "userMail" to MainActivity.mAuth.currentUser!!.email!! ,
                "Score" to ratingBarProfile.numStars )



            db.collection("users").document(emailParameter!!)
                .collection("Ratings").add( dataToUser )


        }


        db =  FirebaseFirestore.getInstance()
        db.collection("users")
            .document(getUserFromMail)
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

        Firebase.storage.reference
            .child(getUserFromMail).downloadUrl
            .addOnSuccessListener { uri ->
                imgProfile.load(uri.toString()) {
                    memoryCachePolicy(CachePolicy.DISABLED) //to force reloading when image changes
                }
            }.addOnFailureListener {
                imgProfile.setImageResource(R.drawable.ic_baseline_no_photography)
            }


        logoutButton.setOnClickListener {

            FirebaseAuth.getInstance().signOut()

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient( requireContext() , gso)

            googleSignInClient.signOut()

            val intent = Intent(context, LoginActivity::class.java)
            startActivity(intent)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
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

    private fun editProfile() {
        setFragmentResult(
            "requestKeyShowToEdit", bundleOf(
                "group06.lab.fullName" to tvFullName.text.toString(),
                "group06.lab.nickName" to tvNickName.text.toString(),
                //"group06.lab.email" to tvEmail.text.toString(),
                "group06.lab.location" to tvLocation.text.toString()
            )
        )
        if (snackbar.isShown)
            snackbar.dismiss()
        findNavController().navigate(R.id.action_showProfileActivity_to_editProfileActivity)
    }
}