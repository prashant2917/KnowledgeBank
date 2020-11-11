package com.pocket.knowledge.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.text.Html
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.pocket.knowledge.R
import com.pocket.knowledge.activities.MainActivity
import com.pocket.knowledge.utils.Constant
import java.net.HttpURLConnection
import java.net.URL

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private var preferences: SharedPreferences? = null
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        preferences = PreferenceManager.getDefaultSharedPreferences(baseContext)

        //Displaying data in log
        //It is optional
        Log.d(TAG, "From: " + remoteMessage.from)
        Log.d(TAG, "Notification Message Body: " + remoteMessage.data["title"])
        Log.d(TAG, "Notification Message Link: " + remoteMessage.data["link"])
        Log.d(TAG, "Notification Message Image: " + remoteMessage.data["image"])
        val imageUrl = remoteMessage.data["image"]
        val bitmap = getBitmapfromUrl(imageUrl)
        val id = remoteMessage.data["id"]!!.toLong()
        val title = remoteMessage.data["title"]
        val message = remoteMessage.data["message"]
        val link = remoteMessage.data["link"]
        if (!NotificationUtils.isAppIsInBackground(applicationContext)) {
            val intent = Intent(Constant.PUSH_NOTIFICATION)
            intent.putExtra("id", id)
            intent.putExtra("title", title)
            intent.putExtra("message", message)
            intent.putExtra("image_url", imageUrl)
            intent.putExtra("link", link)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

            //NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
            //notificationUtils.playNotificationSound();
            sendNotification(id, title, message, link, bitmap)
        } else {
            //Calling method to generate notification
            sendNotification(id, title, message, link, bitmap)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        preferences = PreferenceManager.getDefaultSharedPreferences(baseContext)
        val editor = preferences?.edit()
        editor!!.putString("fcm_token", token)
        editor.apply()
    }

    //This method is only generating push notification
    //It is same as we did in earlier posts
    private fun sendNotification(id: Long, title: String?, message: String?, link: String?, bitmap: Bitmap?) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mBuilder: NotificationCompat.Builder
            val style = NotificationCompat.BigPictureStyle()
            style.bigPicture(bitmap)
            style.setBigContentTitle(title)
            style.setSummaryText(Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY))
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.putExtra("id", id)
            intent.putExtra("link", link)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntent = PendingIntent.getActivity(applicationContext, System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val notificationChannel = NotificationChannel("my_channel_id_001", "My Notifications", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
            mBuilder = NotificationCompat.Builder(baseContext, notificationChannel.id)
            mBuilder.setContentTitle(title)
                    .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)) //.setSmallIcon(R.drawable.ic_stat_onesignal_default)
                    .setSmallIcon(getNotificationIcon(mBuilder))
                    .setContentText(Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY))
                    .setStyle(style)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                    .setContentIntent(pendingIntent)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            val notification = mBuilder.build()
            notificationManager.notify(id.toInt(), notification)
        } else {
            val notiStyle = NotificationCompat.BigPictureStyle()
            notiStyle.bigPicture(bitmap)
            notiStyle.setBigContentTitle(title)
            notiStyle.setSummaryText(Html.fromHtml(message))
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.putExtra("id", id)
            intent.putExtra("link", link)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntent = PendingIntent.getActivity(applicationContext, System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val mBuilder = NotificationCompat.Builder(this, Constant.NOTIFICATION_CHANNEL_NAME)
                    .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)) //.setSmallIcon(R.drawable.ic_stat_onesignal_default)
                    .setContentTitle(title)
                    .setContentText(Html.fromHtml(message))
                    .setStyle(notiStyle)
                    .setAutoCancel(true)
            mBuilder.setSmallIcon(getNotificationIcon(mBuilder))
            if (preferences!!.getBoolean("notifications_new_message_vibrate", true)) {
                mBuilder.setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            }
            if (preferences!!.getString("notifications_new_message_ringtone", null) != null) {
                mBuilder.setSound(Uri.parse(preferences!!.getString("notifications_new_message_ringtone", null)))
            } else {
                val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                mBuilder.setSound(alarmSound)
            }
            if (preferences!!.getBoolean("notifications_new_message", true)) {
                mBuilder.setContentIntent(pendingIntent)
                notificationManager.notify(id.toInt(), mBuilder.build())
            }
        }
    }

    private fun getNotificationIcon(notificationBuilder: NotificationCompat.Builder): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.color = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
            // return R.drawable.ic_stat_onesignal_default;
            R.mipmap.ic_launcher
        } else {
            //return R.drawable.ic_stat_onesignal_default;
            R.mipmap.ic_launcher
        }
    }

    private fun getBitmapfromUrl(imageUrl: String?): Bitmap? {
        return try {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}