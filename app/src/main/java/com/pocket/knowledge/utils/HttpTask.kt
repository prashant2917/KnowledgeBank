package com.pocket.knowledge.utils

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import org.apache.http.NameValuePair
import org.apache.http.client.HttpClient
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection

class HttpTask(private val listener: OnTaskCompleted?, private val mContext: Context, var url: String, private var nameValuePairs: List<NameValuePair>?, private var show_progress: Boolean) : AsyncTask<Void?, Void?, JSONObject?>() {
    private var result: String? = null
    private var jsonResult: JSONObject? = null
    private var pd: ProgressDialog? = null
    override fun onPostExecute(result: JSONObject?) {
        if (show_progress) {
            pd!!.dismiss()
        }

        // Call the interface method
        if (listener != null && result != null) listener.onTaskCompleted(result)
    }

    override fun onPreExecute() {
        Log.d("httpreq", "PREEXE")
        if (show_progress) {
            pd = ProgressDialog(mContext)
            pd!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            pd!!.setMessage("Connessione in corso...")
            pd!!.setCancelable(false)
            pd!!.show()
        }
        super.onPreExecute()
    }

    override fun doInBackground(vararg params: Void?): JSONObject? {
        var bytes: ByteArray? = null
        val client: HttpClient = DefaultHttpClient()
        val post = HttpPost(url)
        try {
            if (nameValuePairs != null) {
                post.entity = UrlEncodedFormEntity(nameValuePairs, "UTF-8")
            }
            val response = client.execute(post)
            val statusLine = response.statusLine
            if (statusLine.statusCode == HttpURLConnection.HTTP_OK) {
                bytes = EntityUtils.toByteArray(response.entity)
                result = String(bytes, charset("UTF-8"))
            }
            Log.d("httpreq", "Server response:$result")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return jsonResult
    }

    interface OnTaskCompleted {
        fun onTaskCompleted(result: JSONObject?)
    }

}