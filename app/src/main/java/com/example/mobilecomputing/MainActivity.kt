package com.example.mobilecomputing

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.room.Room
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.mobilecomputing.databinding.ActivityMainBinding
import com.example.mobilecomputing.db.AppDatabase
import com.example.mobilecomputing.db.ReminderInfo
import java.util.concurrent.TimeUnit
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        val bundle :Bundle ?=intent.extras
        setContentView(view)
        listView = binding.listView

        //populateTestDb()

        refreshListView()

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, id ->

            val selectedReminderInfo = listView.adapter.getItem(position) as ReminderInfo
            val message =
                "Do you want to modify reminder ${selectedReminderInfo.message}?"

            // Show AlertDialog to modify the reminder
            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setTitle("Modify reminder?")
                .setMessage(message)
                .setNegativeButton("Delete") { _, _ ->
                    //delete from database
                    AsyncTask.execute {
                        val db = Room
                            .databaseBuilder(
                                applicationContext,
                                AppDatabase::class.java,
                                "com.example.mobilecomputing.db"
                            )
                            .build()
                        db.reminderDAO().delete(selectedReminderInfo.uid!!)
                    }
                    refreshListView()
                    Toast.makeText(this, "Reminder deleted.", Toast.LENGTH_LONG).show()
                }
                .setPositiveButton("Edit") {_, _ ->
                    startActivity(Intent(applicationContext, AddReminderActivity::class.java).putExtra("selectedID", selectedReminderInfo.uid!!))
                }
                .setNeutralButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()

        }

        binding.mainAddReminer.setOnClickListener {
            startActivity(Intent(applicationContext, AddReminderActivity::class.java))
        }

        binding.mainGoProfile.setOnClickListener {
            startActivity(Intent(applicationContext, ProfileActivity::class.java))
        }

        binding.mainLogOut.setOnClickListener {
            //Change LoginStatus to 0 and start LoginActivity
            applicationContext.getSharedPreferences(
                    getString(R.string.sharedPreferences),
                    Context.MODE_PRIVATE
            ).edit().putInt("LoginStatus", 0).apply()
            startActivity(Intent(applicationContext, LoginActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK))
        }

    }

    override fun onResume() {
        super.onResume()
        refreshListView()
    }

    fun getUserID(): String {
        val prefs = applicationContext.getSharedPreferences(
            getString(R.string.sharedPreferences), Context.MODE_PRIVATE)
        return prefs.getString("username", null).toString()
    }

    private fun refreshListView() {
        var refreshTask = LoadReminderInfoEntries()
        refreshTask.execute()
    }

    inner class LoadReminderInfoEntries : AsyncTask<String?, String?, List<ReminderInfo>>() {
        override fun doInBackground(vararg params: String?): List<ReminderInfo>? {
            val db = Room
                .databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java,
                    "com.example.mobilecomputing.db"
                )
                .build()
            val reminderInfo = db.reminderDAO().getSeenReminderInfos(getUserID())
            db.close()
            return reminderInfo
        }

        override fun onPostExecute(reminderInfos: List<ReminderInfo>?) {
            super.onPostExecute(reminderInfos)
            if (reminderInfos != null) {
                if (reminderInfos.isNotEmpty()) {
                    val adaptor = ReminderAdaptor(applicationContext, reminderInfos)
                    listView.adapter = adaptor
                } else {
                    listView.adapter = null
                }
            }
        }
    }

    companion object {

        fun showNotification(context: Context, message: String, reminderId: Int) {

            val notificationChannel = "REMIND_NOTIFICATION_CHANNEL"
            val notificationId = Random.nextInt(10, 1000) + 5

            val notificationIntent = Intent(context, LoginActivity::class.java)
            notificationIntent.putExtra("notified_reminder_id", reminderId)
            val notificationPendingIntent: PendingIntent = TaskStackBuilder.create(context).run {
                addNextIntentWithParentStack(notificationIntent)
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            }

            val audioAttributes: AudioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val notificationBuilder =
                NotificationCompat.Builder(context, notificationChannel)
                    .setSmallIcon(R.drawable.ic_notification_important_black_48dp)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(message)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setGroup(notificationChannel)
                    .setContentIntent(notificationPendingIntent)
                    .setAutoCancel(true) //remove notification when tapped

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channel = NotificationChannel(
                notificationChannel,
                context.getString(R.string.app_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.app_name)
            }

            //Set custom notification sound
            channel.setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + "com.example.mobilecomputing" + "/" + R.raw.notification), audioAttributes)

            notificationManager.createNotificationChannel(channel)

            notificationManager.notify(notificationId, notificationBuilder.build())

        }

        fun setReminderWithWorkManager(
            context: Context,
            uid: Int,
            timeInMillis: Long,
            message: String
        ) {
            val reminderParameters = Data.Builder()
                .putString("message", message)
                .putInt("uid", uid)
                .build()

            var minutesFromNow = 0L
            if (timeInMillis > System.currentTimeMillis())
                minutesFromNow = timeInMillis - System.currentTimeMillis()

            val reminderRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInputData(reminderParameters)
                .setInitialDelay(minutesFromNow, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(uid.toString(), ExistingWorkPolicy.REPLACE, reminderRequest)
        }
    }
}