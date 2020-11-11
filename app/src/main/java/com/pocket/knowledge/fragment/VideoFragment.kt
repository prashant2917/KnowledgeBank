package com.pocket.knowledge.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.pocket.knowledge.R
import com.pocket.knowledge.activities.PostDetailActivity
import com.pocket.knowledge.adapter.VideoAdapter
import com.pocket.knowledge.callbacks.RecentCallback
import com.pocket.knowledge.config.AdsConfig
import com.pocket.knowledge.config.AppConfig
import com.pocket.knowledge.config.UiConfig
import com.pocket.knowledge.models.News
import com.pocket.knowledge.rests.RestAdapter.createAPI
import com.pocket.knowledge.utils.Constant
import com.pocket.knowledge.utils.NetworkCheck.Companion.isConnect
import com.pocket.knowledge.utils.Tools.getAdRequest
import kotlinx.android.synthetic.main.fragment_video.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VideoFragment : Fragment() {
    private var rootView: View? = null
    private var parentView: View? = null

    private var mAdapter: VideoAdapter? = null
    private var callbackCall: Call<RecentCallback?>? = null
    private var postTotal = 0
    private var failedPage = 0
    private var interstitialAd: InterstitialAd? = null
    private var counter = 1
    private val feedItems = ArrayList<News>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_video, container,false)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parentView = activity!!.findViewById(R.id.main_content)
        loadInterstitialAd()

        swipe_refresh_layout_home.setColorSchemeResources(R.color.colorPrimary)

        recyclerView!!.layoutManager = LinearLayoutManager(activity)
        recyclerView!!.setHasFixedSize(true)

        //set data and list adapter
        mAdapter = VideoAdapter(activity!!, recyclerView, feedItems)
        recyclerView.adapter = mAdapter

        // on item list clicked
        mAdapter!!.setOnItemClickListener (itemClickListener)

        // detect when scroll reach bottom
        mAdapter!!.setOnLoadMoreListener (loadClickListener)

        // on swipe list
        swipe_refresh_layout_home.setOnRefreshListener {
            if (callbackCall != null && callbackCall!!.isExecuted) callbackCall!!.cancel()
            mAdapter!!.resetListData()
            requestAction(1)
        }
        requestAction(1)
    }

    private val itemClickListener = object : VideoAdapter.OnItemClickListener{
        override fun onItemClick(view: View?, obj: News?, position: Int) {
            val intent = Intent(activity, PostDetailActivity::class.java)
            intent.putExtra(Constant.EXTRA_OBJC, obj)
            startActivity(intent)
            showInterstitialAd()
        }



    }

    private val loadClickListener = object : VideoAdapter.OnLoadMoreListener{
        override fun onLoadMore(current_page: Int) {
            if (postTotal > mAdapter!!.itemCount && current_page != 0) {
                val nextPage = current_page + 1
                requestAction(nextPage)
            } else {
                mAdapter!!.setLoaded()
            }
        }


    }

    private fun displayApiResult(posts: ArrayList<News>) {
        mAdapter!!.insertData(posts)
        swipeProgress(false)
        if (posts.isEmpty()) {
            showNoItemView(true)
        }
    }

    private fun requestListPostApi(page_no: Int) {
        val apiInterface = createAPI()
        callbackCall = apiInterface.getVideoPost(AppConfig.API_KEY, page_no, UiConfig.LOAD_MORE)
        callbackCall!!.enqueue(object : Callback<RecentCallback?> {
            override fun onResponse(call: Call<RecentCallback?>, response: Response<RecentCallback?>) {
                val resp = response.body()
                if (resp != null && resp.status == "ok") {
                    postTotal = resp.countTotal
                    displayApiResult(resp.posts as ArrayList<News>)
                } else {
                    onFailRequest(page_no)
                }
            }

            override fun onFailure(call: Call<RecentCallback?>, t: Throwable) {
                if (!call.isCanceled) onFailRequest(page_no)
            }
        })
    }

    private fun onFailRequest(page_no: Int) {
        failedPage = page_no
        mAdapter!!.setLoaded()
        swipeProgress(false)
        if (isConnect(activity!!)) {
            showFailedView(true, getString(R.string.msg_no_network))
        } else {
            showFailedView(true, getString(R.string.msg_offline))
        }
    }

    private fun requestAction(page_no: Int) {
        showFailedView(false, "")
        showNoItemView(false)
        if (page_no == 1) {
            swipeProgress(true)
        } else {
            mAdapter!!.setLoading()
        }
        Handler().postDelayed({ requestListPostApi(page_no) }, Constant.DELAY_TIME)
    }

    override fun onDestroyView() {
        super.onDestroyView()
       // swipeProgress(false)
        if (callbackCall != null && callbackCall!!.isExecuted) {
            callbackCall!!.cancel()
        }
       //shimmer_view_container.stopShimmer()
    }

    private fun showFailedView(show: Boolean, message: String) {
        val lytFailed = rootView!!.findViewById<View>(R.id.lyt_failed_home)
        (rootView!!.findViewById<View>(R.id.failed_message) as TextView).text = message
        if (show) {
            recyclerView!!.visibility = View.GONE
            lytFailed.visibility = View.VISIBLE
        } else {
            recyclerView!!.visibility = View.VISIBLE
            lytFailed.visibility = View.GONE
        }
        rootView!!.findViewById<View>(R.id.failed_retry).setOnClickListener { view: View? -> requestAction(failedPage) }
    }

    private fun showNoItemView(show: Boolean) {
        val lytNoItem = rootView!!.findViewById<View>(R.id.lyt_no_item_home)
        (rootView!!.findViewById<View>(R.id.no_item_message) as TextView).setText(R.string.msg_no_news)
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
            swipe_refresh_layout_home.isRefreshing = show
            shimmer_view_container.visibility = View.GONE
            shimmer_view_container.stopShimmer()
            return
        }
        swipe_refresh_layout_home.post {
            swipe_refresh_layout_home.isRefreshing = show
            shimmer_view_container.visibility = View.VISIBLE
            shimmer_view_container.startShimmer()
        }
    }

    private fun loadInterstitialAd() {
        MobileAds.initialize(activity, resources.getString(R.string.admob_app_id))
        interstitialAd = InterstitialAd(activity)
        interstitialAd!!.adUnitId = resources.getString(R.string.admob_interstitial_unit_id)
        interstitialAd!!.loadAd(getAdRequest(activity))
        interstitialAd!!.adListener = object : AdListener() {
            override fun onAdClosed() {
                interstitialAd!!.loadAd(AdRequest.Builder().build())
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
}