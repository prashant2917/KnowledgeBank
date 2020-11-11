package com.pocket.knowledge.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.MobileAds
import com.pocket.knowledge.R
import com.pocket.knowledge.adapter.NewsAdapter
import com.pocket.knowledge.adapter.SearchAdapter
import com.pocket.knowledge.callbacks.RecentCallback
import com.pocket.knowledge.config.AppConfig
import com.pocket.knowledge.models.News
import com.pocket.knowledge.rests.RestAdapter.createAPI
import com.pocket.knowledge.utils.Constant
import com.pocket.knowledge.utils.NetworkCheck.Companion.isConnect
import com.pocket.knowledge.utils.ThemePref
import com.pocket.knowledge.utils.Tools.getAdRequest
import com.pocket.knowledge.utils.Tools.getTheme
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.include_no_network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class SearchActivity : AppCompatActivity() {

    private var mAdapter: NewsAdapter? = null

    private var mAdapterSuggestion: SearchAdapter? = null
    private val feedItems = ArrayList<News>()


    //private ProgressBar progressBar;

    private var callbackCall: Call<RecentCallback?>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getTheme(this)
        setContentView(R.layout.activity_search)
        initComponent()
        setupToolbar()
        loadBannerAd()
    }

    private fun initComponent() {

        //progressBar = findViewById(R.id.progressBar);

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        recyclerSuggestion.layoutManager = LinearLayoutManager(this)
        recyclerSuggestion.setHasFixedSize(true)
        et_search.addTextChangedListener(textWatcher)

        //set data and list adapter
        mAdapter = NewsAdapter(this, recyclerView,feedItems)
        recyclerView.adapter = mAdapter
        mAdapter!!.setOnItemClickListener(itemClickListener)

        //set data and list adapter suggestion
        mAdapterSuggestion = SearchAdapter(this)
        recyclerSuggestion.adapter = mAdapterSuggestion
        showSuggestionSearch()
        mAdapterSuggestion!!.setOnItemClickListener(searchClickListener)
        bt_clear.setOnClickListener { view: View? -> et_search.setText("") }
        et_search.setOnEditorActionListener(OnEditorActionListener { v: TextView?, actionId: Int, event: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                searchAction()
                return@OnEditorActionListener true
            }
            false
        })
        et_search.setOnTouchListener { view: View?, motionEvent: MotionEvent? ->
            showSuggestionSearch()
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            false
        }
    }

    private val itemClickListener= object : NewsAdapter.OnItemClickListener{
        override fun onItemClick(view: View?, obj: News?, position: Int) {
            val intent = Intent(applicationContext, PostDetailActivity::class.java)
            intent.putExtra(EXTRA_OBJC, obj)
            startActivity(intent)
        }



    }
    private val searchClickListener =object: SearchAdapter.OnItemClickListener{
        override fun onItemClick(view: View?, viewModel: String?, pos: Int) {
            et_search.setText(viewModel)
            lyt_suggestion.visibility = View.GONE
            hideKeyboard()
            searchAction()
        }

    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        val themePref = ThemePref(this)
        if (themePref.isDarkTheme!!) {
            toolbar.setBackgroundColor(resources.getColor(R.color.colorToolbarDark))
            bg_view.setBackgroundColor(resources.getColor(R.color.colorBackgroundDark))
        } else {
            toolbar.setBackgroundColor(resources.getColor(R.color.colorPrimary))
            bg_view.setBackgroundColor(resources.getColor(R.color.colorBackgroundLight))
        }
        val actionBar = supportActionBar
        if (actionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeButtonEnabled(true)
            supportActionBar!!.title = ""
        }
    }

    private var textWatcher: TextWatcher = object : TextWatcher {
        override fun onTextChanged(c: CharSequence, i: Int, i1: Int, i2: Int) {
            if (c.toString().trim { it <= ' ' }.isEmpty()) {
                bt_clear!!.visibility = View.GONE
            } else {
                bt_clear!!.visibility = View.VISIBLE
            }
        }

        override fun beforeTextChanged(c: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {}
    }

    private fun requestSearchApi(query: String) {
        val apiInterface = createAPI()
        callbackCall = apiInterface.getSearchPosts(AppConfig.API_KEY, query, Constant.MAX_SEARCH_RESULT)
        callbackCall!!.enqueue(object : Callback<RecentCallback?> {
            override fun onResponse(call: Call<RecentCallback?>, response: Response<RecentCallback?>) {
                val resp = response.body()
                if (resp != null && resp.status == "ok") {
                    mAdapter!!.insertData(resp.posts)
                    if (resp.posts.isEmpty()) showNotFoundView(true)
                } else {
                    onFailRequest()
                }
                swipeProgress(false)
            }

            override fun onFailure(call: Call<RecentCallback?>, t: Throwable) {
                onFailRequest()
                swipeProgress(false)
            }
        })
    }

    private fun onFailRequest() {
        if (isConnect(this)) {
            showFailedView(true, getString(R.string.msg_no_network))
        } else {
            showFailedView(true, getString(R.string.msg_offline))
        }
    }

    private fun searchAction() {
        lyt_suggestion!!.visibility = View.GONE
        showFailedView(false, "")
        showNotFoundView(false)
        val query = et_search!!.text.toString().trim { it <= ' ' }
        if (query != "") {
            mAdapterSuggestion!!.addSearchHistory(query)
            mAdapter!!.resetListData()
            swipeProgress(true)
            Handler().postDelayed({ requestSearchApi(query) }, Constant.DELAY_TIME)
        } else {
            Toast.makeText(this, R.string.msg_search_input, Toast.LENGTH_SHORT).show()
            swipeProgress(false)
        }
    }

    private fun showSuggestionSearch() {
        mAdapterSuggestion!!.refreshItems()
        lyt_suggestion!!.visibility = View.VISIBLE
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun showFailedView(show: Boolean, message: String) {

        failed_message.text = message
        if (show) {
            recyclerView!!.visibility = View.GONE
            lyt_failed.visibility = View.VISIBLE
        } else {
            recyclerView!!.visibility = View.VISIBLE
            lyt_failed.visibility = View.GONE
        }
        failed_retry.setOnClickListener { view: View? -> searchAction() }
    }

    private fun showNotFoundView(show: Boolean) {
        val lytNoItem = findViewById<View>(R.id.lyt_no_item)
        (findViewById<View>(R.id.no_item_message) as TextView).setText(R.string.msg_no_news_found)
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
            shimmer_view_container.visibility = View.GONE
            shimmer_view_container!!.stopShimmer()
            return
        } else {
            shimmer_view_container!!.visibility = View.VISIBLE
            shimmer_view_container!!.startShimmer()
        }
    }

    override fun onBackPressed() {
        if (et_search!!.length() > 0) {
            et_search!!.setText("")
        } else {
            super.onBackPressed()
        }
    }

    private fun loadBannerAd() {
        MobileAds.initialize(this, resources.getString(R.string.admob_app_id))

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

    companion object {
        const val EXTRA_OBJC = "key.EXTRA_OBJC"
    }
}