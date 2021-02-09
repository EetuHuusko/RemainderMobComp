package com.example.mobilecomputing

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.content.Context
import android.content.Intent
import com.example.mobilecomputing.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val current_user = applicationContext.getSharedPreferences(
            getString(R.string.sharedPreferences),
            Context.MODE_PRIVATE
        ).getString("username", null)

        val profile_user: TextView = findViewById<TextView>(R.id.profile_username)
        profile_user.text = "$current_user"

        binding.editProfile.setOnClickListener {
            startActivity(Intent(applicationContext, ProfileEditActivity::class.java))
        }

        binding.backToMain.setOnClickListener {
            startActivity(Intent(applicationContext, MainActivity::class.java))
        }
    }
}