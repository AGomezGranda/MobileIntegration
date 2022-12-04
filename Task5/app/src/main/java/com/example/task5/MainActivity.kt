package com.example.task5


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import org.json.JSONArray
import org.json.JSONObject

// "USE YOUR OWN API KEY HERE" // enter your OpenWeatherMap API Key here (APPID)

//private val apiKey = "36cc15bb9d8e4ef98632a0e1443aba35"
//private val location = "us"
private val urlString = "https://newsapi.org/v2/everything?q=keyword&apiKey=36cc15bb9d8e4ef98632a0e1443aba35"


//private val apiKey = "98c1573e443d95c54679e7710de7c9b9"

// "USE YOUR OWN API KEY HERE" // enter your OpenWeatherMap API Key here (APPID)
//private val location = "Dublin,ie"
//private val urlString = "https://api.openweathermap.org/data/2.5/weather?q=$location&APPID=$apiKey"

private lateinit var resultJsonString: String
private lateinit var displayText: TextView
private lateinit var textView2: TextView
private lateinit var textView3: TextView
private lateinit var textView4: TextView

private val intentReceiver: BroadcastReceiver = object : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {

        Log.d("JSON", "BroadcastReceiver ... in onReceive()")

        resultJsonString = intent?.getStringExtra("jsonString")?: throw IllegalStateException("JSON String is null")

        Log.d("JSON", "BroadcastReceiver ... in onReceive(), extracting JSON String from Intent")

        displayText.text = "Raw Unparsed JSON : \n" + resultJsonString;

        object : Thread() {
            override fun run() {
                Log.d("JSON", "in run() parsing JSON")
                try {

                    //    val jsonObj = JSONObject(resultJsonString)  // get the root object (contains all JSON)
                    val jsonObj = JSONObject(resultJsonString)  // get the root object (contains all JSON)

                    val title = jsonObj.getString("title")
                    //Get a handler that can be used to post to the main thread
                    var mainHandler: Handler = Handler(Looper.getMainLooper());
                    var myRunnable: Thread = object : Thread() {
                        override fun run() {
                            textView2.text = "Title : " + title
                        }
                    }
                    mainHandler.post(myRunnable);

                    val author: JSONObject = jsonObj.getJSONObject("author")
                    val getAuthor = author.getString("author")
                    mainHandler = Handler(Looper.getMainLooper());
                    myRunnable = object : Thread() {
                        override fun run() {
                            textView3.text = "author" + getAuthor.toString()
                        }
                    }
                    mainHandler.post(myRunnable);

                    val description: JSONArray = jsonObj.getJSONArray("description")
                    val descriptionObject: JSONObject =
                        description[0] as JSONObject // get the first weather object (at index position 0)
                    val descriptionNews: String = descriptionObject.getString("description")

                    mainHandler = Handler(Looper.getMainLooper());
                    myRunnable = object : Thread() {
                        override fun run() {
                            textView4.text = "News description: " + descriptionNews
                        }
                    }
                    mainHandler.post(myRunnable);

                    Log.d("JSON", "end of run() parsing JSON")
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }.start()
    }
}
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        displayText = findViewById<TextView>(R.id.textView1)

        textView2 = findViewById<TextView>(R.id.textView2)
        textView3 = findViewById<TextView>(R.id.textView3)
        textView4 = findViewById<TextView>(R.id.textView4)

        val intentFilter = IntentFilter()
        intentFilter.addAction("JSON_RETRIEVED")
        registerReceiver(intentReceiver, intentFilter)

        startService(
            Intent(baseContext, ReadJSONService::class.java).putExtra("urlString", urlString)
        )

    }
}