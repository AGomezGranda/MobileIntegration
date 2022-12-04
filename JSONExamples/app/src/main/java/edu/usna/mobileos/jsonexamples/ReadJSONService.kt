package edu.usna.mobileos.jsonexamples

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.io.BufferedOutputStream
import java.io.BufferedWriter
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.*


class ReadJSONService : Service() {

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("IT472", "Service started")

        val sslTrustManager = USNASSLTrustManager()
        HttpsURLConnection.setDefaultSSLSocketFactory(sslTrustManager.GetSocketFactory())

        doWork(intent)
        return START_STICKY
    }

    private fun doWork(intent: Intent?) {

        val urlString = intent?.getStringExtra("urlString") ?: ""

        val myThread = Thread {

//            //example of sending data using post and retrieving the response
//            Log.i("IT472", "send post data")
//            val confirmation = postAndRetrieveSiteContent("https://www.usna.edu/Users/cs/adina/teaching/it472/scripts/survey.py", "pizza", "red")
//            Log.i("IT472", "retrieving confirmation $confirmation")

            Log.i("IT472", "retrieving JSON from $urlString")
            val feedString = retrieveSiteContent(urlString)
            Log.i("IT472", "received $feedString")

            //broadcast intent
            val broadcastIntent = Intent()
            broadcastIntent.putExtra("message", feedString)
            broadcastIntent.action = "JSON_RETRIEVED"
            baseContext.sendBroadcast(broadcastIntent)

            //stop the service
            stopSelf()
        }
        myThread.start()
    }


    private fun postAndRetrieveSiteContent(
        siteURL: String,
        favFood: String,
        favColor: String
    ): String {


        var returnString = ""

        try {
            val url = URL(siteURL) //this urlString should not include the query string

            val urlConnection = url.openConnection() as HttpURLConnection

            //enable sending POST data through the connection
            urlConnection.doOutput = true
            urlConnection.setChunkedStreamingMode(0) //0 means use the default value for size of chunks

            //if the length of the data is known, use setFixedLengthStreamingMode(lenght) instead

            //send POST data by writing the query string key1=value1&key2=value2...to the output stream
            val out: OutputStream = BufferedOutputStream(urlConnection.outputStream)
            val writer = BufferedWriter(OutputStreamWriter(out, "UTF-8"))
            writer.write(
                "food=" + URLEncoder.encode(favFood, "UTF-8")
                    .toString() + "&" + "color=" + URLEncoder.encode(favColor, "UTF-8")
            )
            writer.flush()
            writer.close()
            out.close()

            //start reading some incoming data - this is a required step to ensure the request was actually sent
            val responseCode = urlConnection.responseCode

            //read the data from the connection
            val reader = urlConnection.inputStream.bufferedReader()
            returnString = reader.readText()
            reader.close()
            urlConnection.disconnect()

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        return returnString
    }

    private fun retrieveSiteContent(urlString: String): String {

        var returnString = ""
        try {
            val url = URL(urlString)
            val urlConnection = url.openConnection() as HttpURLConnection
            val reader = urlConnection.inputStream.bufferedReader()
            returnString = reader.readText()
            reader.close()
            urlConnection.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return returnString
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.i("IT472", "Service destroyed")
    }
}

//Trust manager for ignoring certificates: https://stackoverflow.com/questions/13742028/overriding-the-ssl-trust-manager-in-android
internal class USNASSLTrustManager {
    private var origTrustmanager: X509TrustManager? = null
    fun GetSocketFactory(): SSLSocketFactory? {
        return try {
            val wrappedTrustManagers = arrayOf<TrustManager>(
                object : X509TrustManager {
                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return origTrustmanager!!.acceptedIssuers
                    }

                    override fun checkClientTrusted(
                        certs: Array<X509Certificate?>?,
                        authType: String?
                    ) {
                        try {
                            origTrustmanager!!.checkClientTrusted(certs, authType)
                        } catch (e: CertificateException) {
                        }
                    }

                    @Throws(CertificateException::class)
                    override fun checkServerTrusted(
                        certs: Array<X509Certificate?>?,
                        authType: String?
                    ) {
                        try {
                            origTrustmanager!!.checkServerTrusted(certs, authType)
                        } catch (ex: Exception) {
                        }
                    }
                }
            )
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, wrappedTrustManagers, SecureRandom())
            sslContext.socketFactory
        } catch (ex: Exception) {
            null
        }
    }

    init {
        try {
            val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            tmf.init(null as KeyStore?)
            val trustManagers = tmf.trustManagers
            origTrustmanager = trustManagers[0] as X509TrustManager
        } catch (ex: Exception) {
        }
    }
}
