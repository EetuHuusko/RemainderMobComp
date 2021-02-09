package com.example.mobilecomputing

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.content.Context
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.example.mobilecomputing.databinding.ActivityCreateAccountBinding
import com.example.mobilecomputing.databinding.ActivityLoginBinding


class CreateAccountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateAccountBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAccountBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.createAccountButton.setOnClickListener {
            val inputUsername = findViewById<EditText>(R.id.createUsername).text
            val inputPassword = findViewById<EditText>(R.id.createPassword).text
            applicationContext.getSharedPreferences(
                getString(R.string.sharedPreferences),
                Context.MODE_PRIVATE
            ).edit().putString("username", inputUsername.toString()).apply()
            applicationContext.getSharedPreferences(
                getString(R.string.sharedPreferences),
                Context.MODE_PRIVATE
            ).edit().putString("password", inputPassword.toString()).apply()
        }

        binding.createBackToLogin.setOnClickListener {
            startActivity(Intent(applicationContext, LoginActivity::class.java))
        }

    }
}