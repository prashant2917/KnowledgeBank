package com.pocket.knowledge.activities

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.MobileAds
import com.pocket.knowledge.R
import com.pocket.knowledge.adapter.CommentsAdapter
import com.pocket.knowledge.callbacks.CommentsCallback
import com.pocket.knowledge.callbacks.SettingsCallback
import com.pocket.knowledge.config.AppConfig
import com.pocket.knowledge.models.Comments
import com.pocket.knowledge.models.Setting
import com.pocket.knowledge.models.Value
import com.pocket.knowledge.rests.ApiInterface
import com.pocket.knowledge.rests.RestAdapter.createAPI
import com.pocket.knowledge.utils.Constant
import com.pocket.knowledge.utils.NetworkCheck.Companion.isConnect
import com.pocket.knowledge.utils.ThemePref
import com.pocket.knowledge.utils.Tools.getAdRequest
import com.pocket.knowledge.utils.Tools.getTheme
import kotlinx.android.synthetic.main.activity_comments.*
import kotlinx.android.synthetic.main.custom_dialog_comment.*
import kotlinx.android.synthetic.main.custom_dialog_edit.*
import kotlinx.android.synthetic.main.custom_dialog_reply.*
import kotlinx.android.synthetic.main.include_no_comment.*
import kotlinx.android.synthetic.main.include_no_network.*
import kotlinx.android.synthetic.main.include_post_comment.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class CommentsActivity : AppCompatActivity() {


    private var adapterCategory: CommentsAdapter? = null
    private var callbackCall: Call<CommentsCallback?>? = null
    private var callbackCallSettings: Call<SettingsCallback?>? = null
    private var staggeredGridLayoutManager: StaggeredGridLayoutManager? = null
    var nid: Long? = null
    private var commentsCount: Long? = null
    var myApplication: MyApplication? = null


   private var postTitle: String? = null

    private var progress: ProgressDialog? = null
    var post: Setting? = null

    var themePref: ThemePref? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getTheme(this)
        setContentView(R.layout.activity_comments)
        themePref = ThemePref(this)
        myApplication = MyApplication.instance
        nid = intent.getLongExtra("nid", 0)
        commentsCount = intent.getLongExtra("count", 0)
        postTitle = intent.getStringExtra("post_title")
        setupToolbar()


        swipe_refresh_layout_category.setColorSchemeResources(R.color.colorPrimary)

        recyclerView.setHasFixedSize(true)
        staggeredGridLayoutManager = StaggeredGridLayoutManager(1, 1)
        recyclerView.layoutManager = staggeredGridLayoutManager

        //set data and list adapter
        adapterCategory = CommentsAdapter(this@CommentsActivity, ArrayList())
        recyclerView.adapter = adapterCategory

        // on item list clicked
        adapterCategory!!.setOnItemClickListener(itemClickListener)


        // on swipe list
        swipe_refresh_layout_category.setOnRefreshListener {
            adapterCategory!!.resetListData()
            requestActionOnRefresh()
        }
        requestAction()
        loadBannerAd()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        if (themePref!!.isDarkTheme!!) {
            toolbar.setBackgroundColor(resources.getColor(R.color.colorToolbarDark))
            findViewById<View>(R.id.lyt_post_comment).setBackgroundColor(resources.getColor(R.color.colorToolbarDark))
        } else {
            toolbar.setBackgroundColor(resources.getColor(R.color.colorPrimary))
            findViewById<View>(R.id.lyt_post_comment).setBackgroundColor(resources.getColor(R.color.colorBackgroundLight))
        }
        val actionBar = supportActionBar
        if (actionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeButtonEnabled(true)
            supportActionBar!!.title = resources.getString(R.string.title_comments)
        }
    }



    private val itemClickListener= object:CommentsAdapter.OnItemClickListener{
        override fun onItemClick(view: View?, obj: Comments?, position: Int, context: Context?) {
            if (myApplication!!.isLogin && myApplication!!.userId == obj!!.user_id) {
                val layoutInflaterAndroid = LayoutInflater.from(context)
                val mView = layoutInflaterAndroid.inflate(R.layout.custom_dialog_edit, null)
                val alert = AlertDialog.Builder(context!!)
                alert.setView(mView)
                val alertDialog = alert.create()
                menu_edit.setOnClickListener { view: View? ->
                    alertDialog.dismiss()
                    dialogUpdateComment(obj)
                }
                menu_delete.setOnClickListener { view: View? ->
                    val builder = AlertDialog.Builder(context)
                    builder.setMessage(getString(R.string.confirm_delete_comment))
                    builder.setPositiveButton(getString(R.string.dialog_yes)) { dialog: DialogInterface?, which: Int ->
                        val retrofit = Retrofit.Builder()
                                .baseUrl(AppConfig.ADMIN_PANEL_URL + "/")
                                .addConverterFactory(GsonConverterFactory.create())
                                .build()
                        val apiInterface = retrofit.create(ApiInterface::class.java)
                        val call = apiInterface.deleteComment(obj.comment_id)
                        call!!.enqueue(object : Callback<Value?> {
                            override fun onResponse(call: Call<Value?>, response: Response<Value?>) {
                                val value = response.body()!!.value
                                val message = response.body()!!.message
                                if (value == "1") {
                                    Toast.makeText(this@CommentsActivity, message, Toast.LENGTH_SHORT).show()
                                    adapterCategory!!.resetListData()
                                    edt_comment_message.setText("")
                                    requestAction()
                                    hideKeyboard()
                                } else {
                                    Toast.makeText(this@CommentsActivity, message, Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(call: Call<Value?>, t: Throwable) {
                                t.printStackTrace()
                                Toast.makeText(this@CommentsActivity, "Network error!", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                    builder.setNegativeButton(getString(R.string.dialog_no), null)
                    val alert1 = builder.create()
                    alert1.show()
                    alertDialog.dismiss()
                }
                alertDialog.show()
            } else if (myApplication!!.isLogin) {
                val layoutInflaterAndroid = LayoutInflater.from(context)
                val mView = layoutInflaterAndroid.inflate(R.layout.custom_dialog_reply, null)
                val alert = AlertDialog.Builder(context!!)
                alert.setView(mView)
                val alertDialog = alert.create()
                menu_reply.setOnClickListener { view: View? ->
                    alertDialog.dismiss()
                    edt_comment_message.setText("@" + obj!!.name + " ")
                    edt_comment_message.setSelection(edt_comment_message.text.length)
                    edt_comment_message.requestFocus()
                    val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    manager.showSoftInput(edt_comment_message, InputMethodManager.SHOW_IMPLICIT)
                }
                alertDialog.show()
            }
        }

    }

    private fun displayApiResult(categories: List<Comments>) {
        swipeProgress(false)
        adapterCategory!!.setListData(categories)
        if (categories.isEmpty()) {
            showNoItemView(true)
        }
    }

    private fun onFailRequest() {
        swipeProgress(false)
        if (isConnect(this@CommentsActivity)) {
            showFailedView(true, getString(R.string.msg_no_network))
        } else {
            showFailedView(true, getString(R.string.msg_no_network))
        }
    }

    private fun requestCategoriesApi() {
        val apiInterface = createAPI()
        callbackCall = apiInterface.getComments(nid)
        callbackCall!!.enqueue(object : Callback<CommentsCallback?> {
            override fun onResponse(call: Call<CommentsCallback?>, response: Response<CommentsCallback?>) {
                val resp = response.body()
                if (resp != null && resp.status == "ok") {
                    displayApiResult(resp.comments)
                    initPostData()
                    Log.d("ACTIVITY_COMMENT", "Init Response")
                } else {
                    onFailRequest()
                }
            }

            override fun onFailure(call: Call<CommentsCallback?>, t: Throwable) {
                if (!call.isCanceled) onFailRequest()
            }
        })
    }

    private fun requestAction() {
        showFailedView(false, "")
        swipeProgress(true)
        showNoItemView(false)
        Handler().postDelayed({ requestCategoriesApi() }, Constant.DELAY_TIME)
    }

    private fun requestActionOnRefresh() {
        showFailedView(false, "")
        swipeProgressOnRefresh(true)
        showNoItemView(false)
        Handler().postDelayed({ requestCategoriesApi() }, Constant.DELAY_TIME)
    }

    private fun initPostData() {
        edt_comment_message!!.setOnClickListener { view: View? ->
            if (!myApplication!!.isLogin) {
                startActivity(Intent(applicationContext, UserLoginActivity::class.java))
            }
        }
        edt_comment_message!!.onFocusChangeListener = OnFocusChangeListener { v: View?, hasFocus: Boolean ->
            if (!myApplication!!.isLogin) {
                startActivity(Intent(applicationContext, UserLoginActivity::class.java))
            }
        }
        txt_comment_count.text = "" + adapterCategory!!.itemCount
        if (adapterCategory!!.itemCount <= 1) {
            txt_comment_text.text = "Comment"
        } else {
            txt_comment_text.text = "Comments"
        }
        txt_post_title.text = postTitle
        requestPostComment()
    }

    private fun requestPostComment() {
        val api = createAPI()
        callbackCallSettings = api.settings
        callbackCallSettings!!.enqueue(object : Callback<SettingsCallback?> {
            override fun onResponse(call: Call<SettingsCallback?>, response: Response<SettingsCallback?>) {
                val resp = response.body()
                if (resp != null && resp.status == "ok") {
                    post = resp.post
                    btn_post_comment!!.setOnClickListener { view: View? ->
                        when {
                            edt_comment_message!!.text.toString() == "" -> {
                                Toast.makeText(applicationContext, R.string.msg_write_comment, Toast.LENGTH_SHORT).show()
                            }
                            edt_comment_message!!.text.toString().length <= 6 -> {
                                Toast.makeText(applicationContext, R.string.msg_write_comment_character, Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                dialogSendComment()
                            }
                        }
                    }
                    Log.d("ACTIVITY_COMMENT", "Ready Post Comment")
                } else {
                    onFailRequest()
                }
            }

            override fun onFailure(call: Call<SettingsCallback?>, t: Throwable) {
                if (!call.isCanceled) onFailRequest()
            }
        })
    }

    fun dialogSendComment() {
        val builder = android.app.AlertDialog.Builder(this@CommentsActivity)
        builder.setMessage(getString(R.string.confirm_send_comment))
        builder.setPositiveButton(getString(R.string.dialog_yes)) { dialogInterface: DialogInterface?, i: Int ->
            if (post!!.comment_approval == "yes") {
                sendCommentApproval()
            } else {
                sendComment()
            }
        }
        builder.setNegativeButton(getString(R.string.dialog_no)) { dialog: DialogInterface?, which: Int -> }
        val alert = builder.create()
        alert.show()
    }

    public override fun onDestroy() {
        super.onDestroy()
        swipeProgress(false)
        if (callbackCall != null && callbackCall!!.isExecuted) {
            callbackCall!!.cancel()
        }
        shimmer_view_container!!.stopShimmer()
    }

    private fun showFailedView(flag: Boolean, message: String) {
        failed_message.text = message
        if (flag) {
            recyclerView!!.visibility = View.GONE
            lyt_failed_category.visibility = View.VISIBLE
        } else {
            recyclerView!!.visibility = View.VISIBLE
            lyt_failed_category.visibility = View.GONE
        }
        failed_retry.setOnClickListener { view: View? -> requestAction() }
    }

    private fun showNoItemView(show: Boolean) {

        txt_no_comment.text=resources.getString(R.string.msg_no_comment)
        if (show) {
            recyclerView!!.visibility = View.GONE
            lyt_no_item_category.visibility = View.VISIBLE
        } else {
            recyclerView!!.visibility = View.VISIBLE
            lyt_no_item_category.visibility = View.GONE
        }
    }

    private fun swipeProgress(show: Boolean) {
        if (!show) {
            swipe_refresh_layout_category!!.isRefreshing = false
            shimmer_view_container!!.visibility = View.GONE
            shimmer_view_container!!.stopShimmer()
            lyt_comment_header!!.visibility = View.VISIBLE
            return
        }
        swipe_refresh_layout_category!!.post {
            swipe_refresh_layout_category!!.isRefreshing = false
            shimmer_view_container!!.visibility = View.VISIBLE
            shimmer_view_container!!.startShimmer()
            lyt_comment_header!!.visibility = View.INVISIBLE
        }
    }

    private fun swipeProgressOnRefresh(show: Boolean) {
        if (!show) {
            swipe_refresh_layout_category!!.isRefreshing = show
            shimmer_view_container!!.visibility = View.GONE
            shimmer_view_container!!.stopShimmer()
            lyt_comment_header!!.visibility = View.VISIBLE
            return
        }
        swipe_refresh_layout_category!!.post {
            swipe_refresh_layout_category!!.isRefreshing = show
            shimmer_view_container!!.visibility = View.VISIBLE
            shimmer_view_container!!.startShimmer()
            lyt_comment_header!!.visibility = View.INVISIBLE
        }
    }

    private fun sendComment() {
        progress = ProgressDialog(this)
        progress!!.setCancelable(false)
        progress!!.setMessage(resources.getString(R.string.sending_comment))
        progress!!.show()
        val content = edt_comment_message!!.text.toString()
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val dateTime = simpleDateFormat.format(Date())
        val retrofit = Retrofit.Builder()
                .baseUrl(AppConfig.ADMIN_PANEL_URL + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        val apiInterface = retrofit.create(ApiInterface::class.java)
        val call = apiInterface.sendComment(nid, myApplication!!.userId, content, dateTime)
        call!!.enqueue(object : Callback<Value?> {
            override fun onResponse(call: Call<Value?>, response: Response<Value?>) {
                val value = response.body()!!.value
                val message = response.body()!!.message
                Handler().postDelayed({
                    progress!!.dismiss()
                    if (value == "1") {
                        Toast.makeText(applicationContext, R.string.msg_comment_success, Toast.LENGTH_SHORT).show()
                        edt_comment_message!!.setText("")
                        adapterCategory!!.resetListData()
                        requestAction()
                        hideKeyboard()
                    } else {
                        Toast.makeText(applicationContext, R.string.msg_comment_failed, Toast.LENGTH_SHORT).show()
                    }
                }, Constant.DELAY_REFRESH)
            }

            override fun onFailure(call: Call<Value?>, t: Throwable) {
                progress!!.dismiss()
                Toast.makeText(applicationContext, "Network Error!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun sendCommentApproval() {
        progress = ProgressDialog(this)
        progress!!.setCancelable(false)
        progress!!.setMessage(resources.getString(R.string.sending_comment))
        progress!!.show()
        val content = edt_comment_message!!.text.toString()
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val dateTime = simpleDateFormat.format(Date())
        val retrofit = Retrofit.Builder()
                .baseUrl(AppConfig.ADMIN_PANEL_URL + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        val apiInterface = retrofit.create(ApiInterface::class.java)
        val call = apiInterface.sendComment(nid, myApplication!!.userId, content, dateTime)
        call!!.enqueue(object : Callback<Value?> {
            override fun onResponse(call: Call<Value?>, response: Response<Value?>?) {
                val value = response!!.body()!!.value
                val message = response.body()!!.message
                Handler().postDelayed({
                    progress!!.dismiss()
                    if (value == "1") {
                        val builder = android.app.AlertDialog.Builder(this@CommentsActivity)
                        builder.setMessage(R.string.msg_comment_approval)
                        builder.setPositiveButton(getString(R.string.dialog_ok)) { dialogInterface: DialogInterface?, i: Int ->
                            Toast.makeText(applicationContext, R.string.msg_comment_success, Toast.LENGTH_SHORT).show()
                            edt_comment_message!!.setText("")
                            adapterCategory!!.resetListData()
                            requestAction()
                            hideKeyboard()
                        }
                        val alert = builder.create()
                        alert.show()
                    } else {
                        Toast.makeText(applicationContext, R.string.msg_comment_failed, Toast.LENGTH_SHORT).show()
                    }
                }, Constant.DELAY_REFRESH)
            }

            override fun onFailure(call: Call<Value?>, t: Throwable) {
                progress!!.dismiss()
                Toast.makeText(applicationContext, "Network Error!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun dialogUpdateComment(obj: Comments) {
        val layoutInflaterAndroid = LayoutInflater.from(this@CommentsActivity)
        val view = layoutInflaterAndroid.inflate(R.layout.custom_dialog_comment, null)
        if (themePref!!.isDarkTheme!!) {
            view.findViewById<View>(R.id.header_update_comment).setBackgroundColor(resources.getColor(R.color.colorToolbarDark))
        } else {
            view.findViewById<View>(R.id.header_update_comment).setBackgroundColor(resources.getColor(R.color.colorPrimary))
        }
        edt_id.setText(obj.comment_id)
        edt_date_time.setText(obj.date_time)
        edt_update_message.setText(obj.content)
        edt_update_message.requestFocus()
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(edt_update_message, InputMethodManager.SHOW_IMPLICIT)
        val alert = AlertDialog.Builder(this@CommentsActivity)
        alert.setView(view)
        alert.setCancelable(false)
        alert.setPositiveButton("UPDATE") { dialog: DialogInterface, which: Int ->
            when {
                edt_update_message.text.toString() == "" -> {
                    Toast.makeText(applicationContext, R.string.msg_write_comment, Toast.LENGTH_SHORT).show()
                }
                edt_update_message.text.toString().length <= 6 -> {
                    Toast.makeText(applicationContext, R.string.msg_write_comment_character, Toast.LENGTH_SHORT).show()
                }
                else -> {
                    dialog.dismiss()
                    hideKeyboard()
                    progress = ProgressDialog(this)
                    progress!!.setCancelable(false)
                    progress!!.setMessage(resources.getString(R.string.updating_comment))
                    progress!!.show()
                    val commentId = edt_id.text.toString()
                    val dateTime = edt_date_time.text.toString()
                    val content = edt_update_message.text.toString()
                    val retrofit = Retrofit.Builder()
                            .baseUrl(AppConfig.ADMIN_PANEL_URL + "/")
                            .addConverterFactory(GsonConverterFactory.create())
                            .build()
                    val apiInterface = retrofit.create(ApiInterface::class.java)
                    val call = apiInterface.updateComment(commentId, dateTime, content)
                    call!!.enqueue(object : Callback<Value?> {
                        override fun onResponse(call: Call<Value?>, response: Response<Value?>) {
                            val value = response.body()!!.value
                            val message = response.body()!!.message
                            Handler().postDelayed({
                                progress!!.dismiss()
                                if (value == "1") {
                                    Toast.makeText(applicationContext, R.string.msg_comment_update, Toast.LENGTH_SHORT).show()
                                    adapterCategory!!.resetListData()
                                    requestAction()
                                    hideKeyboard()
                                } else {
                                    Toast.makeText(applicationContext, R.string.msg_update_comment_failed, Toast.LENGTH_SHORT).show()
                                }
                            }, Constant.DELAY_REFRESH)
                        }

                        override fun onFailure(call: Call<Value?>, t: Throwable) {
                            t.printStackTrace()
                            progress!!.dismiss()
                            Toast.makeText(applicationContext, "Jaringan Error!", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }
        }
        alert.setNegativeButton("CANCEL") { dialog: DialogInterface, which: Int ->
            dialog.dismiss()
            hideKeyboard()
        }
        alert.show()
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            android.R.id.home -> {
                if (edt_comment_message!!.length() > 0) {
                    edt_comment_message!!.setText("")
                } else {
                    onBackPressed()
                }
                true
            }
            else -> super.onOptionsItemSelected(menuItem)
        }
    }

    public override fun onResume() {
        super.onResume()
        adapterCategory!!.resetListData()
        requestAction()
    }

    override fun onBackPressed() {
        if (edt_comment_message!!.length() > 0) {
            edt_comment_message!!.setText("")
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
}