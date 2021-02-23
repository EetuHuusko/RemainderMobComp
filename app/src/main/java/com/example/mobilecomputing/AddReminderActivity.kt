package com.example.mobilecomputing

import android.content.Intent
import android.content.Context
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.DatePicker
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.room.Room
import java.time.LocalDateTime
import java.util.*
import com.example.mobilecomputing.databinding.ActivityAddReminderBinding
import com.example.mobilecomputing.databinding.ActivityMainBinding
import com.example.mobilecomputing.db.AppDatabase
import com.example.mobilecomputing.db.ReminderInfo

class AddReminderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddReminderBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddReminderBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val bundle :Bundle ?=intent.extras

        if(bundle!=null)
        {
            val selectedID = bundle.getInt("selectedID")

            AsyncTask.execute {
                val db = Room
                    .databaseBuilder(
                        applicationContext,
                        AppDatabase::class.java,
                        "com.example.mobilecomputing.db"
                    )
                    .build()
                val reminderInfo = db.reminderDAO().getReminderInfo(selectedID)
                db.close()

                val dateTime = reminderInfo.reminder_time.split("T").toTypedArray()
                findViewById<EditText>(R.id.addReminderMessage).setText(reminderInfo.message, TextView.BufferType.EDITABLE)
                findViewById<EditText>(R.id.addReminderTime).setText(dateTime[1])
                findViewById<EditText>(R.id.addReminderDate).setText(dateTime[0])
                findViewById<EditText>(R.id.addReminderLocation).setText(reminderInfo.location_x, TextView.BufferType.EDITABLE)
            }
        }

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        findViewById<EditText>(R.id.addReminderDate).setOnClickListener {
            val datePicker = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, month, day ->
                findViewById<EditText>(R.id.addReminderDate).setText("" + addZero(year) + "-" + addZero(month + 1) + "-" + addZero(day))
            }, day, month, year)
            datePicker.updateDate(2021, 1 - 1, 1);
            datePicker.show()
        }

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        findViewById<EditText>(R.id.addReminderTime).setOnClickListener {
            val timePicker = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                findViewById<EditText>(R.id.addReminderTime).setText(""+ addZero(hour) + ":" + addZero(minute))
            }, hour, minute, true)
            timePicker.show()
        }

        binding.addReminder.setOnClickListener {
            val loggedUserID = applicationContext.getSharedPreferences(getString(R.string.sharedPreferences),
                    Context.MODE_PRIVATE).getString("username",null).toString()

            val rem_message = findViewById<EditText>(R.id.addReminderMessage).text
            val rem_datetime = findViewById<EditText>(R.id.addReminderDate).text.toString() + "T" + findViewById<EditText>(R.id.addReminderTime).text.toString()
            val rem_location = findViewById<EditText>(R.id.addReminderLocation).text

            val reminder = ReminderInfo(null,
                message = rem_message.toString(),
                location_x = rem_location.toString(),
                location_y = "",
                reminder_time = rem_datetime,
                creation_time = LocalDateTime.now().toString(),
                creation_id = loggedUserID,
                reminder_seen = false
            )

            if(bundle!=null) {
                val editReminder = ReminderInfo(
                    uid = bundle?.getInt("selectedID"),
                    message = rem_message.toString(),
                    location_x = rem_location.toString(),
                    location_y = "",
                    reminder_time = rem_datetime,
                    creation_time = LocalDateTime.now().toString(),
                    creation_id = loggedUserID,
                    reminder_seen = false
                )
                bundle?.getInt("selected_reminder")
                AsyncTask.execute {
                    val db = Room.databaseBuilder(
                        applicationContext,
                        AppDatabase::class.java,
                        "com.example.mobilecomputing.db"
                    ).build()
                    db.reminderDAO().updateReminderInfo(editReminder)
                    db.close()
                }
                Toast.makeText(this, "Reminder modified!", Toast.LENGTH_LONG).show()
                finish()

            } else {
                val newReminder = ReminderInfo(
                    null,
                    message = rem_message.toString(),
                    location_x = rem_location.toString(),
                    location_y = "",
                    reminder_time = rem_datetime,
                    creation_time = LocalDateTime.now().toString(),
                    creation_id = loggedUserID,
                    reminder_seen = false
                )
                AsyncTask.execute {
                    val db = Room.databaseBuilder(
                        applicationContext,
                        AppDatabase::class.java,
                        "com.example.mobilecomputing.db"
                    ).build()
                    val uuid1 = db.reminderDAO().insert(newReminder).toInt()
                    db.close()
                }
                Toast.makeText(this, "Reminder added!", Toast.LENGTH_LONG).show()
                finish()

            }
        }

        binding.addBackToMain.setOnClickListener {
            finish()
        }

    }

    private fun addZero(number: Int): String {
        return when (number <= 9) {
            true -> "0${number}"
            false -> number.toString()
        }
    }
}