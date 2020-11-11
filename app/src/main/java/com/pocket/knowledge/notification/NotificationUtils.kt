package com.pocket.knowledge.notification

import android.app.Activity
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.pocket.knowledge.R
import com.pocket.knowledge.activities.NotificationDetailActivity
import com.pocket.knowledge.activities.WebViewActivity
import com.pocket.knowledge.activities.WebViewImageActivity
import com.squareup.picasso.Picasso

class NotificationUtils {

    companion object {
        fun isAppIsInBackground(context: Context): Boolean {
            var isInBackground = true
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
                val runningProcesses = am.runningAppProcesses
                for (processInfo in runningProcesses) {
                    if (processInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        for (activeProcess in processInfo.pkgList) {
                            if (activeProcess == context.packageName) {
                                isInBackground = false
                            }
                        }
                    }
                }
            } else {
                val taskInfo = am.getRunningTasks(1)
                val componentInfo = taskInfo[0].topActivity
                if (componentInfo!!.packageName == context.packageName) {
                    isInBackground = false
                }
            }
            return isInBackground
        }

        @JvmStatic
        fun oneSignalNotificationHandler(activity: Activity, intent: Intent) {
            if (intent.hasExtra("nid")) {
                val nid = intent.getLongExtra("nid", 0)
                val url = intent.getStringExtra("external_link")
                if (nid == 0L) {
                    if (url == "" || url == "no_url") {
                        Log.d("OneSignal", "do nothing")
                    } else {
                        val act1 = Intent(activity, WebViewActivity::class.java)
                        act1.putExtra("url", url)
                        activity.startActivity(act1)
                    }
                } else {
                    val act2 = Intent(activity, NotificationDetailActivity::class.java)
                    act2.putExtra("id", nid)
                    activity.startActivity(act2)
                }
            }
        }

        @JvmStatic
        fun fcmNotificationHandler(activity: Activity, intent: Intent) {
            val fcmId = intent.getLongExtra("id", 1L)
            val url = intent.getStringExtra("link")
            if (fcmId != 1L) {
                if (fcmId == 0L) {
                    if (url != "") {
                        if (url.startsWith("http://")) {
                            val a = Intent(activity, WebViewActivity::class.java)
                            a.putExtra("url", url)
                            activity.startActivity(a)
                        }
                        if (url.startsWith("https://")) {
                            val b = Intent(activity, WebViewActivity::class.java)
                            b.putExtra("url", url)
                            activity.startActivity(b)
                        }
                        if (url.endsWith(".jpg") || url.endsWith(".jpeg") || url.endsWith(".png")) {
                            val c = Intent(activity, WebViewImageActivity::class.java)
                            c.putExtra("image_url", url)
                            activity.startActivity(c)
                        }
                        if (url.endsWith(".pdf")) {
                            val d = Intent(Intent.ACTION_VIEW)
                            d.data = Uri.parse(url)
                            activity.startActivity(d)
                        }
                    }
                    Log.d("FCM_INFO", " id : $fcmId")
                } else {
                    val action = Intent(activity, NotificationDetailActivity::class.java)
                    action.putExtra("id", fcmId)
                    activity.startActivity(action)
                    Log.d("FCM_INFO", "id : $fcmId")
                }
            }
        }

        @JvmStatic
        fun showDialogNotification(activity: Activity, intent: Intent) {
            val id = intent.getLongExtra("id", 1L)
            val title = intent.getStringExtra("title")
            val message = intent.getStringExtra("message")
            val imageUrl = intent.getStringExtra("image_url")
            val url = intent.getStringExtra("link")
            val layoutInflaterAndroid = LayoutInflater.from(activity)
            val view = layoutInflaterAndroid.inflate(R.layout.custom_dialog, null)
            val alert = AlertDialog.Builder(activity)
            alert.setView(view)
            val notificationTitle = view.findViewById<TextView>(R.id.txt_title)
            val notificationMessage = view.findViewById<TextView>(R.id.message)
            val notificationImage = view.findViewById<ImageView>(R.id.big_image)
            if (id != 1L) {
                if (id == 0L) {
                    if (url != "") {
                        notificationTitle.text = title
                        notificationMessage.text = Html.fromHtml(message)
                        Picasso.get()
                                .load(imageUrl.replace(" ", "%20"))
                                .placeholder(R.drawable.ic_thumbnail)
                                .into(notificationImage)
                        alert.setPositiveButton("Open link") { dialogInterface: DialogInterface?, i: Int ->
                            if (url.startsWith("http://")) {
                                val intent1 = Intent(activity, WebViewActivity::class.java)
                                intent1.putExtra("url", url)
                                activity.startActivity(intent1)
                            }
                            if (url.startsWith("https://")) {
                                val intent1 = Intent(activity, WebViewActivity::class.java)
                                intent1.putExtra("url", url)
                                activity.startActivity(intent1)
                            }
                            if (url.endsWith(".jpg") || url.endsWith(".jpeg") || url.endsWith(".png")) {
                                val intent1 = Intent(activity, WebViewImageActivity::class.java)
                                intent1.putExtra("image_url", url)
                                activity.startActivity(intent1)
                            }
                            if (url.endsWith(".pdf")) {
                                val intent1 = Intent(Intent.ACTION_VIEW)
                                intent1.data = Uri.parse(url)
                                activity.startActivity(intent1)
                            }
                        }
                        alert.setNegativeButton(activity.resources.getString(R.string.dialog_dismiss), null)
                    } else {
                        notificationTitle.text = title
                        notificationMessage.text = Html.fromHtml(message)
                        Picasso.get()
                                .load(imageUrl.replace(" ", "%20"))
                                .placeholder(R.drawable.ic_thumbnail)
                                .into(notificationImage)
                        alert.setPositiveButton(activity.resources.getString(R.string.dialog_ok), null)
                    }
                } else {
                    notificationTitle.text = title
                    notificationMessage.text = Html.fromHtml(message)
                    Picasso.get()
                            .load(imageUrl.replace(" ", "%20"))
                            .placeholder(R.drawable.ic_thumbnail)
                            .into(notificationImage)
                    alert.setPositiveButton(activity.resources.getString(R.string.dialog_read_more)) { dialog: DialogInterface?, which: Int ->
                        val intent12 = Intent(activity, NotificationDetailActivity::class.java)
                        intent12.putExtra("id", id)
                        activity.startActivity(intent12)
                    }
                    alert.setNegativeButton(activity.resources.getString(R.string.dialog_dismiss), null)
                }
                alert.setCancelable(false)
                alert.show()
            }
        }
    }

}