package com.pocket.knowledge.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.pocket.knowledge.R
import com.pocket.knowledge.config.UiConfig
import com.pocket.knowledge.utils.ThemePref
import com.pocket.knowledge.utils.Tools.getTheme
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {
    private var isCancelled = false
    var nid: Long = 0
    var url = ""
    var themePref: ThemePref? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getTheme(this)
        setContentView(R.layout.activity_splash)
        themePref = ThemePref(this)

        if (themePref!!.isDarkTheme!!) {
            //img_splash.setImageResource(R.drawable.bg_splash_dark);
            parent_view.setBackgroundColor(resources.getColor(R.color.colorBackgroundDark))
        } else {
            //img_splash.setImageResource(R.drawable.bg_splash_default);
            parent_view.setBackgroundColor(resources.getColor(R.color.colorBackgroundLight))
        }

        progressBar.visibility = View.VISIBLE
        if (intent.hasExtra("nid")) {
            nid = intent.getLongExtra("nid", 0)
            url = intent.getStringExtra("external_link")
        }
        Handler().postDelayed({
            if (!isCancelled) {
                if (nid == 0L) {
                    if (url == "" || url == "no_url") {
                        startActivity(Intent(applicationContext, MainActivity::class.java))
                        finish()
                    } else {
                        startActivity(Intent(applicationContext, MainActivity::class.java))
                        val intent = Intent(applicationContext, WebViewActivity::class.java)
                        intent.putExtra("url", url)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                    val intent = Intent(applicationContext, NotificationDetailActivity::class.java)
                    intent.putExtra("id", nid)
                    startActivity(intent)
                    finish()
                }
            }
        }, UiConfig.SPLASH_TIME.toLong())
    }
}