package com.example.mobilecomputing

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.room.Room
import com.example.mobilecomputing.db.AppDatabase
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import java.util.*

class GeofenceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("TEST", "GEOFENCE TRIGGERED")
        if (context != null) {
            // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
            val geofencingEvent = GeofencingEvent.fromIntent(intent)
            val geofencingTransition = geofencingEvent.geofenceTransition

            if (geofencingTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofencingTransition == Geofence.GEOFENCE_TRANSITION_DWELL
            ) {
                if (intent != null) {
                    // Retrieve data from intent
                    val uid = intent.getIntExtra("uid", 0)
                    var text = intent.getStringExtra("message")
                    if (text == null) {
                        text = "DEFAULT STRING"
                    }

                    Log.d("TEST", "GEOFENCE TRIGGERED")

                    AsyncTask.execute{
                        val db = Room
                            .databaseBuilder(
                                context,
                                AppDatabase::class.java,
                                "com.example.mobilecomputing.db"
                            )
                            .build()
                        val reminderTIME = db.reminderDAO().getReminderInfo(uid).reminder_time

                        if (reminderTIME != "T") {

                            val reminderCalendar = GregorianCalendar.getInstance()
                            val setDate = reminderTIME.split("T")
                                .toTypedArray()[0].split("-").toTypedArray()
                            val setTime = reminderTIME.split("T")
                                .toTypedArray()[1].split(":").toTypedArray()

                            reminderCalendar.set(java.util.Calendar.YEAR, setDate[0].toInt())
                            reminderCalendar.set(java.util.Calendar.MONTH, setDate[1].toInt() - 1)
                            reminderCalendar.set(
                                java.util.Calendar.DAY_OF_MONTH,
                                setDate[2].toInt()
                            )
                            reminderCalendar.set(java.util.Calendar.HOUR_OF_DAY, setTime[0].toInt())
                            reminderCalendar.set(java.util.Calendar.MINUTE, setTime[1].toInt())
                            reminderCalendar.set(java.util.Calendar.SECOND, 0)

                            if (reminderCalendar.timeInMillis >
                                java.util.Calendar.getInstance().timeInMillis - 60000 &&
                                reminderCalendar.timeInMillis <
                                java.util.Calendar.getInstance().timeInMillis + 60000
                            ) {
                                db.reminderDAO().updateSeen(false, uid)
                                val message =
                                    "$text at ${reminderTIME}"
                                MainActivity.setReminderWithWorkManager(
                                    context,
                                    uid,
                                    reminderCalendar.timeInMillis,
                                    message
                                )

                                val triggeringGeofences =
                                    geofencingEvent.triggeringGeofences
                                MainActivity.removeGeofences(context, triggeringGeofences)
                            }
                        } else {
                            MainActivity.setReminderWithWorkManager(
                                context,
                                uid,
                                Calendar.getInstance().timeInMillis + 1000, //alert in 1 second
                                text
                            )

                            val triggeringGeofences =
                                geofencingEvent.triggeringGeofences
                            MainActivity.removeGeofences(context, triggeringGeofences)
                        }
                    }
                }
            }
        }
    }
}
