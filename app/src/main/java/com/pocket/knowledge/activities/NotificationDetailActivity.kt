package com.pocket.knowledge.activities

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import com.pocket.knowledge.BuildConfig
import com.pocket.knowledge.R
import com.pocket.knowledge.adapter.ImageAdapter
import com.pocket.knowledge.adapter.RelatedAdapter
import com.pocket.knowledge.callbacks.PostDetailCallback
import com.pocket.knowledge.config.AppConfig
import com.pocket.knowledge.models.Images
import com.pocket.knowledge.models.News
import com.pocket.knowledge.rests.RestAdapter.createAPI
import com.pocket.knowledge.utils.AppBarLayoutBehavior
import com.pocket.knowledge.utils.Constant
import com.pocket.knowledge.utils.DbHandler
import com.pocket.knowledge.utils.NetworkCheck.Companion.isConnect
import com.pocket.knowledge.utils.ThemePref
import com.pocket.knowledge.utils.Tools.getAdRequest
import com.pocket.knowledge.utils.Tools.getFormatedDate
import com.pocket.knowledge.utils.Tools.getTheme
import kotlinx.android.synthetic.main.activity_post_detail.*
import kotlinx.android.synthetic.main.include_no_network.*
import kotlinx.android.synthetic.main.include_post_detail.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationDetailActivity : AppCompatActivity() {
    private var callbackCall: Call<PostDetailCallback?>? = null

    private var runnableCode: Runnable? = null
    private val handler = Handler()

    private var post: News? = null
    private var menu: Menu? = null

    private var databaseHandler: DbHandler? = null

    private var interstitialAd: InterstitialAd? = null
    private var bgParagraph: String? = null

    var themePref: ThemePref? = null
    private var nid: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getTheme(this)
        setContentView(R.layout.activity_post_detail)
        themePref = ThemePref(this)
        databaseHandler = DbHandler(applicationContext)
        val appBarLayout = findViewById<AppBarLayout>(R.id.appBarLayout)
        (appBarLayout.layoutParams as CoordinatorLayout.LayoutParams).behavior = AppBarLayoutBehavior()

        swipe_refresh_layout.setColorSchemeResources(R.color.colorPrimary)
        swipe_refresh_layout.isRefreshing = false



        //post = (News) getIntent().getSerializableExtra(Constant.EXTRA_OBJC);
        val intent = intent
        nid = intent.getLongExtra("id", 0)
        requestAction()
        swipe_refresh_layout.setOnRefreshListener {
            shimmer_view_container.visibility = View.VISIBLE
            shimmer_view_container.startShimmer()
            lyt_main_content.visibility = View.GONE
            requestAction()
        }
        initToolbar()
        initAds()
        loadBannerAd()
        loadInterstitialAd()
        loadNativeAd()
    }

    private fun requestAction() {
        showFailedView(false, "")
        swipeProgress(true)
        Handler().postDelayed({ requestPostData() }, 200)
    }

    private fun requestPostData() {
        callbackCall = createAPI().getNewsDetail(nid)
        callbackCall!!.enqueue(object : Callback<PostDetailCallback?> {
            override fun onResponse(call: Call<PostDetailCallback?>, response: Response<PostDetailCallback?>) {
                val responseHome = response.body()
                if (responseHome == null || responseHome.status != "ok") {
                    onFailRequest()
                    return
                }
                post = responseHome.post
                displayAllData(responseHome)
                swipeProgress(false)
                lyt_main_content!!.visibility = View.VISIBLE
            }

            override fun onFailure(call: Call<PostDetailCallback?>, th: Throwable) {
                Log.e("onFailure", th.message)
                if (!call.isCanceled) {
                    onFailRequest()
                }
            }
        })
    }

    private fun onFailRequest() {
        swipeProgress(false)
        lyt_main_content!!.visibility = View.GONE
        if (isConnect(this@NotificationDetailActivity)) {
            showFailedView(true, getString(R.string.msg_no_network))
        } else {
            showFailedView(true, getString(R.string.msg_offline))
        }
    }

    private fun showFailedView(show: Boolean, message: String) {

        failed_message.text = message
        if (show) {
            lyt_failed_home.visibility = View.VISIBLE
        } else {
            lyt_failed_home.visibility = View.GONE
        }
        failed_retry.setOnClickListener { view: View? -> requestAction() }
    }

    private fun swipeProgress(show: Boolean) {
        if (!show) {
            swipe_refresh_layout!!.isRefreshing = show
            shimmer_view_container!!.visibility = View.GONE
            shimmer_view_container!!.stopShimmer()
            lyt_main_content!!.visibility = View.VISIBLE
            return
        }
        swipe_refresh_layout!!.post {
            swipe_refresh_layout!!.isRefreshing = show
            shimmer_view_container!!.visibility = View.VISIBLE
            shimmer_view_container!!.startShimmer()
            lyt_main_content!!.visibility = View.GONE
        }
    }

    private fun startAutoSlider(position: Int) {
        if (runnableCode != null) {
            handler.removeCallbacks(runnableCode)
        }
        runnableCode = Runnable {
            var currentItem = view_pager_image!!.currentItem + 1
            if (currentItem >= position) {
                currentItem = 0
            }
            view_pager_image!!.currentItem = currentItem
            handler.postDelayed(runnableCode, 4000)
        }
        handler.postDelayed(runnableCode, 4000)
    }

    private fun displayAllData(responseHome: PostDetailCallback) {
        displayImages(responseHome.images)
        displayPostData()
        displayRelated(responseHome.related)
    }

    private fun displayPostData() {
        txt_title!!.text = Html.fromHtml(post!!.news_title)
        txt_comment_count!!.text = "" + post!!.comments_count
        Handler().postDelayed({
            if (post!!.comments_count == 0L) {
                txt_comment_text!!.setText(R.string.txt_no_comment)
            }
            if (post!!.comments_count == 1L) {
                txt_comment_text!!.text = resources.getString(R.string.txt_read) + " " + post!!.comments_count + " " + resources.getString(R.string.txt_comment)
            } else if (post!!.comments_count > 1) {
                txt_comment_text!!.text = resources.getString(R.string.txt_read) + " " + post!!.comments_count + " " + resources.getString(R.string.txt_comments)
            }
        }, 1000)

        //news_description.setBackgroundColor(Color.parseColor("#ffffff"));
        news_description!!.setBackgroundColor(Color.TRANSPARENT)
        news_description!!.settings.defaultTextEncodingName = "UTF-8"
        news_description!!.isFocusableInTouchMode = false
        news_description!!.isFocusable = false
        news_description!!.settings.javaScriptEnabled = true
        val webSettings = news_description!!.settings
        val res = resources
        val fontSize = res.getInteger(R.integer.font_size)
        webSettings.defaultFontSize = fontSize
        val mimeType = "text/html; charset=UTF-8"
        val encoding = "utf-8"
        val htmlText = post!!.news_description
        bgParagraph = if (themePref!!.isDarkTheme!!) {
            //news_description.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBackgroundDark));
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
        news_description!!.loadDataWithBaseURL(null, textDefault, mimeType, encoding, null)
        news_description!!.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (url.startsWith("http://")) {
                    val intent = Intent(applicationContext, WebViewActivity::class.java)
                    intent.putExtra("url", url)
                    startActivity(intent)
                }
                if (url.startsWith("https://")) {
                    val intent = Intent(applicationContext, WebViewActivity::class.java)
                    intent.putExtra("url", url)
                    startActivity(intent)
                }
                if (url.endsWith(".jpg") || url.endsWith(".jpeg") || url.endsWith(".png")) {
                    val intent = Intent(applicationContext, WebViewImageActivity::class.java)
                    intent.putExtra("image_url", url)
                    startActivity(intent)
                }
                if (url.endsWith(".pdf")) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(url)
                    startActivity(intent)
                }
                return true
            }
        }
        txt_category!!.text = post!!.category_name
        txt_category!!.setBackgroundColor(ContextCompat.getColor(this, R.color.colorCategory))
        txt_date!!.visibility = View.VISIBLE
        txt_date!!.text = getFormatedDate(post!!.news_date)
        if (post!!.content_type != "Post") {
            thumbnail_video!!.visibility = View.VISIBLE
        } else {
            thumbnail_video!!.visibility = View.GONE
        }
        Handler().postDelayed({ lyt_related!!.visibility = View.VISIBLE }, 1000)
        btn_comment!!.setOnClickListener { v: View? ->
            val intent = Intent(applicationContext, CommentsActivity::class.java)
            intent.putExtra("nid", post!!.nid)
            intent.putExtra("count", post!!.comments_count)
            intent.putExtra("post_title", post!!.news_title)
            startActivity(intent)
        }
        txt_comment_text!!.setOnClickListener { v: View? ->
            val intent = Intent(applicationContext, CommentsActivity::class.java)
            intent.putExtra("nid", post!!.nid)
            intent.putExtra("count", post!!.comments_count)
            intent.putExtra("post_title", post!!.news_title)
            startActivity(intent)
        }
        val actionBar = supportActionBar
        if (actionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeButtonEnabled(true)
            supportActionBar!!.title = post!!.category_name
        }
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        if (themePref!!.isDarkTheme!!) {
            toolbar.setBackgroundColor(resources.getColor(R.color.colorToolbarDark))
        } else {
            toolbar.setBackgroundColor(resources.getColor(R.color.colorPrimary))
        }
    }

    private fun displayImages(list: List<Images>) {

        val adapter = ImageAdapter(list)
        view_pager_image.adapter = adapter
        view_pager_image.offscreenPageLimit = 4
        view_pager_image.addOnPageChangeListener(object : SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position < list.size) {
                }
            }
        })

        tabDots.setupWithViewPager(view_pager_image, true)
        if (list.size > 1) {
            tabDots.visibility = View.VISIBLE
        } else {
            tabDots.visibility = View.GONE
        }
        adapter.setOnItemClickListener(itemClickListener)
    }
    private val itemClickListener= object:ImageAdapter.OnItemClickListener{
        override fun onItemClick(view: View?, p: Images?, position: Int) {
            when (p!!.content_type) {
                "youtube" -> {
                    val intent = Intent(applicationContext, YoutubePlayerActivity::class.java)
                    intent.putExtra("video_id", p.video_id)
                    startActivity(intent)
                    showInterstitialAd()
                }
                "Url" -> {
                    val intent = Intent(applicationContext, VideoPlayerActivity::class.java)
                    intent.putExtra("video_url", post!!.video_url)
                    startActivity(intent)
                    showInterstitialAd()
                }
                "Upload" -> {
                    val intent = Intent(applicationContext, VideoPlayerActivity::class.java)
                    intent.putExtra("video_url", AppConfig.ADMIN_PANEL_URL + "/upload/video/" + post!!.video_url)
                    startActivity(intent)
                    showInterstitialAd()
                }
                else -> {
                    val intent = Intent(applicationContext, ImageSliderActivity::class.java)
                    intent.putExtra("position", position)
                    intent.putExtra("nid", post!!.nid)
                    startActivity(intent)
                    showInterstitialAd()
                }
            }
        }

    }

    private fun displayRelated(list: List<News?>) {
        recycler_view_related.layoutManager = LinearLayoutManager(this@NotificationDetailActivity)
        val adapterNews = RelatedAdapter(this@NotificationDetailActivity, recycler_view_related, list as ArrayList<News?>)
        recycler_view_related.adapter = adapterNews
        recycler_view_related.isNestedScrollingEnabled = false
        adapterNews.setOnItemClickListener(relatedItemClickListener)
    }
    private val relatedItemClickListener= object: RelatedAdapter.OnItemClickListener{
        override fun onItemClick(view: View?, obj: News?, position: Int) {
            val intent = Intent(applicationContext, NotificationDetailActivity::class.java)
            intent.putExtra(Constant.EXTRA_OBJC, obj)
            startActivity(intent)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_news_detail, menu)
        this.menu = menu
        addToFavorite()
        return true
    }

    private fun addToFavorite() {
        val data = databaseHandler!!.getFavRow(nid)
        if (data.isEmpty()) {
            menu!!.getItem(0).icon = ContextCompat.getDrawable(applicationContext, R.drawable.ic_favorite_outline_white)
        } else {
            if (data[0].nid == nid) {
                menu!!.getItem(0).icon = ContextCompat.getDrawable(applicationContext, R.drawable.ic_favorite_white)
            }
        }
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.action_later -> {
                val data = databaseHandler!!.getFavRow(nid)
                if (data.isEmpty()) {
                    databaseHandler!!.addToFavorite(News(
                            post!!.nid,
                            post!!.news_title,
                            post!!.category_name,
                            post!!.news_date,
                            post!!.news_image,
                            post!!.news_description,
                            post!!.content_type,
                            post!!.video_url,
                            post!!.video_id,
                            post!!.comments_count
                    ))
                    Snackbar.make(coordinatorLayout!!, R.string.favorite_added, Snackbar.LENGTH_SHORT).show()
                    menu!!.getItem(0).icon = ContextCompat.getDrawable(applicationContext, R.drawable.ic_favorite_white)
                } else {
                    if (data[0].nid == nid) {
                        databaseHandler!!.removeFav(News(nid))
                        Snackbar.make(coordinatorLayout!!, R.string.favorite_removed, Snackbar.LENGTH_SHORT).show()
                        menu!!.getItem(0).icon = ContextCompat.getDrawable(applicationContext, R.drawable.ic_favorite_outline_white)
                    }
                }
            }
            R.id.action_share -> {
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(Intent.EXTRA_TEXT, """
     ${post!!.news_title}

     ${resources.getString(R.string.share_content)}

     https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}
     """.trimIndent())
                sendIntent.type = "text/plain"
                startActivity(sendIntent)
            }
            else -> return super.onOptionsItemSelected(menuItem)
        }
        return true
    }

    private fun initAds() {
        MobileAds.initialize(this, resources.getString(R.string.admob_app_id))
    }

    private fun loadBannerAd() {
        adView.loadAd(getAdRequest(this))
        adView.adListener = object : AdListener() {
            override fun onAdClosed() {}
            override fun onAdFailedToLoad(error: Int) {
                adView.visibility = View.GONE
            }

            override fun onAdLeftApplication() {}
            override fun onAdOpened() {}
            override fun onAdLoaded() {
                adView.visibility = View.VISIBLE
            }
        }
    }

    private fun loadInterstitialAd() {
        interstitialAd = InterstitialAd(applicationContext)
        interstitialAd!!.adUnitId = resources.getString(R.string.admob_interstitial_unit_id)
        interstitialAd!!.loadAd(getAdRequest(this))
        interstitialAd!!.adListener = object : AdListener() {
            override fun onAdClosed() {
                interstitialAd!!.loadAd(getAdRequest(this@NotificationDetailActivity))
            }
        }
    }

    private fun showInterstitialAd() {
        if (interstitialAd != null && interstitialAd!!.isLoaded) {
            interstitialAd!!.show()
        }
    }

    private fun loadNativeAd() {
    }

    public override fun onDestroy() {
        if (!(callbackCall == null || callbackCall!!.isCanceled)) {
            callbackCall!!.cancel()
        }
        shimmer_view_container!!.stopShimmer()
        super.onDestroy()
    }
}