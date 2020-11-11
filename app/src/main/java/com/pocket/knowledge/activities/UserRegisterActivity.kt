package com.pocket.knowledge.activities

import android.app.ProgressDialog
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
import kotlinx.android.synthetic.main.activity_user_register.*
import org.json.JSONException
import org.json.JSONObject
class UserRegisterActivity : AppCompatActivity(), ValidationListener {
  /*  @Required(order = 1)
    @TextRule(order = 2, minLength = 3, maxLength = 35, trim = true, message = "Enter Valid Full Name")*/


   /* @Required(order = 3)
    @Email(order = 4, message = "Please Check and Enter a valid Email Address")*/


  /*  @Required(order = 5)
    @Password(order = 6, message = "Enter a Valid Password")
    @TextRule(order = 7, minLength = 6, message = "Enter a Password Correctly")*/

    private var validator: Validator? = null

    private var strFullName: String? = null
    private var strEmail: String? = null
    private var strPassword: String? = null
    var strMessage: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getTheme(this)
        setContentView(R.layout.activity_user_register)




        txt_terms.setOnClickListener { v: View? -> startActivity(Intent(applicationContext, PrivacyPolicyActivity::class.java)) }
        btn_login.setOnClickListener { v: View? ->
            finish()
            startActivity(Intent(applicationContext, UserLoginActivity::class.java))
        }
        btn_update.setOnClickListener { v: View? -> validator!!.validateAsync() }
        validator = Validator(this)
        validator!!.validationListener = this
    }

    override fun onValidationSucceeded() {
        try {
            strFullName = edt_user.text.toString().replace(" ", "%20")
            strEmail = edt_email!!.text.toString()
            strPassword = edt_password!!.text.toString()
            if (isNetworkAvailable(this@UserRegisterActivity)) {
                MyTaskRegister().execute(Constant.REGISTER_URL + strFullName + "&email=" + strEmail + "&password=" + encrypt(strPassword!!))
            } else {
                val dialog = AlertDialog.Builder(this)
                dialog.setTitle(R.string.whops)
                dialog.setMessage(R.string.msg_no_network)
                dialog.setPositiveButton(R.string.dialog_ok, null)
                dialog.setCancelable(false)
                dialog.show()
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

    private inner class MyTaskRegister : AsyncTask<String?, Void?, String?>() {
        var progressDialog: ProgressDialog? = null
        override fun onPreExecute() {
            super.onPreExecute()
            progressDialog = ProgressDialog(this@UserRegisterActivity)
            progressDialog!!.setTitle(resources.getString(R.string.title_please_wait))
            progressDialog!!.setMessage(resources.getString(R.string.register_process))
            progressDialog!!.setCancelable(false)
            progressDialog!!.show()
        }

         override fun doInBackground(vararg params: String?): String? {
            return getJSONString(params[0])
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (null == result || result.isEmpty()) {
                showToast("No Data Found!!!")
            } else {
                try {
                    val mainJson = JSONObject(result)
                    val jsonArray = mainJson.getJSONArray(Constant.CATEGORY_ARRAY_NAME)
                    var objJson: JSONObject? = null
                    for (i in 0 until jsonArray.length()) {
                        objJson = jsonArray.getJSONObject(i)
                        strMessage = objJson.getString(Constant.MSG)
                        Constant.GET_SUCCESS_MSG = objJson.getInt(Constant.SUCCESS)
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
        if (Constant.GET_SUCCESS_MSG == 0) {
            val dialog = AlertDialog.Builder(this@UserRegisterActivity)
            dialog.setTitle(R.string.whops)
            dialog.setMessage(R.string.register_exist)
            dialog.setPositiveButton(R.string.dialog_ok, null)
            dialog.setCancelable(false)
            dialog.show()
            edt_email!!.setText("")
            edt_email!!.requestFocus()
        } else {
            val dialog = AlertDialog.Builder(this@UserRegisterActivity)
            dialog.setTitle(R.string.register_title)
            dialog.setMessage(R.string.register_success)
            dialog.setPositiveButton(R.string.dialog_ok) { dialogInterface, i ->
                val intent = Intent(applicationContext, UserLoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
            dialog.setCancelable(false)
            dialog.show()
        }
    }

    fun showToast(msg: String?) {
        Toast.makeText(this@UserRegisterActivity, msg, Toast.LENGTH_LONG).show()
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            android.R.id.home -> onBackPressed()
            else -> return super.onOptionsItemSelected(menuItem)
        }
        return true
    }

    companion object {
        private val TAG = UserRegisterActivity::class.java.name
    }
}