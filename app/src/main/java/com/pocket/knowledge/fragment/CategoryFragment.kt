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
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.pocket.knowledge.R
import com.pocket.knowledge.activities.CategoryDetailsActivity
import com.pocket.knowledge.adapter.CategoryAdapter
import com.pocket.knowledge.callbacks.CategoriesCallback
import com.pocket.knowledge.config.AdsConfig
import com.pocket.knowledge.config.AppConfig
import com.pocket.knowledge.models.Category
import com.pocket.knowledge.rests.RestAdapter.createAPI
import com.pocket.knowledge.utils.Constant
import com.pocket.knowledge.utils.ItemOffsetDecoration
import com.pocket.knowledge.utils.NetworkCheck.Companion.isConnect
import com.pocket.knowledge.utils.Tools.getAdRequest
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_category.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class CategoryFragment : Fragment() {
    private var rootView: View? = null
    private var parentView: View? = null

    private var mAdapter: CategoryAdapter? = null
    private var callbackCall: Call<CategoriesCallback?>? = null
    private var interstitialAd: InterstitialAd? = null
    private var counter = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_category, container,false)

        return rootView


        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parentView = activity!!.main_content
        loadInterstitialAd()


        swipe_refresh_layout_category.setColorSchemeResources(R.color.colorPrimary)

        val itemDecoration = ItemOffsetDecoration(activity!!, R.dimen.item_offset)
        recyclerViewCategory.addItemDecoration(itemDecoration)
        recyclerViewCategory.layoutManager = StaggeredGridLayoutManager(3, LinearLayoutManager.VERTICAL)
        recyclerViewCategory.setHasFixedSize(true)

        //set data and list adapter
        mAdapter = CategoryAdapter(ArrayList())
        recyclerViewCategory.adapter = mAdapter

        // on item list clicked
        mAdapter?.setOnItemClickListener(itemclickListener)

        swipe_refresh_layout_category.setOnRefreshListener {
            mAdapter!!.resetListData()
            requestAction()
        }
        requestAction()
    }

    private val  itemclickListener= object : CategoryAdapter.OnItemClickListener{
        override fun onItemClick(view: View?, obj: Category?, position: Int) {
            val intent = Intent(activity, CategoryDetailsActivity::class.java)
            intent.putExtra(Constant.EXTRA_OBJC, obj)
            startActivity(intent)
            showInterstitialAd()
        }

        // on swipe list

    }

    private fun displayApiResult(categories: List<Category>) {
        mAdapter!!.setListData(categories)
        swipeProgress(false)
        if (categories.isEmpty()) {
            showNoItemView(true)
        }
    }

    private fun requestCategoriesApi() {
        val apiInterface = createAPI()
        callbackCall = apiInterface.getAllCategories(AppConfig.API_KEY)
        callbackCall!!.enqueue(object : Callback<CategoriesCallback?> {
            override fun onResponse(call: Call<CategoriesCallback?>, response: Response<CategoriesCallback?>) {
                val resp = response.body()
                if (resp != null && resp.status == "ok") {
                    displayApiResult(resp.categories)
                } else {
                    onFailRequest()
                }
            }

            override fun onFailure(call: Call<CategoriesCallback?>, t: Throwable) {
                if (!call.isCanceled) onFailRequest()
            }
        })
    }

    private fun onFailRequest() {
        swipeProgress(false)
        if (isConnect(activity!!)) {
            showFailedView(true, getString(R.string.msg_no_network))
        } else {
            showFailedView(true, getString(R.string.msg_offline))
        }
    }

    private fun requestAction() {
        showFailedView(false, "")
        swipeProgress(true)
        showNoItemView(false)
        Handler().postDelayed({ requestCategoriesApi() }, Constant.DELAY_TIME)
    }

    override fun onDestroy() {
        super.onDestroy()
       // swipeProgress(false)
        if (callbackCall != null && callbackCall!!.isExecuted) {
            callbackCall!!.cancel()
        }
       // shimmer_view_container.stopShimmer()
    }

    private fun showFailedView(flag: Boolean, message: String) {
        val lytFailed = rootView!!.findViewById<View>(R.id.lyt_failed_category)
        (rootView!!.findViewById<View>(R.id.failed_message) as TextView).text = message
        if (flag) {
            recyclerViewCategory.visibility = View.GONE
            lytFailed.visibility = View.VISIBLE
        } else {
            recyclerViewCategory.visibility = View.VISIBLE
            lytFailed.visibility = View.GONE
        }
        rootView!!.findViewById<View>(R.id.failed_retry).setOnClickListener { view: View? -> requestAction() }
    }

    private fun showNoItemView(show: Boolean) {
        val lytNoItem = rootView!!.findViewById<View>(R.id.lyt_no_item_category)
        (rootView!!.findViewById<View>(R.id.no_item_message) as TextView).setText(R.string.msg_no_category)
        if (show) {
            recyclerViewCategory.visibility = View.GONE
            lytNoItem.visibility = View.VISIBLE
        } else {
            recyclerViewCategory.visibility = View.VISIBLE
            lytNoItem.visibility = View.GONE
        }
    }

    private fun swipeProgress(show: Boolean) {
        if (!show) {

            swipe_refresh_layout_category.isRefreshing = show
            shimmer_view_container.visibility = View.GONE
            shimmer_view_container.stopShimmer()
            return
        }
        swipe_refresh_layout_category.post {
            swipe_refresh_layout_category.isRefreshing = show
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
                interstitialAd!!.loadAd(getAdRequest(activity))
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