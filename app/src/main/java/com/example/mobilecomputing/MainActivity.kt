package com.example.mobilecomputing

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ListView
import android.os.AsyncTask
import android.content.Intent
import android.util.Log
import android.widget.*
import androidx.room.Room
import androidx.core.app.NotificationCompat
import com.example.mobilecomputing.db.AppDatabase
import com.example.mobilecomputing.db.ReminderInfo
import com.example.mobilecomputing.databinding.ActivityMainBinding
import com.example.mobilecomputing.databinding.ActivityLoginBinding
import com.example.mobilecomputing.databinding.ActivityProfileBinding
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

        populateTestDb()
        
        refreshListView()

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
            val reminderInfos = db.reminderDAO().getReminderInfos()
            db.close()
            return reminderInfos
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
        val testReminder1 = ReminderInfo(
            null,
            message = "Pick up Johnny from school",
            time = "2021-02-15 Monday 10:00 UTC+2",
            location = "School"
        )
        AsyncTask.execute {
            val db = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java,
                "com.example.mobilecomputing"
            ).build()
            val uuid1 = db.reminderDAO().insert(testReminder1).toInt()
            db.close()
        }
    }
}