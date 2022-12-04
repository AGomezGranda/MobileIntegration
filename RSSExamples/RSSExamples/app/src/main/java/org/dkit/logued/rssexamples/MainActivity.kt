package org.dkit.logued.rssexamples

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast

// Use an Explicit (named Service) Intent to create service
private lateinit var serviceIntent: Intent
//private lateinit var textView: TextView

private lateinit var intentFilter: IntentFilter    // for BroadcastReceiver
private lateinit var resultRssList: ArrayList<RssItem>

// create BroadcastReceiver
private val intentReceiver: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("RSS","BroadcastReceiver ... in onReceive()" )

        // extract the list of RssItem objects from Intent
        resultRssList =
            intent?.getParcelableArrayListExtra<RssItem>("RssItemsList") ?:  // Elvis operator ?:
                    throw IllegalStateException("RssItem ArrayList is null")

        Log.d("RSS", "BroadcastReceiver ... onReceive(), output list of RssItems:")

        // Iterate through the list of RssItem objects and print out their details
        // Log output is formatted here for readability in the LogCat pane.
        for( item in resultRssList) {
            Log.d("RSS", "\nRssItem\nTitle: ${item.title}\nLink: " +
                    "${item.link}\nDescription: ${item.description}\nPub Date:${item.pubDate}\n")
        }

//
//        Toast.makeText(
//            baseContext, "Work Complete!",  // or baseContext ?????
//            Toast.LENGTH_LONG
//        ).show()
    }
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        Log.d("RSS", "in onCreate()")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        serviceIntent = Intent(baseContext, RetrieveFeedService::class.java)

        // create intent filter for BroadcastReceiver
        intentFilter = IntentFilter()
        intentFilter.addAction("WORK_COMPLETE_ACTION") //note the same action as broadcast by the Service


    }

    override fun onStart() {
        super.onStart()
        registerReceiver(intentReceiver, intentFilter)
        startService(serviceIntent)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(intentReceiver)
        stopService(serviceIntent)
    }

}