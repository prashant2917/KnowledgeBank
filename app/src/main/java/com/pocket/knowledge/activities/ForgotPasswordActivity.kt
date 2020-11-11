package com.pocket.knowledge.activities

import android.content.DialogInterface
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.pocket.knowledge.R
import com.pocket.knowledge.activities.ForgotPasswordActivity
import com.pocket.knowledge.utils.Constant
import com.pocket.knowledge.utils.NetworkCheck.Companion.getJSONString
import com.pocket.knowledge.utils.NetworkCheck.Companion.isNetworkAvailable
import com.pocket.knowledge.utils.Tools.getTheme
import id.solodroid.validationlibrary.Rule
import id.solodroid.validationlibrary.Validator
import id.solodroid.validationlibrary.Validator.ValidationListener
import id.solodroid.validationlibrary.annotation.Email
import id.solodroid.validationlibrary.annotation.Required
import kotlinx.android.synthetic.main.activity_user_forgot.*
import org.json.JSONException
import org.json.JSONObject

class ForgotPasswordActivity : AppCompatActivity(), ValidationListener {
    @Required(order = 1)
    @Email(order = 2, message = "Please Check and Enter a valid Email Address")

    var strEmail: String? = null
    var strMessage: String? = null
    private var validator: Validator? = null

    var progressBar: ProgressBar? = null
    var layout: LinearLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getTheme(this)
        setContentView(R.layout.activity_user_forgot)

        layout = findViewById(R.id.view)
        btnForgot!!.setOnClickListener { v: View? -> validator!!.validateAsync() }
        validator = Validator(this)
        validator!!.validationListener = this
    }

    override fun onValidationSucceeded() {
        strEmail = etUserName!!.text.toString()
        if (isNetworkAvailable(this@ForgotPasswordActivity)) {
            MyTaskForgot().execute(Constant.FORGET_PASSWORD_URL + strEmail)
        } else {
            Toast.makeText(applicationContext, resources.getString(R.string.msg_no_network), Toast.LENGTH_SHORT).show()
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

    private inner class MyTaskForgot : AsyncTask<String?, Void?, String?>() {
        override fun onPreExecute() {
            super.onPreExecute()
            progressBar!!.visibility = View.VISIBLE
            layout!!.visibility = View.INVISIBLE
        }

         override fun doInBackground(vararg params: String?): String? {
            return getJSONString(params[0])
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (null == result || result.isEmpty()) {
                Toast.makeText(applicationContext, resources.getString(R.string.msg_no_network), Toast.LENGTH_SHORT).show()
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
                    progressBar!!.visibility = View.GONE
                    setResult()
                }, Constant.DELAY_PROGRESS_DIALOG.toLong())
            }
        }


    }

    fun setResult() {
        if (Constant.GET_SUCCESS_MSG == 0) {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle(R.string.whops)
            dialog.setMessage(R.string.forgot_failed_message)
            dialog.setPositiveButton(R.string.dialog_ok, null)
            dialog.setCancelable(false)
            dialog.show()
            layout!!.visibility = View.VISIBLE
            etUserName!!.setText("")
            etUserName!!.requestFocus()
        } else {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle(R.string.dialog_success)
            dialog.setMessage(R.string.forgot_success_message)
            dialog.setPositiveButton(R.string.dialog_ok) { dialogInterface: DialogInterface?, i: Int ->
                val intent = Intent(this@ForgotPasswordActivity, UserLoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
            dialog.setCancelable(false)
            dialog.show()
        }
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            android.R.id.home -> onBackPressed()
            else -> return super.onOptionsItemSelected(menuItem)
        }
        return true
    }
}