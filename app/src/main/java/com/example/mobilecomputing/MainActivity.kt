package com.example.mobilecomputing

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
    var arrayList: ArrayList<TestData> = ArrayList()
    var adapter: TestAdapter? = null
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        listView = binding.listView

        arrayList.add(TestData("Reminder 1", "09.02.2021 16:00 UTC+2", "Oulu"))
        arrayList.add(TestData("Reminder 2", "10.02.2021 16:00 UTC+2", "Oulu"))
        adapter = TestAdapter(this, arrayList)
        listView.adapter = adapter

        //populateTestDb()
        
        //refreshListView()

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

class TestAdapter(private val context: Context, private val arrayList: java.util.ArrayList<TestData>) : BaseAdapter() {
    private lateinit var message: TextView
    private lateinit var time: TextView
    private lateinit var location: TextView
    override fun getCount(): Int {
        return arrayList.size
    }
    override fun getItem(position: Int): Any {
        return position
    }
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var convertView = convertView
        convertView = LayoutInflater.from(context).inflate(R.layout.reminder_item, parent, false)
        message = convertView.findViewById(R.id.reminderMessage)
        time = convertView.findViewById(R.id.reminderTime)
        location = convertView.findViewById(R.id.reminderLocation)
        message.text = " " + arrayList[position].message
        time.text = arrayList[position].time
        location.text = arrayList[position].location
        return convertView
    }
}
class TestData(var message: String, var time: String, var location: String)