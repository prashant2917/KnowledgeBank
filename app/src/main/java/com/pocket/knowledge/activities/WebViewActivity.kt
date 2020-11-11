package com.pocket.knowledge.activities

import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.pocket.knowledge.R
import com.pocket.knowledge.utils.ThemePref
import com.pocket.knowledge.utils.Tools.getTheme
import kotlinx.android.synthetic.main.activity_webview.*
import kotlinx.android.synthetic.main.include_no_network.*

class WebViewActivity : AppCompatActivity() {

    private var strUrl: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getTheme(this)
        setContentView(R.layout.activity_webview)

        val intent = intent
        if (null != intent) {
            strUrl = intent.getStringExtra("url")
        }
        displayData()
        failed_retry.setOnClickListener { view: View? ->
            lyt_failed.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
            displayData()
        }
        setupToolbar()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val themePref = ThemePref(this)
        if (themePref.isDarkTheme!!) {
            toolbar.setBackgroundColor(resources.getColor(R.color.colorToolbarDark))
        } else {
            toolbar.setBackgroundColor(resources.getColor(R.color.colorPrimary))
        }
        val actionBar = supportActionBar
        if (actionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeButtonEnabled(true)
            supportActionBar!!.title = strUrl
        }
    }

    private fun displayData() {
        val handler = Handler()
        handler.postDelayed({ loadData() }, 1000)
    }

    private fun loadData() {
        webView!!.settings.javaScriptEnabled = true
        webView!!.settings.builtInZoomControls = false
        webView!!.settings.setSupportZoom(true)
        webView!!.settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
        webView!!.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        if (Build.VERSION.SDK_INT >= 19) {
            webView!!.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else {
            webView!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
        webView!!.webViewClient = PQClient()
        webView!!.webViewClient = MyWebViewClient()
        webView!!.loadUrl(strUrl)
    }

    private inner class MyWebViewClient : WebViewClient() {
        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap) {
            super.onPageStarted(view, url, favicon)
            progressBar!!.visibility = View.VISIBLE
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {

            //start intent for "tel:" links
            var url: String? = url
            if (url != null && url.startsWith("tel:")) {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse(url))
                startActivity(intent)
                view.reload()
                return true
            }

            //start intent for "sms:" links
            if (url != null && url.startsWith("sms:")) {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(url))
                startActivity(intent)
                view.reload()
                return true
            }

            //start intent for "sms:" links
            if (url != null && url.startsWith("mailto:")) {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(url))
                startActivity(intent)
                view.reload()
                return true
            }
            if (url != null && url.startsWith("http://pin.bbm.com/")) {
                val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                i.setPackage("com.bbm")
                try {
                    startActivity(i)
                } catch (e: ActivityNotFoundException) {
                    i.setPackage(null)
                    startActivity(i)
                }
                view.reload()
                return true
            }
            if (url != null && url.startsWith("https://api.whatsapp.com/")) {
                val packageManager = packageManager
                val i = Intent(Intent.ACTION_VIEW)
                try {
                    i.setPackage("com.whatsapp")
                    i.data = Uri.parse(url)
                    if (i.resolveActivity(packageManager) != null) {
                        startActivity(i)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                view.reload()
                return true
            }
            if (url != null && url.startsWith("https://www.instagram.com/")) {
                val packageManager = packageManager
                val i = Intent(Intent.ACTION_VIEW)
                try {
                    i.setPackage("com.instagram.android")
                    i.data = Uri.parse(url)
                    if (i.resolveActivity(packageManager) != null) {
                        startActivity(i)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                view.reload()
                return true
            }
            if (url != null && url.startsWith("instagram://")) {
                val packageManager = packageManager
                val i = Intent(Intent.ACTION_VIEW)
                try {
                    i.setPackage("com.instagram.android")
                    i.data = Uri.parse(url)
                    if (i.resolveActivity(packageManager) != null) {
                        startActivity(i)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                view.reload()
                return true
            }
            if (url != null && url.startsWith("https://maps.google.com/")) {
                val packageManager = packageManager
                val i = Intent(Intent.ACTION_VIEW)
                try {
                    i.setPackage("com.google.android.apps.maps")
                    i.data = Uri.parse(url)
                    if (i.resolveActivity(packageManager) != null) {
                        startActivity(i)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                view.reload()
                return true
            }
            if (url != null && url.startsWith("file:///android_asset/[external]http")) {
                url = url.replace("file:///android_asset/[external]", "")
                view.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } else {
                view.loadUrl(url)
            }
            return true
        }

        override fun onPageFinished(view: WebView, url: String) {
            progressBar!!.visibility = View.GONE
        }

        override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
            progressBar!!.visibility = View.GONE
            Toast.makeText(applicationContext, resources.getString(R.string.msg_offline), Toast.LENGTH_LONG).show()
            view.loadUrl("about:blank")
        }
    }

    inner class PQClient : WebViewClient() {
        private var progressDialog: ProgressDialog? = null
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {

            // If url contains mailto link then open Mail Intent
            return if (url.contains("mailto:")) {

                // Could be cleverer and use a regex
                //Open links in new browser
                view.context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(url)))

                // Here we can open new activity
                true
            } else {

                // Stay within this webview and load url
                view.loadUrl(url)
                true
            }
        }

        //Show loader on url load
        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap) {

            // Then show progress  Dialog
            // in standard case YourActivity.this
            if (progressDialog == null) {
                progressDialog = ProgressDialog(applicationContext)
                progressDialog!!.setMessage("Loading...")
                progressDialog!!.hide()
            }
        }

        // Called when all page resources loaded
        override fun onPageFinished(view: WebView, url: String) {
            webView!!.loadUrl("javascript:(function(){ " +
                    "document.getElementById('android-app').style.display='none';})()")
            try {
                // Close progressDialog
                if (progressDialog!!.isShowing) {
                    progressDialog!!.dismiss()
                    progressDialog = null
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_webview, menu)
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.open_in_browser -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(strUrl)))
                true
            }
            else -> super.onOptionsItemSelected(menuItem)
        }
    }
}