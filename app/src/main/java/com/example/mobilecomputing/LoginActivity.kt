package com.example.mobilecomputing

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.content.Context
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.example.mobilecomputing.databinding.ActivityLoginBinding


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Creating credential for testing purposes

        applicationContext.getSharedPreferences(
            getString(R.string.sharedPreferences),
            Context.MODE_PRIVATE
        ).edit().putString("username", "admin").apply()

        applicationContext.getSharedPreferences(
            getString(R.string.sharedPreferences),
            Context.MODE_PRIVATE
        ).edit().putString("password", "admin").apply()

        binding.LoginButton.setOnClickListener {
            Log.d("Login", "Login Button Triggered")
            // Authentication check
            authentication()
        }

        binding.ChangeButton.setOnClickListener {
            Log.d("ChangeLogin", "Change Login Button Triggered")
            startActivity(Intent(applicationContext, FingerLoginActivity::class.java))
        }

        checkLoginStatus()
    }


    override fun onResume() {
        super.onResume()
        checkLoginStatus()
    }

    private fun authentication() {
        val input_username = findViewById<EditText>(R.id.loginUsername).text
        val input_password = findViewById<EditText>(R.id.loginPassword).text
        val sharedPref = applicationContext.getSharedPreferences(
            getString(R.string.sharedPreferences), Context.MODE_PRIVATE)
        val saved_username = sharedPref.getString("username", null)
        val saved_password = sharedPref.getString("password", null)
        if(input_username.toString().equals(saved_username)) {
            if(input_password.toString().equals(saved_password)) {
                applicationContext.getSharedPreferences(
                    getString(R.string.sharedPreferences),
                    Context.MODE_PRIVATE
                ).edit().putInt("LoginStatus", 1).apply()
                startActivity(Intent(applicationContext, MainActivity::class.java))
            }
        }

    }

    private fun checkLoginStatus() {
        val login_status = applicationContext.getSharedPreferences(getString(R.string.sharedPreferences), Context.MODE_PRIVATE).getInt("LoginStatus", 0)

        if(login_status == 1){
            startActivity(Intent(applicationContext, MainActivity::class.java))
        }
    }
}
