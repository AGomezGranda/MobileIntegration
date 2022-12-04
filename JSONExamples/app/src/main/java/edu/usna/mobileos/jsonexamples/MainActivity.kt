package edu.usna.mobileos.jsonexamples

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONException
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    // weather feed for Annapolis from forecast.io using instructor's API key
    private val apiKey = "646463100831aeea0ccec00b18447d24"
    private val location = "38.9784,-76.4922"
    private val urlString = "https://api.forecast.io/forecast/$apiKey/$location"
    private lateinit var displayText: TextView
    private lateinit var tv2: TextView
    private lateinit var tv3: TextView


    private val intentReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                val feedString = intent.getStringExtra("message") ?: ""
                // display entire jSON
                //displayText.text = feedString

                //parse the JSON string as a JSONObject
                val jsonObject = JSONObject(feedString)

                //get the timezone
                val timezone = jsonObject.getString("timezone")

                //display timezone
                displayText.text = timezone



                try {
                    val jsonObj = JSONObject(feedString)
                    val currentlyObj = jsonObj.getJSONObject("currently")
                    val currentlyTemp = currentlyObj.getString("temperature")
                    val minutelyObj = jsonObj.getJSONObject("minutely")
                    val minutelySummary = minutelyObj.getString("summary")
                    val dailyObj = jsonObj.getJSONObject("daily")
                    val dailySummary = dailyObj.getString("summary")
                    displayText.text = "$currentlyTemp ${0x00B0.toChar()}F"
                    tv2.text = minutelySummary
                    tv3.text = dailySummary
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        displayText = findViewById(R.id.displayText)
        tv2 = findViewById(R.id.textView2)
        tv3 = findViewById(R.id.textView3)

        val intentFilter = IntentFilter()
        intentFilter.addAction("JSON_RETRIEVED") //note the same action as broadcast by the Service
        registerReceiver(intentReceiver, intentFilter)

        startService(
            Intent(baseContext, ReadJSONService::class.java).putExtra(
                "urlString",
                urlString
            )
        )

    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(intentReceiver)
    }


}