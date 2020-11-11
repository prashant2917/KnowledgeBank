package com.pocket.knowledge.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.text.Html
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.onesignal.NotificationExtenderService
import com.onesignal.OSNotificationReceivedResult
import com.pocket.knowledge.R
import com.pocket.knowledge.activities.MainActivity
import com.pocket.knowledge.utils.Constant
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class MyOneSignalMessagingService : NotificationExtenderService() {
    var message: String? = null
    private var bigpicture: String? = null
    var title: String? = null
    private var cname: String? = null
    var url: String? = null
    var nid: Long = 0
    override fun onNotificationProcessing(receivedResult: OSNotificationReceivedResult): Boolean {
        title = receivedResult.payload.title
        message = receivedResult.payload.body
        bigpicture = receivedResult.payload.bigPicture
        try {
            nid = receivedResult.payload.additionalData.getLong("cat_id")
            cname = receivedResult.payload.additionalData.getString("cat_name")
            url = receivedResult.payload.additionalData.getString("external_link")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        sendNotification()
        return true
    }

    private fun sendNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent: Intent
        if (nid == 0L && url != "false" && url!!.trim { it <= ' ' }.isNotEmpty()) {
            intent = Intent(this, MainActivity::class.java)
            intent.putExtra("nid", nid)
            intent.putExtra("external_link", url)
            intent.putExtra("cname", cname)
        } else {
            intent = Intent(this, MainActivity::class.java)
            intent.putExtra("nid", nid)
            intent.putExtra("external_link", url)
            intent.putExtra("cname", cname)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(Constant.NOTIFICATION_CHANNEL_NAME, getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(mChannel)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val contentIntent = PendingIntent.getActivity(this, System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val mBuilder = NotificationCompat.Builder(this, Constant.NOTIFICATION_CHANNEL_NAME)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                .setContentTitle(title)
                .setTicker(message)
                .setAutoCancel(true)
                .setSound(uri)
                .setChannelId(Constant.NOTIFICATION_CHANNEL_NAME)
                .setLights(Color.RED, 800, 800)
        mBuilder.setSmallIcon(getNotificationIcon(mBuilder))
        if (bigpicture != null) {
            mBuilder.setStyle(NotificationCompat.BigPictureStyle().bigPicture(getBitmapFromURL(bigpicture)).setSummaryText(Html.fromHtml(message)))
            mBuilder.setContentText(Html.fromHtml(message))
        } else {
            mBuilder.setContentText(Html.fromHtml(message))
        }
        mBuilder.setContentIntent(contentIntent)
        notificationManager.notify(nid.toInt(), mBuilder.build())
    }

    private fun getNotificationIcon(notificationBuilder: NotificationCompat.Builder): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.color = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
            //return R.drawable.ic_stat_onesignal_default;
            R.mipmap.ic_launcher
        } else {
            //return R.drawable.ic_stat_onesignal_default;
            R.mipmap.ic_launcher
        }
    }

    companion object {
        fun getBitmapFromURL(src: String?): Bitmap? {
            return try {
                val url = URL(src)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input = connection.inputStream
                BitmapFactory.decodeStream(input)
            } catch (e: IOException) {
                // Log exception
                null
            }
        }
    }
}