package com.example.mobilecomputing

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.os.Build
import android.widget.BaseAdapter
import android.view.LayoutInflater
import android.widget.TextView
import android.view.View
import android.view.ViewGroup
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ListView
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import android.widget.*
import androidx.room.Room
import androidx.core.app.NotificationCompat
import com.example.mobilecomputing.db.AppDatabase
import com.example.mobilecomputing.db.ReminderInfo
import com.example.mobilecomputing.ReminderAdaptor
import com.example.mobilecomputing.databinding.ActivityMainBinding
import com.example.mobilecomputing.databinding.ActivityLoginBinding
import com.example.mobilecomputing.databinding.ActivityProfileBinding
import com.example.mobilecomputing.databinding.ActivityAddReminderBinding
import kotlinx.android.synthetic.main.activity_main.view.*


class MainActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
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

    private fun refreshListView() {
        var refreshTask = LoadReminderInfoEntries()
        refreshTask.execute()
    }

    inner class LoadReminderInfoEntries : AsyncTask<String?, String?, List<ReminderInfo>>() {
        override fun doInBackground(vararg params: String?): List<ReminderInfo> {
            val db = Room
                .databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java,
                    "com.example.mobilecomputing.db"
                )
                .build()
            val reminderInfo = db.reminderDAO().getReminderInfos()
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

    private fun populateTestDb(){
        val loggedUserID = applicationContext.getSharedPreferences(getString(R.string.sharedPreferences),
                Context.MODE_PRIVATE).getString("username",null).toString()
        val testReminder1 = ReminderInfo(null, message = "Pick up Johnny from school",
                location_x = "65.06002857341045",
                location_y = "25.46766200254371",
                reminder_time = "2021-02-26 Friday 14:00 UTC+2",
                creation_time = "2021-02-21 Sunday 18:00 UTC+2",
                creation_id = loggedUserID, reminder_seen = false)
        AsyncTask.execute {
            val db = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java,
                "com.example.mobilecomputing.db"
            ).build()
            val uuid1 = db.reminderDAO().insert(testReminder1).toInt()
            db.close()
        }
    }
}