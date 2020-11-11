package com.pocket.knowledge.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.pocket.knowledge.R
import com.pocket.knowledge.config.AppConfig
import com.pocket.knowledge.models.User
import com.pocket.knowledge.rests.RestAdapter.createAPI
import com.pocket.knowledge.utils.Constant
import com.pocket.knowledge.utils.ThemePref
import com.pocket.knowledge.utils.Tools.getTheme
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_edit_profile.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.IOException


class EditProfileActivity : AppCompatActivity() {

    private var myApplication: MyApplication? = null


    var bitmap: Bitmap? = null

    var progressDialog: ProgressDialog? = null
    private var strName: String? = null
    private var strEmail: String? = null
    private var strImage: String? = null
    private var strPassword: String? = null
    private var strNewImage: String? = null
    private var strOldImage: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getTheme(this)
        setContentView(R.layout.activity_edit_profile)
        setSupportActionBar(toolbar)
        val themePref = ThemePref(this)
        if (themePref.isDarkTheme!!) {
            toolbar.setBackgroundColor(resources.getColor(R.color.colorToolbarDark))
        } else {
            toolbar.setBackgroundColor(resources.getColor(R.color.colorPrimary))
        }
        val actionBar = supportActionBar
        if (actionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeButtonEnabled(true)
            supportActionBar!!.setTitle(R.string.title_menu_edit_profile)
        }
        val intent = intent
        strName = intent.getStringExtra("name")
        strEmail = intent.getStringExtra("email")
        strImage = intent.getStringExtra("user_image")
        strPassword = intent.getStringExtra("password")
        progressDialog = ProgressDialog(this@EditProfileActivity)
        progressDialog!!.setTitle(resources.getString(R.string.title_please_wait))
        progressDialog!!.setMessage(resources.getString(R.string.logout_process))
        progressDialog!!.setCancelable(false)
        myApplication = MyApplication.instance


        edt_user.setText(strName)
        edt_email.setText(strEmail)
        edt_password.setText(strPassword)
        displayProfileImage()
        btn_change_image.setOnClickListener { view: View? -> selectImage() }
        btn_update?.setOnClickListener { view: View? -> updateUserData() }
    }

    private fun displayProfileImage() {
        if (strImage == "") {
            profile_image!!.setImageResource(R.drawable.ic_user_account)
        } else {
            Picasso.get()
                    .load(AppConfig.ADMIN_PANEL_URL + "/upload/avatar/" + strImage!!.replace(" ", "%20"))
                    .resize(300, 300)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_account)
                    .into(profile_image)
        }
    }

    private fun selectImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, IMAGE)
    }

    private fun convertToString(): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val imgByte = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(imgByte, Base64.DEFAULT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            val path = data.data
            try {
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, path)
                tmp_image!!.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun updateUserData() {
        progressDialog = ProgressDialog(this@EditProfileActivity)
        progressDialog!!.setTitle(R.string.updating_profile)
        progressDialog!!.setMessage(resources.getString(R.string.waiting_message))
        progressDialog!!.setCancelable(false)
        progressDialog!!.show()
        strName = edt_user!!.text.toString()
        strEmail = edt_email!!.text.toString()
        strPassword = edt_password!!.text.toString()
        if (bitmap != null) {
            uploadImage()
        } else {
            updateData()
        }
    }

    private fun updateData() {
        val apiInterface = createAPI()
        val call = apiInterface.updateUserData(myApplication!!.userId, strName, strEmail, strPassword)
        call!!.enqueue(object : Callback<User?> {
            override fun onResponse(call: Call<User?>, response: Response<User?>) {
                Handler().postDelayed({
                    progressDialog!!.dismiss()
                    val builder = AlertDialog.Builder(this@EditProfileActivity)
                    builder.setMessage(R.string.success_updating_profile)
                    builder.setPositiveButton(resources.getString(R.string.dialog_ok)) { dialogInterface: DialogInterface?, i: Int -> finish() }
                    builder.setCancelable(false)
                    builder.show()
                }, Constant.DELAY_PROGRESS_DIALOG.toLong())
            }

            override fun onFailure(call: Call<User?>, t: Throwable) {
                progressDialog!!.dismiss()
            }
        })
    }

    private fun uploadImage() {
        strOldImage = strImage
        strNewImage = convertToString()
        val apiInterface = createAPI()
        val call = apiInterface.updatePhotoProfile(myApplication!!.userId, strName, strEmail, strPassword, strOldImage, strNewImage)
        call!!.enqueue(object : Callback<User?> {
            override fun onResponse(call: Call<User?>, response: Response<User?>) {
                Handler().postDelayed({
                    progressDialog!!.dismiss()
                    val builder = AlertDialog.Builder(this@EditProfileActivity)
                    builder.setMessage(R.string.success_updating_profile)
                    builder.setPositiveButton(resources.getString(R.string.dialog_ok)) { dialogInterface: DialogInterface?, i: Int -> finish() }
                    builder.setCancelable(false)
                    builder.show()
                }, Constant.DELAY_PROGRESS_DIALOG.toLong())
            }

            override fun onFailure(call: Call<User?>, t: Throwable) {
                progressDialog!!.dismiss()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(menuItem)
        }
    }

    public override fun onResume() {
        super.onResume()
    }

    companion object {
        private const val IMAGE = 100
    }
}