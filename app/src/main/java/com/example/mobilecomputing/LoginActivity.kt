package com.example.mobilecomputing

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.content.Context
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.mobilecomputing.databinding.ActivityLoginBinding


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Creating credential for testing purposes
        if (!(applicationContext.getSharedPreferences(getString(R.string.sharedPreferences), Context.MODE_PRIVATE).contains("AccountChanged"))) {
            applicationContext.getSharedPreferences(
                getString(R.string.sharedPreferences),
                Context.MODE_PRIVATE
            ).edit().putString("username", "admin").apply()

            applicationContext.getSharedPreferences(
                getString(R.string.sharedPreferences),
                Context.MODE_PRIVATE
            ).edit().putString("password", "admin").apply()
        }

        binding.LoginButton.setOnClickListener {
            Log.d("Test", "Login Button Triggered")
            // Authentication check
            authentication()
        }
        binding.CreateButton.setOnClickListener {
            Log.d("Test", "Create Account Button Triggered")
            // CreateAccountActivity
            notLogged()
            startActivity(Intent(applicationContext, CreateAccountActivity::class.java))
        }
        binding.ChangeButton.setOnClickListener {
            Log.d("Test", "Change Login Button Triggered")
            // Change to Fingerprint Login
            notLogged()
            startActivity(Intent(applicationContext, FingerLoginActivity::class.java))
        }

        checkLoginStatus()
    }


    override fun onResume() {
        super.onResume()
        checkLoginStatus()
    }

    private fun authentication() {
        val inputUsername = findViewById<EditText>(R.id.loginUsername).text
        val inputPassword = findViewById<EditText>(R.id.loginPassword).text
        val sharedPref = applicationContext.getSharedPreferences(
            getString(R.string.sharedPreferences), Context.MODE_PRIVATE)
        val savedUsername = sharedPref.getString("username", null)
        val savedPassword = sharedPref.getString("password", null)

        if(inputUsername.toString() == savedUsername) {
            if (inputPassword.toString() == savedPassword) {
                applicationContext.getSharedPreferences(
                    getString(R.string.sharedPreferences),
                    Context.MODE_PRIVATE
                ).edit().putInt("LoginStatus", 1).apply()
                startActivity(Intent(applicationContext, MainActivity::class.java).putExtra("loggedUser", inputUsername.toString()))
            } else {
                Toast.makeText(this, "Wrong username or password.", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Wrong username or password.", Toast.LENGTH_LONG).show()
        }

    }

    private fun checkLoginStatus() {
        val loginStatus = applicationContext.getSharedPreferences(
            getString(R.string.sharedPreferences),
            Context.MODE_PRIVATE).getInt("LoginStatus", 0)

        if(loginStatus == 1){
            startActivity(Intent(applicationContext, MainActivity::class.java))
        }
    }

    private fun notLogged() {
        applicationContext.getSharedPreferences(
            getString(R.string.sharedPreferences),
            Context.MODE_PRIVATE
        ).edit().putInt("LoginStatus", 0).apply()
    }
}
