package com.example.mobilecomputing

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.example.mobilecomputing.databinding.ActivityAddReminderBinding
import com.example.mobilecomputing.db.AppDatabase
import com.example.mobilecomputing.db.ReminderInfo
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import java.time.LocalDateTime
import java.util.*

const val LOCATION_REQUEST = 1223
const val GEOFENCE_LOCATION_REQUEST_CODE = 12345
const val GEOFENCE_RADIUS = 500
const val GEOFENCE_ID = "REMINDER_GEOFENCE_ID"
const val GEOFENCE_EXPIRATION = 10*24*60*60*1000 // 10 days
const val GEOFENCE_DWELL_DELAY = 10*1000 // 1 sec

class AddReminderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddReminderBinding
    private lateinit var geofencingClient: GeofencingClient

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddReminderBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val bundle :Bundle ?=intent.extras

        geofencingClient = LocationServices.getGeofencingClient(this)

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
                binding.addReminderMessage.setText(reminderInfo.message,
                    TextView.BufferType.EDITABLE)
                binding.addReminderTime.setText(dateTime[1])
                binding.addReminderDate.setText(dateTime[0])
                binding.addReminderLocation.setText(reminderInfo.location_x.toString()
                        + "," + reminderInfo.location_y.toString(),
                    TextView.BufferType.EDITABLE)
            }
        }

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        binding.addReminderDate.setOnClickListener {
            val datePicker = DatePickerDialog(this, DatePickerDialog.OnDateSetListener
            { _, year, month, day ->
                binding.addReminderDate.setText("" +
                        addZero(year) + "-" + addZero(month + 1) + "-" + addZero(day))
            }, day, month, year)
            datePicker.updateDate(2021, 1 - 1, 1);
            datePicker.show()
        }

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        binding.addReminderTime.setOnClickListener {
            val timePicker = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener
            { _, hour, minute ->
                binding.addReminderTime.setText("" +
                        addZero(hour) + ":" + addZero(minute))
            }, hour, minute, true)
            timePicker.show()
        }

        binding.addReminderLocation.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            if (bundle!=null) {
                val locationIntentSplit = binding.addReminderLocation.text.split(",")
                    .toTypedArray()
                intent.putExtra("latitude", locationIntentSplit[0].toDouble())
                intent.putExtra("longitude", locationIntentSplit[1].toDouble())
                intent.putExtra("selectedID", bundle.getInt("selectedID"))
            }
            startActivityForResult(intent, LOCATION_REQUEST)
        }

        binding.addReminder.setOnClickListener {
            val loggedUserID = applicationContext.
                    getSharedPreferences(getString(R.string.sharedPreferences),
                    Context.MODE_PRIVATE).getString("username",null).toString()

            val remMessage = binding.addReminderMessage.text

            val locationSplit = binding.addReminderLocation.text.split(",")
                .toMutableList()

            if(bundle!=null) {

                if(locationSplit[0] == "Select location"){
                    locationSplit[0] = "0.0"
                    locationSplit.add(1,"0.0")
                }

                val remDatetime = binding.addReminderDate.text.toString() + "T" +
                        binding.addReminderTime.text.toString()

                val editReminder = ReminderInfo(
                    uid = bundle.getInt("selectedID"),
                    message = remMessage.toString(),
                    location_x = locationSplit[0].toDouble(),
                    location_y = locationSplit[1].toDouble(),
                    reminder_time = remDatetime,
                    creation_time = LocalDateTime.now().toString(),
                    creation_id = loggedUserID,
                    reminder_seen = false
                )
                bundle.getInt("selected_reminder")
                AsyncTask.execute {
                    val db = Room.databaseBuilder(
                        applicationContext,
                        AppDatabase::class.java,
                        "com.example.mobilecomputing.db"
                    ).build()
                    db.reminderDAO().updateReminderInfo(editReminder)
                    val uuid = bundle.getInt("selectedID")

                    if (remDatetime != "T" && locationSplit[0] != "0.0") {

                        val reminderCalendar = GregorianCalendar.getInstance()
                        val setDate = editReminder.reminder_time.split("T")
                            .toTypedArray()[0].split("-").toTypedArray()
                        val setTime = editReminder.reminder_time.split("T")
                            .toTypedArray()[1].split(":").toTypedArray()

                        reminderCalendar.set(Calendar.YEAR,setDate[0].toInt())
                        reminderCalendar.set(Calendar.MONTH,setDate[1].toInt()-1)
                        reminderCalendar.set(Calendar.DAY_OF_MONTH,setDate[2].toInt())
                        reminderCalendar.set(Calendar.HOUR_OF_DAY, setTime[0].toInt())
                        reminderCalendar.set(Calendar.MINUTE, setTime[1].toInt())
                        reminderCalendar.set(Calendar.SECOND, 0)

                        if (reminderCalendar.timeInMillis > Calendar.getInstance().timeInMillis) {
                            db.reminderDAO().updateSeen(false, uuid)
                            val message =
                                "$editReminder.message"
                            MainActivity.setReminderWithWorkManager(
                                applicationContext,
                                uuid,
                                reminderCalendar.timeInMillis,
                                message
                            )
                            val latlng =
                                LatLng(locationSplit[0].toDouble(), locationSplit[1].toDouble())
                            createGeoFence(latlng, uuid, editReminder.message, geofencingClient)
                        }
                    }
                    if (remDatetime == "T" && locationSplit[0] != "0.0") {

                        db.reminderDAO().updateSeen(false, uuid)
                        val latlng =
                            LatLng(locationSplit[0].toDouble(), locationSplit[1].toDouble())
                        createGeoFence(latlng, uuid, editReminder.message, geofencingClient)

                    }
                    if (remDatetime != "T" && locationSplit[0] == "0.0") {

                        val reminderCalendar = GregorianCalendar.getInstance()
                        val setDate = editReminder.reminder_time.split("T")
                            .toTypedArray()[0].split("-").toTypedArray()
                        val setTime = editReminder.reminder_time.split("T")
                            .toTypedArray()[1].split(":").toTypedArray()

                        reminderCalendar.set(Calendar.YEAR,setDate[0].toInt())
                        reminderCalendar.set(Calendar.MONTH,setDate[1].toInt()-1)
                        reminderCalendar.set(Calendar.DAY_OF_MONTH,setDate[2].toInt())
                        reminderCalendar.set(Calendar.HOUR_OF_DAY, setTime[0].toInt())
                        reminderCalendar.set(Calendar.MINUTE, setTime[1].toInt())
                        reminderCalendar.set(Calendar.SECOND, 0)

                        if (reminderCalendar.timeInMillis > Calendar.getInstance().timeInMillis) {
                            db.reminderDAO().updateSeen(false, uuid)
                            val message =
                                "${editReminder.message} at ${editReminder.reminder_time}"
                            MainActivity.setReminderWithWorkManager(
                                applicationContext,
                                uuid,
                                reminderCalendar.timeInMillis,
                                message
                            )
                        }
                    }
                }

                Toast.makeText(this, "Reminder modified!", Toast.LENGTH_LONG).show()
                finish()

            } else {

                if(locationSplit[0] == "Select location"){
                    locationSplit[0] = "0.0"
                    locationSplit.add(1,"0.0")
                }

                val remDatetime = binding.addReminderDate.text.toString() + "T" +
                        binding.addReminderTime.text.toString()

                val newReminder = ReminderInfo(
                    null,
                    message = remMessage.toString(),
                    location_x = locationSplit[0].toDouble(),
                    location_y = locationSplit[1].toDouble(),
                    reminder_time = remDatetime,
                    creation_time = LocalDateTime.now().toString(),
                    creation_id = loggedUserID,
                    reminder_seen = false
                )
                Log.d("DEBUG", "Reminder time is: ${newReminder.reminder_time}")

                AsyncTask.execute {
                    val db = Room.databaseBuilder(
                        applicationContext,
                        AppDatabase::class.java,
                        "com.example.mobilecomputing.db"
                    ).build()
                    val uuid = db.reminderDAO().insert(newReminder).toInt()

                    if (remDatetime != "T" && locationSplit[0] != "0.0") {

                        val reminderCalendar = GregorianCalendar.getInstance()
                        val setDate = newReminder.reminder_time.split("T")
                            .toTypedArray()[0].split("-").toTypedArray()
                        val setTime = newReminder.reminder_time.split("T")
                            .toTypedArray()[1].split(":").toTypedArray()

                        reminderCalendar.set(Calendar.YEAR,setDate[0].toInt())
                        reminderCalendar.set(Calendar.MONTH,setDate[1].toInt()-1)
                        reminderCalendar.set(Calendar.DAY_OF_MONTH,setDate[2].toInt())
                        reminderCalendar.set(Calendar.HOUR_OF_DAY, setTime[0].toInt())
                        reminderCalendar.set(Calendar.MINUTE, setTime[1].toInt())
                        reminderCalendar.set(Calendar.SECOND, 0)

                        if (reminderCalendar.timeInMillis > Calendar.getInstance().timeInMillis) {
                            db.reminderDAO().updateSeen(false, uuid)
                            val latlng = LatLng(locationSplit[0].toDouble(),
                                    locationSplit[1].toDouble())
                            createGeoFence(latlng, uuid, newReminder.message, geofencingClient)
                        }
                    }
                    if (remDatetime == "T" && locationSplit[0] != "0.0") {

                        db.reminderDAO().updateSeen(false, uuid)
                        val latlng = LatLng(locationSplit[0].toDouble(),
                            locationSplit[1].toDouble())
                        createGeoFence(latlng, uuid, newReminder.message, geofencingClient)

                    }
                    if (remDatetime != "T" && locationSplit[0] == "0.0") {

                        val reminderCalendar = GregorianCalendar.getInstance()
                        val setDate = newReminder.reminder_time.split("T")
                            .toTypedArray()[0].split("-").toTypedArray()
                        val setTime = newReminder.reminder_time.split("T")
                            .toTypedArray()[1].split(":").toTypedArray()

                        reminderCalendar.set(Calendar.YEAR,setDate[0].toInt())
                        reminderCalendar.set(Calendar.MONTH,setDate[1].toInt()-1)
                        reminderCalendar.set(Calendar.DAY_OF_MONTH,setDate[2].toInt())
                        reminderCalendar.set(Calendar.HOUR_OF_DAY, setTime[0].toInt())
                        reminderCalendar.set(Calendar.MINUTE, setTime[1].toInt())
                        reminderCalendar.set(Calendar.SECOND, 0)

                        if (reminderCalendar.timeInMillis > Calendar.getInstance().timeInMillis) {
                            db.reminderDAO().updateSeen(false, uuid)
                            val message =
                                "${newReminder.message} at ${newReminder.reminder_time}"
                            MainActivity.setReminderWithWorkManager(
                                applicationContext,
                                uuid,
                                reminderCalendar.timeInMillis,
                                message
                            )
                        }
                    }
                }
                Toast.makeText(this, "Reminder added!", Toast.LENGTH_LONG).show()
                finish()
            }
        }
        binding.addBackToMain.setOnClickListener {
            finish()
        }

    }

    private fun createGeoFence(location: LatLng, uid: Int, message: String, geofencingClient: GeofencingClient) {
        val geofence = Geofence.Builder()
            .setRequestId(GEOFENCE_ID)
            .setCircularRegion(location.latitude, location.longitude, GEOFENCE_RADIUS.toFloat())
            .setExpirationDuration(GEOFENCE_EXPIRATION.toLong())
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL)
            .setLoiteringDelay(GEOFENCE_DWELL_DELAY)
            .build()

        val geofenceRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        val intent = Intent(this, GeofenceReceiver::class.java)
            .putExtra("uid", uid)
            .putExtra("message", message)

        val pendingIntent = PendingIntent.getBroadcast(
                applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ),
                    GEOFENCE_LOCATION_REQUEST_CODE
                )
            } else {
                geofencingClient.addGeofences(geofenceRequest, pendingIntent).run{
                    addOnSuccessListener {
                        Log.d("TEST", "GEOFENCE ADDED SUCCESSFULLY")
                    }
                    addOnFailureListener {
                        Log.d("TEST", "GEOFENCE FAILED")
                    }
                }
            }
        } else {
            geofencingClient.addGeofences(geofenceRequest, pendingIntent).run{
                addOnSuccessListener {
                    Log.d("TEST", "GEOFENCE ADDED SUCCESSFULLY")
                }
                addOnFailureListener {
                    Log.d("TEST", "GEOFENCE FAILED")
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOCATION_REQUEST) {
            if (resultCode == RESULT_OK) {
                val locationLatitude = data!!.getDoubleExtra("latitude", 0.0)
                val locationLongitude = data!!.getDoubleExtra("longitude", 0.0)

                binding.addReminderLocation.text = "$locationLatitude,$locationLongitude"
            }
        }
    }

    private fun addZero(number: Int): String {
        return when (number <= 9) {
            true -> "0${number}"
            false -> number.toString()
        }
    }

}
