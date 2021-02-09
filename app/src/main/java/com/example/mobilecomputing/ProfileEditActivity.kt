package com.example.mobilecomputing
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.content.Context
import android.widget.Button
import android.content.Intent
import android.widget.Toast
import com.example.mobilecomputing.databinding.ActivityProfileEditBinding

class ProfileEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileEditBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.backToProfile.setOnClickListener {
            startActivity(Intent(applicationContext, ProfileActivity::class.java))
        }

        binding.changeUsername.setOnClickListener {
            updateUsername()
            Toast.makeText(this, "Username changed", Toast.LENGTH_LONG).show()
        }

        binding.changePassword.setOnClickListener {
            updatePassword()
            Toast.makeText(this, "Password changed", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateUsername() {
        applicationContext.getSharedPreferences(
            getString(R.string.sharedPreferences),
            Context.MODE_PRIVATE
        ).edit().putInt("AccountChanged", 1).apply()
        val inputUsername = findViewById<EditText>(R.id.editProfileUsername).text
        applicationContext.getSharedPreferences(
            getString(R.string.sharedPreferences),
            Context.MODE_PRIVATE
        ).edit().putString("username", inputUsername.toString()).apply()
    }

    private fun updatePassword() {
        applicationContext.getSharedPreferences(
            getString(R.string.sharedPreferences),
            Context.MODE_PRIVATE
        ).edit().putInt("AccountChanged", 1).apply()
        val inputPassword = findViewById<EditText>(R.id.editProfilePassword).text
        applicationContext.getSharedPreferences(
            getString(R.string.sharedPreferences),
            Context.MODE_PRIVATE
        ).edit().putString("password", inputPassword.toString()).apply()
    }
}