package com.group06.lab1

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class ShowProfileActivity : AppCompatActivity() {
    private lateinit var tvFullName : TextView
    private lateinit var tvNickName : TextView
    private lateinit var tvEmail : TextView
    private lateinit var tvLocation : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        //TODO: Retrieve data from filesystem

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_profile)

        tvFullName = findViewById<TextView>(R.id.tvFullName)
        tvNickName = findViewById<TextView>(R.id.tvNickName)
        tvEmail = findViewById<TextView>(R.id.tvEmail)
        tvLocation =  findViewById<TextView>(R.id.tvLocation)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.activity_show_profile_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.editMenu -> {
//                Toast.makeText(this, "Simple action", Toast.LENGTH_LONG).show()
                Intent(this, EditProfileActivity::class.java).also {
                    it.putExtra("fullName", tvFullName.text.toString())
                    it.putExtra("nickName", tvNickName.text.toString())
                    it.putExtra("email", tvEmail.text.toString())
                    it.putExtra("location", tvLocation.text.toString())
                    startActivityForResult(it, 1)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1 && resultCode == Activity.RESULT_OK) {
            tvFullName.text = data?.getStringExtra("fullName")
            tvNickName.text = data?.getStringExtra("nickName")
            tvEmail.text = data?.getStringExtra("email")
            tvLocation.text = data?.getStringExtra("location")
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        //TODO: Save data into filesystem
    }

}
