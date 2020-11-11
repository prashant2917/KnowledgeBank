package com.pocket.knowledge.activities

import android.content.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.CompoundButton
import android.widget.FrameLayout
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomnavigation.LabelVisibilityMode
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pocket.knowledge.BuildConfig
import com.pocket.knowledge.R
import com.pocket.knowledge.callbacks.AppDataCallback
import com.pocket.knowledge.callbacks.SettingsCallback
import com.pocket.knowledge.callbacks.UserCallback
import com.pocket.knowledge.config.AppConfig
import com.pocket.knowledge.fragment.CategoryFragment
import com.pocket.knowledge.fragment.FavoriteFragment
import com.pocket.knowledge.fragment.RecentFragment
import com.pocket.knowledge.fragment.VideoFragment
import com.pocket.knowledge.models.AppData
import com.pocket.knowledge.models.Setting
import com.pocket.knowledge.models.User
import com.pocket.knowledge.notification.NotificationUtils.Companion.fcmNotificationHandler
import com.pocket.knowledge.notification.NotificationUtils.Companion.oneSignalNotificationHandler
import com.pocket.knowledge.notification.NotificationUtils.Companion.showDialogNotification
import com.pocket.knowledge.rests.RestAdapter.createAPI
import com.pocket.knowledge.utils.*
import com.pocket.knowledge.utils.AlertDialogUtils.Companion.geTwoButtonDialog
import com.pocket.knowledge.utils.AppDataUtil.Companion.getAppVersionCode
import com.pocket.knowledge.utils.GDPR.updateConsentStatus
import com.pocket.knowledge.utils.NetworkCheck.Companion.isConnect
import com.pocket.knowledge.utils.Tools.getTheme
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.include_about.*
import org.apache.http.NameValuePair
import org.apache.http.message.BasicNameValuePair
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class MainActivity : AppCompatActivity() {
    private var exitTime: Long = 0
    private var myApplication: MyApplication? = null


    var prevMenuItem: MenuItem? = null
    var pagerNumber = 4
    var preferences: SharedPreferences? = null
    private var broadcastReceiver: BroadcastReceiver? = null

    var user: User? = null
    var post: Setting? = null
    var appData: AppData? = null
    var androidId: String? = null
    private var callbackCall: Call<UserCallback?>? = null
    private var callbackCallSettings: Call<SettingsCallback?>? = null
    private var callbackCallAppData: Call<AppDataCallback?>? = null


    var themePref: ThemePref? = null
    private var mBottomSheetDialog: BottomSheetDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getTheme(this)
        setContentView(R.layout.activity_main)

        themePref = ThemePref(this)
        preferences = PreferenceManager.getDefaultSharedPreferences(baseContext)
        androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        updateConsentStatus(this)

        (tab_appbar_layout.layoutParams as CoordinatorLayout.LayoutParams).behavior = AppBarLayoutBehavior()
        myApplication = MyApplication.instance



        viewpager.adapter = MyAdapter(supportFragmentManager)
        viewpager.offscreenPageLimit = pagerNumber

        navigation.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    viewpager.currentItem = 0
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_category -> {
                    viewpager.currentItem = 1
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_video -> {
                    viewpager.currentItem = 2
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_favorite -> {
                    viewpager.currentItem = 3
                    return@OnNavigationItemSelectedListener true
                }
            }
           return@OnNavigationItemSelectedListener false
        })
        navigation.labelVisibilityMode = LabelVisibilityMode.LABEL_VISIBILITY_LABELED
        viewpager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                if (prevMenuItem != null) {
                    prevMenuItem!!.isChecked = false
                } else {
                    navigation.menu.getItem(0).isChecked = false
                }
                navigation.menu.getItem(position).isChecked = true
                prevMenuItem = navigation.menu.getItem(position)
                when (viewpager.currentItem) {
                    0 -> {
                        title_toolbar.text = resources.getString(R.string.app_name)
                    }
                    1 -> {
                        title_toolbar.text = resources.getString(R.string.title_nav_category)
                    }
                    2 -> {
                        title_toolbar.text = resources.getString(R.string.title_nav_video)
                    }
                    3 -> {
                        title_toolbar.text = resources.getString(R.string.title_nav_favorite)
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        onReceiveNotification()
        oneSignalNotificationHandler(this, intent)
        fcmNotificationHandler(this, intent)
        requestUpdateToken()
        initToolbarIcon()
        displayUserProfile()
        validate()
        requestAppData()
    }

    inner class MyAdapter internal constructor(fm: FragmentManager?) : FragmentPagerAdapter(fm!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(position: Int): Fragment {
            when (position) {
                0 -> return RecentFragment()
                1 -> return CategoryFragment()
                2 -> return VideoFragment()
                3 -> return FavoriteFragment()
            }
            return RecentFragment()
        }

        override fun getCount(): Int {
            return pagerNumber
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    private fun initToolbarIcon() {
        if (themePref!!.isDarkTheme!!) {
            toolbar.setBackgroundColor(resources.getColor(R.color.colorToolbarDark))
            navigation!!.setBackgroundColor(resources.getColor(R.color.colorToolbarDark))
        } else {
            toolbar.setBackgroundColor(resources.getColor(R.color.colorPrimary))
        }

        btn_search.setOnClickListener { view: View? -> Handler().postDelayed({ startActivity(Intent(applicationContext, SearchActivity::class.java)) }, 50) }

        btn_profile.setOnClickListener { view: View? -> Handler().postDelayed({ startActivity(Intent(applicationContext, ProfileActivity::class.java)) }, 50) }

        btn_overflow.setOnClickListener { view: View? -> showBottomSheetDialog() }
        btn_profile.visibility = View.VISIBLE
        btn_overflow.visibility = View.GONE
    }

    override fun onBackPressed() {
        if (viewpager.currentItem != 0) {
            viewpager.setCurrentItem(0, true)
        } else {
            exitApp()
        }
    }

    private fun exitApp() {
        if (System.currentTimeMillis() - exitTime > 2000) {
            Toast.makeText(this, getString(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show()
            exitTime = System.currentTimeMillis()
        } else {
            finish()
        }
    }

    private fun displayUserProfile() {
        if (myApplication!!.isLogin) {
            requestUserData()
        } else {
            img_profile.setImageResource(R.drawable.ic_account_circle_white)
        }
    }

    private fun requestUserData() {
        val apiInterface = createAPI()
        callbackCall = apiInterface.getUser(myApplication!!.userId)
        callbackCall!!.enqueue(object : Callback<UserCallback?> {
            override fun onResponse(call: Call<UserCallback?>, response: Response<UserCallback?>) {
                val resp = response.body()
                if (resp != null && resp.status == "ok") {
                    user = resp.response
                    if (user!!.image == "") {
                        img_profile.setImageResource(R.drawable.ic_account_circle_white)
                    } else {
                        Picasso.get()
                                .load(AppConfig.ADMIN_PANEL_URL + "/upload/avatar/" + user!!.image.replace(" ", "%20"))
                                .resize(54, 54)
                                .centerCrop()
                                .placeholder(R.drawable.ic_account_circle_white)
                                .into(img_profile)
                    }
                } else {
                    onFailRequest()
                }
            }

            override fun onFailure(call: Call<UserCallback?>, t: Throwable) {
                if (!call.isCanceled) onFailRequest()
            }
        })
    }

    private fun requestAppData() {
        val api = createAPI()
        callbackCallAppData = api.appData
        callbackCallAppData!!.enqueue(object : Callback<AppDataCallback?> {
            override fun onResponse(call: Call<AppDataCallback?>, response: Response<AppDataCallback?>) {
                Log.d("ACTIVITY_MAIN", "response $response")
                val resp = response.body()
                Log.d("ACTIVITY_MAIN", "status " + resp!!.status)
                if (resp != null && resp.status == "ok") {
                    appData = resp.appData
                    val versioncode = appData!!.version_code!!.toInt()
                    if (getAppVersionCode() < versioncode) {
                        geTwoButtonDialog(this@MainActivity,
                                resources.getString(R.string.update_now_title),
                                resources.getString(R.string.update_msg),
                                resources.getString(R.string.update_now_positive_Btn),
                                resources.getString(R.string.dialog_cancel), appDataCallback).show()
                    }
                } else {
                    onFailRequest()
                }
            }

            override fun onFailure(call: Call<AppDataCallback?>, t: Throwable) {
                if (!call.isCanceled) onFailRequest()
            }
        })
    }

    private fun validate() {
        val api = createAPI()
        callbackCallSettings = api.settings
        callbackCallSettings!!.enqueue(object : Callback<SettingsCallback?> {
            override fun onResponse(call: Call<SettingsCallback?>, response: Response<SettingsCallback?>) {
                val resp = response.body()
                if (resp != null && resp.status == "ok") {
                    post = resp.post
                    if (BuildConfig.APPLICATION_ID == post!!.package_name) {
                        Log.d("ACTIVITY_MAIN", "Package Name Validated")
                    } else {
                        val dialog = AlertDialog.Builder(this@MainActivity)
                        dialog.setTitle(resources.getString(R.string.whops))
                        dialog.setMessage(resources.getString(R.string.msg_validate))
                        dialog.setPositiveButton(resources.getString(R.string.dialog_ok)) { dialogInterface: DialogInterface?, i: Int -> finish() }
                        dialog.setCancelable(false)
                        dialog.show()
                        Log.d("ACTIVITY_MAIN", "Package Name NOT Validated")
                    }
                } else {
                    onFailRequest()
                }
            }

            override fun onFailure(call: Call<SettingsCallback?>, t: Throwable) {
                if (!call.isCanceled) onFailRequest()
            }
        })
    }

    private fun requestUpdateToken() {
        val apiInterface = createAPI()
        callbackCall = apiInterface.getUserToken('"'.toString() + androidId + '"')
        callbackCall!!.enqueue(object : Callback<UserCallback?> {
            override fun onResponse(call: Call<UserCallback?>, response: Response<UserCallback?>) {
                val resp = response.body()
                if (resp != null && resp.status == "ok") {
                    user = resp.response
                    val token = user!!.token
                    val uniqueId = user!!.user_unique_id
                    val prefToken = preferences!!.getString("fcm_token", null)
                    if (token == prefToken && uniqueId == androidId) {
                        Log.d("TOKEN", "FCM Token already exists")
                    } else {
                        updateRegistrationIdToBackend()
                    }
                } else {
                    onFailRequest()
                }
            }

            override fun onFailure(call: Call<UserCallback?>, t: Throwable) {
                if (!call.isCanceled) onFailRequest()
            }
        })
    }

    private fun onFailRequest() {
        if (isConnect(this)) {
            sendRegistrationIdToBackend()
        } else {
            Toast.makeText(applicationContext, getString(R.string.msg_no_network), Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendRegistrationIdToBackend() {
        Log.d("FCM_TOKEN", "Send data to server...")
        val token = preferences!!.getString("fcm_token", null)
        val appVersion = BuildConfig.VERSION_CODE.toString() + " (" + BuildConfig.VERSION_NAME + ")"
        val osVersion = currentVersion() + " " + Build.VERSION.RELEASE
        val model = Build.MODEL
        val manufacturer = Build.MANUFACTURER
        if (token != null) {
            val nameValuePairs: MutableList<NameValuePair> = ArrayList(1)
            nameValuePairs.add(BasicNameValuePair("user_android_token", token))
            nameValuePairs.add(BasicNameValuePair("user_unique_id", androidId))
            nameValuePairs.add(BasicNameValuePair("user_app_version", appVersion))
            nameValuePairs.add(BasicNameValuePair("user_os_version", osVersion))
            nameValuePairs.add(BasicNameValuePair("user_device_model", model))
            nameValuePairs.add(BasicNameValuePair("user_device_manufacturer", manufacturer))
            HttpTask(null, this@MainActivity, AppConfig.ADMIN_PANEL_URL + "/token-register.php", nameValuePairs, false).execute()
            Log.d("FCM_TOKEN_VALUE", "$token $androidId")
        }
    }

    private fun updateRegistrationIdToBackend() {
        Log.d("FCM_TOKEN", "Update data to server...")
        val token = preferences!!.getString("fcm_token", null)
        if (token != null) {
            val nameValuePairs: MutableList<NameValuePair> = ArrayList(1)
            nameValuePairs.add(BasicNameValuePair("user_android_token", token))
            nameValuePairs.add(BasicNameValuePair("user_unique_id", androidId))
            HttpTask(null, this@MainActivity, AppConfig.ADMIN_PANEL_URL + "/token-update.php", nameValuePairs, false).execute()
            Log.d("FCM_TOKEN_VALUE", "$token $androidId")
        }
    }

    private fun aboutDialog() {
        val layoutInflaterAndroid = LayoutInflater.from(this@MainActivity)
        val view = layoutInflaterAndroid.inflate(R.layout.custom_dialog_about, null)
        val alert = AlertDialog.Builder(this@MainActivity)
        alert.setView(view)
        alert.setCancelable(false)
        alert.setPositiveButton(R.string.dialog_ok) { dialog: DialogInterface, which: Int -> dialog.dismiss() }
        alert.show()
    }

    private fun showBottomSheetDialog() {
        val view = layoutInflater.inflate(R.layout.lyt_bottom_sheet, null)
        val lytBottomSheet = view.findViewById<FrameLayout>(R.id.bottom_sheet)
        val switchTheme = view.findViewById<Switch>(R.id.switch_theme)
        if (themePref!!.isDarkTheme!!) {
            switchTheme.isChecked = true
            lytBottomSheet.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded_dark)
        } else {
            switchTheme.isChecked = false
            lytBottomSheet.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded_default)
        }
        switchTheme.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            Log.e("INFO", "" + isChecked)
            themePref!!.isDarkTheme=isChecked
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
        btn_privacy_policy.setOnClickListener { action: View? -> startActivity(Intent(applicationContext, PrivacyPolicyActivity::class.java)) }
        btn_rate.setOnClickListener { action: View? -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID))) }
        btn_more.setOnClickListener { action: View? -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.play_more_apps)))) }
        btn_about.setOnClickListener { action: View? -> aboutDialog() }
        mBottomSheetDialog = BottomSheetDialog(this, R.style.SheetDialog)
        mBottomSheetDialog!!.setContentView(view)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBottomSheetDialog!!.window!!.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
        mBottomSheetDialog!!.show()
        mBottomSheetDialog!!.setOnDismissListener { dialog: DialogInterface? -> mBottomSheetDialog = null }
    }

    private fun onReceiveNotification() {
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Constant.PUSH_NOTIFICATION) {
                    showDialogNotification(this@MainActivity, intent)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver!!, IntentFilter(Constant.REGISTRATION_COMPLETE))
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver!!, IntentFilter(Constant.PUSH_NOTIFICATION))
        displayUserProfile()
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver!!)
        super.onPause()
    }

    var appDataCallback: AlertDialogCallback = object : AlertDialogCallback {
        override fun onPositiveButtonClick() {
            val appName = packageName
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=$appName")))
            } catch (exception: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appName")))
            }
        }

        override fun onNegativeButtonClick() {}
    }

    companion object {
        fun currentVersion(): String {
            val release = Build.VERSION.RELEASE.replace("(\\d+[.]\\d+)(.*)".toRegex(), "$1").toDouble()
            var codeName = "Unsupported"
            if (release >= 4.1 && release < 4.4) codeName = "Jelly Bean" else if (release < 5) codeName = "Kit Kat" else if (release < 6) codeName = "Lollipop" else if (release < 7) codeName = "Marshmallow" else if (release < 8) codeName = "Nougat" else if (release < 9) codeName = "Oreo"
            return codeName
        }
    }
}