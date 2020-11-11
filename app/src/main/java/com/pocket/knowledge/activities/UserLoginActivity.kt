package com.pocket.knowledge.activities

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.pocket.knowledge.R
import com.pocket.knowledge.utils.Base64Helper.encrypt
import com.pocket.knowledge.utils.Constant
import com.pocket.knowledge.utils.NetworkCheck.Companion.getJSONString
import com.pocket.knowledge.utils.NetworkCheck.Companion.isNetworkAvailable
import com.pocket.knowledge.utils.Tools.getTheme
import id.solodroid.validationlibrary.Rule
import id.solodroid.validationlibrary.Validator
import id.solodroid.validationlibrary.Validator.ValidationListener
import kotlinx.android.synthetic.main.activity_user_login.*
import org.json.JSONException
import org.json.JSONObject

class UserLoginActivity : AppCompatActivity(), ValidationListener {
    /*@Required(order = 1)
    @Email(order = 2, message = "Please Check and Enter a valid Email Address"*)*/
    private var strEmail: String? = null
   /* @Required(order = 3)
    @Password(order = 4, message = "Enter a Valid Password")
    @TextRule(order = 5, minLength = 6, message = "Enter a Password Correctly")*/
    private var strPassword: String? = null
    var strMessage: String? = null
    var strName: String? = null
    var strPassengerId: String? = null






    private var validator: Validator? = null
    private var myApplication: MyApplication? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getTheme(this)
        setContentView(R.layout.activity_user_login)
        myApplication = MyApplication.instance

        btn_update.setOnClickListener { v: View? ->
            validator!!.validateAsync()
            myApplication!!.saveType("normal")
        }
        txt_forgot.setOnClickListener { v: View? ->
            val intent = Intent(this@UserLoginActivity, ForgotPasswordActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
        btn_create.setOnClickListener { v: View? ->
            startActivity(Intent(applicationContext, UserRegisterActivity::class.java))
            finish()
        }
        validator = Validator(this)
        validator!!.validationListener = this
    }

    override fun onValidationSucceeded() {
        try {
            strEmail = edt_email.text.toString()
            strPassword = edt_password.text.toString()
            if (isNetworkAvailable(this@UserLoginActivity)) {
                MyTaskLoginNormal().execute(Constant.NORMAL_LOGIN_URL + strEmail + "&password=" + encrypt(strPassword!!))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, "Exception ")
        }
    }

    override fun onValidationFailed(failedView: View, failedRule: Rule<*>) {
        val message = failedRule.failureMessage
        if (failedView is EditText) {
            failedView.requestFocus()
            failedView.error = message
        } else {
            Toast.makeText(this, "Record Not Saved", Toast.LENGTH_SHORT).show()
        }
    }

    private inner class MyTaskLoginNormal : AsyncTask<String?, Void?, String?>() {
        var progressDialog: ProgressDialog? = null
        override fun onPreExecute() {
            super.onPreExecute()
            progressDialog = ProgressDialog(this@UserLoginActivity)
            progressDialog!!.setTitle(resources.getString(R.string.title_please_wait))
            progressDialog!!.setMessage(resources.getString(R.string.login_process))
            progressDialog!!.setCancelable(false)
            progressDialog!!.show()
        }

         override fun doInBackground(vararg params: String?): String? {
            return getJSONString(params[0])
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (result!=null || result?.length == 0) {

                try {
                    val mainJson = JSONObject(result)
                    val jsonArray = mainJson.getJSONArray(Constant.CATEGORY_ARRAY_NAME)
                    var objJson: JSONObject? = null
                    for (i in 0 until jsonArray.length()) {
                        objJson = jsonArray.getJSONObject(i)
                        if (objJson.has(Constant.MSG)) {
                            strMessage = objJson.getString(Constant.MSG)
                            Constant.GET_SUCCESS_MSG = objJson.getInt(Constant.SUCCESS)
                        } else {
                            Constant.GET_SUCCESS_MSG = objJson.getInt(Constant.SUCCESS)
                            strName = objJson.getString(Constant.USER_NAME)
                            strPassengerId = objJson.getString(Constant.USER_ID)
                            //strImage = objJson.getString("normal");
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                Handler().postDelayed({
                    if (null != progressDialog && progressDialog!!.isShowing) {
                        progressDialog!!.dismiss()
                    }
                    setResult()
                }, Constant.DELAY_PROGRESS_DIALOG.toLong())
            }
        }
    }

    fun setResult() {
        when(Constant.GET_SUCCESS_MSG){
            0->{
                val dialog = AlertDialog.Builder(this)
                dialog.setTitle(R.string.whops)
                dialog.setMessage(R.string.login_failed)
                dialog.setPositiveButton(R.string.dialog_ok, null)
                dialog.setCancelable(false)
                dialog.show()
            }
            2->{
                val dialog = AlertDialog.Builder(this)
                dialog.setTitle(R.string.whops)
                dialog.setMessage(R.string.login_disabled)
                dialog.setPositiveButton(R.string.dialog_ok, null)
                dialog.setCancelable(false)
                dialog.show()

            }
            else->{
                myApplication!!.saveIsLogin(true)
                myApplication!!.saveLogin(strPassengerId, strName, strEmail)
                val dialog = AlertDialog.Builder(this)
                dialog.setTitle(R.string.login_title)
                dialog.setMessage(R.string.login_success)
                dialog.setPositiveButton(R.string.dialog_ok) { dialogInterface: DialogInterface?, i: Int -> finish() }
                dialog.setCancelable(false)
                dialog.show()
            }
        }

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

    companion object {
        private val TAG = UserLoginActivity::class.java.name
    }
}