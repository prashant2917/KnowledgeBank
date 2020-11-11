package com.pocket.knowledge.activities

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.pocket.knowledge.R
import com.pocket.knowledge.callbacks.SettingsCallback
import com.pocket.knowledge.models.Setting
import com.pocket.knowledge.rests.RestAdapter.createAPI
import com.pocket.knowledge.utils.Constant
import com.pocket.knowledge.utils.NetworkCheck.Companion.isConnect
import com.pocket.knowledge.utils.ThemePref
import com.pocket.knowledge.utils.Tools.getTheme
import kotlinx.android.synthetic.main.activity_privacy_policy.*
import kotlinx.android.synthetic.main.include_no_network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
class PrivacyPolicyActivity : AppCompatActivity() {

    var callbackCall: Call<SettingsCallback?>? = null

    private var bgParagraph: String? = null
    var themePref: ThemePref? = null
    var post: Setting? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getTheme(this)
        setContentView(R.layout.activity_privacy_policy)
        themePref = ThemePref(this)

        swipe_refresh.setColorSchemeResources(R.color.colorPrimary)

        requestAction()
        swipe_refresh.setOnRefreshListener { requestAction() }
        setupToolbar()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        if (themePref!!.isDarkTheme!!) {
            toolbar.setBackgroundColor(resources.getColor(R.color.colorToolbarDark))
        } else {
            toolbar.setBackgroundColor(resources.getColor(R.color.colorPrimary))
        }
        val actionBar = supportActionBar
        if (actionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeButtonEnabled(true)
            supportActionBar!!.title = resources.getString(R.string.title_about_privacy)
        }
    }

    private fun requestAction() {
        showFailedView(false, "")
        swipeProgress(true)
        Handler().postDelayed({ requestDetailsPostApi() }, Constant.DELAY_TIME)
    }

    private fun requestDetailsPostApi() {
        val api = createAPI()
        callbackCall = api.settings
        callbackCall!!.enqueue(object : Callback<SettingsCallback?> {
            override fun onResponse(call: Call<SettingsCallback?>, response: Response<SettingsCallback?>) {
                val resp = response.body()
                if (resp != null && resp.status == "ok") {
                    post = resp.post
                    displayPostData()
                    swipeProgress(false)
                } else {
                    onFailRequest()
                }
            }

            override fun onFailure(call: Call<SettingsCallback?>, t: Throwable) {
                if (!call.isCanceled) onFailRequest()
            }
        })
    }

    private fun onFailRequest() {
        swipeProgress(false)
        if (isConnect(this)) {
            showFailedView(true, getString(R.string.msg_no_network))
        } else {
            showFailedView(true, getString(R.string.msg_offline))
        }
    }

    private fun showFailedView(show: Boolean, message: String) {

        failed_message.text = message
        if (show) {
            lyt_main_content.visibility = View.GONE
            lyt_failed.visibility = View.VISIBLE
        } else {
            lyt_main_content.visibility = View.VISIBLE
            lyt_failed.visibility = View.GONE
        }
        failed_retry.setOnClickListener { view: View? -> requestAction() }
    }

    private fun swipeProgress(show: Boolean) {
        if (!show) {
            swipe_refresh!!.isRefreshing = show
            shimmer_view_container.visibility = View.GONE
            shimmer_view_container.stopShimmer()
            return
        }
        swipe_refresh!!.post {
            swipe_refresh!!.isRefreshing = show
            shimmer_view_container.visibility = View.VISIBLE
            shimmer_view_container.startShimmer()
        }
    }

    fun displayPostData() {

        webview_privacy_policy.setBackgroundColor(Color.parseColor("#ffffff"))
        webview_privacy_policy.isFocusableInTouchMode = false
        webview_privacy_policy.isFocusable = false
        webview_privacy_policy.settings.defaultTextEncodingName = "UTF-8"
        val webSettings = webview_privacy_policy.settings
        val res = resources
        val fontSize = res.getInteger(R.integer.font_size)
        webSettings.defaultFontSize = fontSize
        val mimeType = "text/html; charset=UTF-8"
        val encoding = "utf-8"
        val htmlText = post!!.privacy_policy
        bgParagraph = if (themePref!!.isDarkTheme!!) {
            webview_privacy_policy.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBackgroundDark))
            "<style type=\"text/css\">body{color: #eeeeee;}"
        } else {
            "<style type=\"text/css\">body{color: #000000;}"
        }
        val textDefault = ("<html><head>"
                + "<style>img{max-width:100%;height:auto;} figure{max-width:100%;height:auto;} iframe{width:100%;}</style> "
                + bgParagraph
                + "</style></head>"
                + "<body>"
                + htmlText
                + "</body></html>")
        val textRtl = ("<html dir='rtl'><head>"
                + "<style>img{max-width:100%;height:auto;} figure{max-width:100%;height:auto;} iframe{width:100%;}</style> "
                + bgParagraph
                + "</style></head>"
                + "<body>"
                + htmlText
                + "</body></html>")
        webview_privacy_policy.loadDataWithBaseURL(null, textDefault, mimeType, encoding, null)
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(menuItem)
    }
}