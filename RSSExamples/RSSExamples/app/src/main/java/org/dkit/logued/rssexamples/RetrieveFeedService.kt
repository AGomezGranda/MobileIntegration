package org.dkit.logued.rssexamples

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.util.Xml
import android.widget.Toast
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.net.URL

class RetrieveFeedService : Service() {

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("RSS","RetrieveFeedService... onStartCommand() called.")
        Toast.makeText(this, "RetrieveFeedService started - onStartCommand() called", Toast.LENGTH_LONG).show()

        doWork()

        return START_STICKY
    }

    // called when activity issues a stopService(Intent) request
    override fun onDestroy() {
        Log.d("RSS","RSS... onDestroy() called.")
        super.onDestroy()
        Toast.makeText(this, "MyService destroyed - onDestroy() called", Toast.LENGTH_LONG).show()
    }

    fun doWork()
    {
        // Running a task in a separate thread
        // will prevent this Service from blocking the UI
        // in the MainActivity
        //
        val thread = object : Thread() {
            override fun run() {
                Log.i("RSS", "Service ... in doWork() - Thread Started")

                val resultRssList: ArrayList<RssItem>

                try {
                    resultRssList = parseRSS("https://xkcd.com/rss.xml")

                    //When finished - send a Broadcast to notify the Activity
                    val broadcastIntent = Intent()

                    //add any extras that want to be returned - nothing in this case.
                    broadcastIntent.putParcelableArrayListExtra("RssItemsList",resultRssList)

                    //set user-defined action
                    broadcastIntent.action = "WORK_COMPLETE_ACTION"

                    //broadcast the intent
                    baseContext.sendBroadcast(broadcastIntent)
                    // the broadcast is delivered, by Android system, to any BroadcastReceivers
                    // that have a corresponding intent-filter

                    //stopSelf()  // stop the service as job is done ?????????????? DL

                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                Log.i("RSS", "MyService ... in doWork() - Thread Finished")
            }
        }
        thread.start()

    }


    @Throws(IOException::class, XmlPullParserException::class)
    fun parseRSS(urlString: String): ArrayList<RssItem> {
        val parser = Xml.newPullParser()

        // create URL object from String
        val feedURL = URL(urlString)

        // create InputStream from URL
        val inputStream: InputStream = feedURL.openStream()

        // set XMLPullParser to use the input stream
        parser.setInput(inputStream, null)

        var currentRSSItem: RssItem? = null
        val resultRssList: ArrayList<RssItem> = ArrayList()

        var eventType = parser.eventType
        var done = false

        while (eventType != XmlPullParser.END_DOCUMENT && !done) {
            var name: String? = null
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    name = parser.name
                    if (name =="item") {
                        // a new item element
                        currentRSSItem = RssItem()
                    } else if (currentRSSItem != null) {
                        //we are within an <item>
                        when(name){
                            "link" -> currentRSSItem.link =parser.nextText()
                            "description" -> currentRSSItem?.description = parser.nextText()
                            "pubDate" -> currentRSSItem.pubDate = parser.nextText()
                            "title"-> currentRSSItem.title = parser.nextText()
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    name = parser.name
                    if (name=="item" && currentRSSItem != null) {
                        resultRssList.add(currentRSSItem)
                    } else if (name =="channel") {
                        done = true
                    }
                }
            }
            eventType = parser.next()
        }
        return resultRssList
    }

}