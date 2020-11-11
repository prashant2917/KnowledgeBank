package com.pocket.knowledge.activities

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.pocket.knowledge.BuildConfig
import com.pocket.knowledge.R
import com.pocket.knowledge.callbacks.UserCallback
import com.pocket.knowledge.config.AppConfig
import com.pocket.knowledge.models.User
import com.pocket.knowledge.rests.RestAdapter.createAPI
import com.pocket.knowledge.utils.Constant
import com.pocket.knowledge.utils.NetworkCheck.Companion.isConnect
import com.pocket.knowledge.utils.ThemePref
import com.pocket.knowledge.utils.Tools.getTheme
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.include_about.*
import kotlinx.android.synthetic.main.include_sign_in.*
import kotlinx.android.synthetic.main.include_sign_out.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity() {
    private var callbackCall: Call<UserCallback?>? = null
    private var myApplication: MyApplication? = null
    var user: User? = null
    private var progressDialog: ProgressDialog? = null

    var themePref: ThemePref? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getTheme(this)
        setContentView(R.layout.activity_profile)
        themePref = ThemePref(this)
        myApplication = MyApplication.instance
        setupToolbar()
        initComponent()
        requestAction()
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
            supportActionBar!!.title = resources.getString(R.string.title_menu_profile)
        }
    }

    private fun initComponent() {


        switch_theme.isChecked = themePref!!.isDarkTheme!!
        switch_theme.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            Log.e("INFO", "" + isChecked)
            themePref!!.isDarkTheme = isChecked
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
        btn_privacy_policy.setOnClickListener { startActivity(Intent(applicationContext, PrivacyPolicyActivity::class.java)) }
        btn_rate.setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID))) }
        btn_more.setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.play_more_apps)))) }
        btn_about.setOnClickListener { aboutDialog() }
    }

    private fun requestAction() {
        if (myApplication!!.isLogin) {
            view_sign_in.visibility = View.VISIBLE
            view_sign_out.visibility = View.GONE
            btn_logout!!.visibility = View.VISIBLE
            btn_logout!!.setOnClickListener { view: View? -> logoutDialog() }
            requestPostApi()
        } else {
            view_sign_in.visibility = View.GONE
            view_sign_out.visibility = View.VISIBLE
            btn_login.setOnClickListener { v: View? -> startActivity(Intent(applicationContext, UserLoginActivity::class.java)) }
            txt_register.setOnClickListener { v: View? -> startActivity(Intent(applicationContext, UserRegisterActivity::class.java)) }
            btn_logout!!.visibility = View.GONE
        }
    }

    private fun requestPostApi() {
        val apiInterface = createAPI()
        callbackCall = apiInterface.getUser(myApplication!!.userId)
        callbackCall!!.enqueue(object : Callback<UserCallback?> {
            override fun onResponse(call: Call<UserCallback?>, response: Response<UserCallback?>) {
                val resp = response.body()
                if (resp != null && resp.status == "ok") {
                    user = resp.response
                    displayData()
                } else {
                    onFailRequest()
                }
            }

            override fun onFailure(call: Call<UserCallback?>, t: Throwable) {
                if (!call.isCanceled) onFailRequest()
            }
        })
    }

    fun displayData() {
        txt_username.text = user!!.name
        txt_email.text = user!!.email

        if (user!!.image == "") {
            img_profile.setImageResource(R.drawable.ic_user_account)
        } else {
            Picasso.get()
                    .load(AppConfig.ADMIN_PANEL_URL + "/upload/avatar/" + user!!.image.replace(" ", "%20"))
                    .resize(300, 300)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_account)
                    .into(img_profile)
        }
        btn_edit.setOnClickListener { view: View? ->
            val intent = Intent(applicationContext, EditProfileActivity::class.java)
            intent.putExtra("name", user!!.name)
            intent.putExtra("email", user!!.email)
            intent.putExtra("user_image", user!!.image)
            intent.putExtra("password", user!!.password)
            startActivity(intent)
        }
    }

    private fun onFailRequest() {
        if (!isConnect(this)) {
            Toast.makeText(applicationContext, getString(R.string.msg_no_network), Toast.LENGTH_SHORT).show()
        }
    }

    private fun logoutDialog() {
        val builder = AlertDialog.Builder(this@ProfileActivity)
        builder.setTitle(R.string.logout_title)
        builder.setMessage(R.string.logout_message)
        builder.setPositiveButton(R.string.dialog_yes) { di: DialogInterface?, i: Int ->
            progressDialog = ProgressDialog(this@ProfileActivity)
            progressDialog!!.setTitle(resources.getString(R.string.title_please_wait))
            progressDialog!!.setMessage(resources.getString(R.string.logout_process))
            progressDialog!!.setCancelable(false)
            progressDialog!!.show()
            MyApplication.instance.saveIsLogin(false)
            Handler().postDelayed({
                progressDialog!!.dismiss()
                val builder1 = AlertDialog.Builder(this@ProfileActivity)
                builder1.setMessage(R.string.logout_success)
                builder1.setPositiveButton(R.string.dialog_ok) { dialogInterface: DialogInterface?, i1: Int -> finish() }
                builder1.setCancelable(false)
                builder1.show()
            }, Constant.DELAY_PROGRESS_DIALOG.toLong())
        }
        builder.setNegativeButton(R.string.dialog_cancel, null)
        builder.show()
    }

    private fun aboutDialog() {
        val layoutInflaterAndroid = LayoutInflater.from(this@ProfileActivity)
        val view = layoutInflaterAndroid.inflate(R.layout.custom_dialog_about, null)
        val alert = AlertDialog.Builder(this@ProfileActivity)
        alert.setView(view)
        alert.setCancelable(false)
        alert.setPositiveButton(R.string.dialog_ok) { dialog: DialogInterface, which: Int -> dialog.dismiss() }
        alert.show()
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(menuItem)
        }
    }

    public override fun onResume() {
        super.onResume()
        requestAction()
    }
}