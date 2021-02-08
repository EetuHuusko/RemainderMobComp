package com.example.mobilecomputing

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.content.Context
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.example.mobilecomputing.databinding.ActivityFingerLoginBinding
import com.example.mobilecomputing.databinding.ActivityLoginBinding

class FingerLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFingerLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFingerLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }
}