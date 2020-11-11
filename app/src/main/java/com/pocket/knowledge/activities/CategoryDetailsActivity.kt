package com.pocket.knowledge.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.pocket.knowledge.R
import com.pocket.knowledge.adapter.RecentAdapter
import com.pocket.knowledge.callbacks.CategoryDetailsCallback
import com.pocket.knowledge.config.AdsConfig
import com.pocket.knowledge.config.AppConfig
import com.pocket.knowledge.config.UiConfig
import com.pocket.knowledge.models.Category
import com.pocket.knowledge.models.News
import com.pocket.knowledge.notification.NotificationUtils.Companion.showDialogNotification
import com.pocket.knowledge.rests.RestAdapter.createAPI
import com.pocket.knowledge.utils.Constant
import com.pocket.knowledge.utils.NetworkCheck.Companion.isConnect
import com.pocket.knowledge.utils.ThemePref
import com.pocket.knowledge.utils.Tools.getAdRequest
import com.pocket.knowledge.utils.Tools.getTheme
import kotlinx.android.synthetic.main.activity_category_details.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class CategoryDetailsActivity : AppCompatActivity() {
    private var mAdapter: RecentAdapter? = null
    private var callbackCall: Call<CategoryDetailsCallback?>? = null
    private var category: Category? = null

    private var postTotal: Long = 0
    private var failedPage: Long = 0
    private var broadcastReceiver: BroadcastReceiver? = null
    private var interstitialAd: InterstitialAd? = null
    private var counter = 1
    private var lytShimmer: ShimmerFrameLayout? = null
    private val feedItems = ArrayList<News>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getTheme(this)
        setContentView(R.layout.activity_category_details)
        initAds()
        loadBannerAd()
        loadInterstitialAd()
        onReceiveNotification()

        // get extra object
        category = intent.getSerializableExtra(Constant.EXTRA_OBJC) as Category
        postTotal = category!!.postCount
        lytShimmer = findViewById(R.id.shimmer_view_container)
        swipe_refresh_layout.setColorSchemeResources(R.color.colorPrimary)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        //set data and list adapter
        mAdapter = RecentAdapter(this, recyclerView, feedItems)
        recyclerView.adapter = mAdapter

        // on item list clicked
        mAdapter!!.setOnItemClickListener(itemClickListener)

        // detect when scroll reach bottom
        mAdapter!!.setOnLoadMoreListener(loadMoreListener)

        // on swipe list
        swipe_refresh_layout.setOnRefreshListener {
            if (callbackCall != null && callbackCall!!.isExecuted) {
                callbackCall!!.cancel()
            }
            mAdapter!!.resetListData()
            requestAction(1)
        }
        requestAction(1)
        initToolbar()

        txt_title_toolbar.text = category!!.categoryName
    }


    private val itemClickListener = object : RecentAdapter.OnItemClickListener {

        override fun onItemClick(view: View?, obj: News?, position: Int) {
            val intent = Intent(applicationContext, PostDetailActivity::class.java)
            intent.putExtra(Constant.EXTRA_OBJC, obj)
            startActivity(intent)
            showInterstitialAd()
        }

    }

    private val loadMoreListener = object : RecentAdapter.OnLoadMoreListener {
        override fun onLoadMore(current_page: Int) {
            if (postTotal > mAdapter!!.itemCount && current_page != 0) {
                val nextPage = current_page + 1
                requestAction(nextPage.toLong())
            } else {
                mAdapter!!.setLoaded()
            }
        }


    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        val themePref = ThemePref(this)
        if (themePref.isDarkTheme!!) {
            toolbar.setBackgroundColor(resources.getColor(R.color.colorToolbarDark))
        } else {
            toolbar.setBackgroundColor(resources.getColor(R.color.colorPrimary))
        }
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_search -> {
                val intent = Intent(applicationContext, SearchActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(menuItem)
        }
    }

    private fun displayApiResult(posts: List<News?>) {
        mAdapter!!.insertData(posts)
        swipeProgress(false)
        if (posts.isEmpty()) {
            showNoItemView(true)
        }
    }

    private fun requestPostApi(page_no: Long) {
        val apiInterface = createAPI()
        callbackCall = apiInterface.getCategoryDetailsByPage(category!!.cid, AppConfig.API_KEY, page_no, UiConfig.LOAD_MORE.toLong())
        callbackCall!!.enqueue(object : Callback<CategoryDetailsCallback?> {
            override fun onResponse(call: Call<CategoryDetailsCallback?>, response: Response<CategoryDetailsCallback?>) {
                val resp = response.body()
                if (resp != null && resp.status == "ok") {
                    displayApiResult(resp.posts)
                } else {
                    onFailRequest(page_no)
                }
            }

            override fun onFailure(call: Call<CategoryDetailsCallback?>, t: Throwable) {
                if (!call.isCanceled) onFailRequest(page_no)
            }
        })
    }

    private fun onFailRequest(page_no: Long) {
        failedPage = page_no
        mAdapter!!.setLoaded()
        swipeProgress(false)
        if (isConnect(applicationContext)) {
            showFailedView(true, getString(R.string.msg_no_network))
        } else {
            showFailedView(true, getString(R.string.msg_offline))
        }
    }

    private fun requestAction(page_no: Long) {
        showFailedView(false, "")
        showNoItemView(false)
        if (page_no == 1L) {
            swipeProgress(true)
        } else {
            mAdapter!!.setLoading()
        }
        Handler().postDelayed({ requestPostApi(page_no) }, Constant.DELAY_TIME)
    }

    private fun showFailedView(show: Boolean, message: String) {
        val lytFailed = findViewById<View>(R.id.lyt_failed)
        (findViewById<View>(R.id.failed_message) as TextView).text = message
        if (show) {
            recyclerView!!.visibility = View.GONE
            lytFailed.visibility = View.VISIBLE
        } else {
            recyclerView!!.visibility = View.VISIBLE
            lytFailed.visibility = View.GONE
        }
        findViewById<View>(R.id.failed_retry).setOnClickListener { view: View? -> requestAction(failedPage) }
    }

    private fun showNoItemView(show: Boolean) {
        val lytNoItem = findViewById<View>(R.id.lyt_no_item)
        (findViewById<View>(R.id.no_item_message) as TextView).setText(R.string.msg_no_news)
        if (show) {
            recyclerView!!.visibility = View.GONE
            lytNoItem.visibility = View.VISIBLE
        } else {
            recyclerView!!.visibility = View.VISIBLE
            lytNoItem.visibility = View.GONE
        }
    }

    private fun swipeProgress(show: Boolean) {
        if (!show) {
            swipe_refresh_layout!!.isRefreshing = show
            shimmer_view_container!!.visibility = View.GONE
            shimmer_view_container!!.stopShimmer()
            return
        }
        swipe_refresh_layout!!.post {
            swipe_refresh_layout!!.isRefreshing = show
            shimmer_view_container!!.visibility = View.VISIBLE
            shimmer_view_container!!.startShimmer()
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        swipeProgress(false)
        if (callbackCall != null && callbackCall!!.isExecuted) {
            callbackCall!!.cancel()
        }
        shimmer_view_container!!.stopShimmer()
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
                interstitialAd!!.loadAd(getAdRequest(this@CategoryDetailsActivity))
            }
        }
    }

    private fun showInterstitialAd() {
        if (interstitialAd != null && interstitialAd!!.isLoaded) {
            if (counter == AdsConfig.INTERSTITIAL_AD_INTERVAL) {
                interstitialAd!!.show()
                counter = 1
            } else {
                counter++
            }
        }
    }

    private fun onReceiveNotification() {
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Constant.PUSH_NOTIFICATION) {
                    showDialogNotification(this@CategoryDetailsActivity, intent)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver!!, IntentFilter(Constant.REGISTRATION_COMPLETE))
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver!!, IntentFilter(Constant.PUSH_NOTIFICATION))
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver!!)
        super.onPause()
    }
}