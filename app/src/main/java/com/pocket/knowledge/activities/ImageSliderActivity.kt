package com.pocket.knowledge.activities

import android.Manifest
import android.annotation.TargetApi
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.DexterError
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.pocket.knowledge.R
import com.pocket.knowledge.adapter.AdapterImageSlider
import com.pocket.knowledge.callbacks.PostDetailCallback
import com.pocket.knowledge.config.AppConfig
import com.pocket.knowledge.models.Images
import com.pocket.knowledge.rests.RestAdapter.createAPI
import com.pocket.knowledge.utils.NetworkCheck.Companion.isConnect
import com.pocket.knowledge.utils.Tools.getTheme
import kotlinx.android.synthetic.main.activity_image_slider.*
import kotlinx.android.synthetic.main.include_no_network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class ImageSliderActivity : AppCompatActivity() {
    private var callbackCall: Call<PostDetailCallback?>? = null

    var nid: Long? = null
    var position = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getTheme(this)
        setContentView(R.layout.activity_image_slider)

        nid = intent.getLongExtra("nid", 0)
        position = intent.getIntExtra("position", 0)
        //post = (News) getIntent().getSerializableExtra(EXTRA_OBJC);
        requestAction()
        initToolbar()
    }

    private fun requestAction() {
        showFailedView(false, "")
        requestPostData()
    }

    private fun requestPostData() {
        callbackCall = createAPI().getNewsDetail(nid!!)
        callbackCall!!.enqueue(object : Callback<PostDetailCallback?> {
            override fun onResponse(call: Call<PostDetailCallback?>, response: Response<PostDetailCallback?>) {
                val responseHome = response.body()
                if (responseHome == null || responseHome.status != "ok") {
                    onFailRequest()
                    return
                }
                displayAllData(responseHome)
            }

            override fun onFailure(call: Call<PostDetailCallback?>, th: Throwable) {
                Log.e("onFailure", th.message)
                if (!call.isCanceled) {
                    onFailRequest()
                }
            }
        })
    }

    private fun onFailRequest() {
        if (isConnect(this@ImageSliderActivity)) {
            showFailedView(true, getString(R.string.msg_no_network))
        } else {
            showFailedView(true, getString(R.string.msg_offline))
        }
    }

    private fun showFailedView(show: Boolean, message: String) {

        failed_message.text = message
        if (show) {
            lyt_failed_home.visibility = View.VISIBLE
        } else {
            lyt_failed_home.visibility = View.GONE
        }
        findViewById<View>(R.id.failed_retry).setOnClickListener { view: View? -> requestAction() }
    }

    private fun displayAllData(responseHome: PostDetailCallback) {
        displayImages(responseHome.images)
    }

    private fun displayImages(list: List<Images>) {

        val adapter = AdapterImageSlider(this@ImageSliderActivity, list)
        view_pager_image.adapter = adapter
        view_pager_image.offscreenPageLimit = 4
        view_pager_image.currentItem = position
        view_pager_image.addOnPageChangeListener(object : SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                txt_number!!.text = (position + 1).toString() + " of " + list.size
                lyt_save!!.setOnClickListener { view: View? -> requestStoragePermission(list, position) }
            }
        })
        txt_number!!.text = (position + 1).toString() + " of " + list.size
        lyt_save!!.setOnClickListener { view: View? -> requestStoragePermission(list, position) }
        lyt_close!!.setOnClickListener { view: View? -> finish() }
    }

    private fun initToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            supportActionBar!!.title = ""
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    @TargetApi(16)
    private fun requestStoragePermission(list: List<Images>, position: Int) {
        Dexter.withActivity(this@ImageSliderActivity)
                .withPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            SaveImage(this@ImageSliderActivity).execute(AppConfig.ADMIN_PANEL_URL + "/upload/" + list[position].image_name)
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied) {
                            // show alert dialog navigating to Setting
                            showSettingsDialog()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken) {
                        token.continuePermissionRequest()
                    }
                }).withErrorListener { error: DexterError -> Toast.makeText(applicationContext, "Error occurred! $error", Toast.LENGTH_SHORT).show() }
                .onSameThread()
                .check()
    }

    private fun showSettingsDialog() {
        val builder = AlertDialog.Builder(this@ImageSliderActivity)
        builder.setTitle(R.string.permission_msg)
        builder.setMessage(R.string.permission_upload)
        builder.setPositiveButton(R.string.dialog_settings) { dialog: DialogInterface, which: Int ->
            dialog.cancel()
            openSettings()
        }
        builder.setNegativeButton(R.string.dialog_cancel) { dialog: DialogInterface, which: Int -> dialog.cancel() }
        builder.show()
    }

    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivityForResult(intent, 101)
    }

    inner class SaveImage(private val context: Context) : AsyncTask<String?, String?, String?>() {
        private var pDialog: ProgressDialog? = null
        private var myFileUrl: URL? = null
        private var bmImg: Bitmap? = null
        private var file: File? = null
        override fun onPreExecute() {
            super.onPreExecute()
            pDialog = ProgressDialog(context)
            pDialog!!.setMessage(resources.getString(R.string.download_msg))
            pDialog!!.isIndeterminate = false
            pDialog!!.setCancelable(false)
            pDialog!!.show()
        }

         override fun doInBackground(vararg args: String?): String? {
            var `as`: Array<String?>? = null
            try {
                myFileUrl = URL(args[0])
                val conn = myFileUrl!!.openConnection() as HttpURLConnection
                conn.doInput = true
                conn.connect()
                val `is` = conn.inputStream
                bmImg = BitmapFactory.decodeStream(`is`)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            try {
                val path = myFileUrl!!.path
                val idStr = path.substring(path.lastIndexOf('/') + 1)
                val filepath = Environment.getExternalStorageDirectory()
                val dir = File(filepath.absolutePath + "/" + resources.getString(R.string.app_name) + "/")
                dir.mkdirs()
                val fileName = "Image__$idStr"
                file = File(dir, fileName)
                val fos = FileOutputStream(file)
                bmImg!!.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.flush()
                fos.close()
                `as` = arrayOfNulls(1)
                `as`[0] = file.toString()
                MediaScannerConnection.scanFile(this@ImageSliderActivity, `as`, null) { s1: String?, uri: Uri? -> }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(args: String?) {
            Handler().postDelayed({
                Toast.makeText(applicationContext, R.string.download_success, Toast.LENGTH_SHORT).show()
                pDialog!!.dismiss()
            }, 3000)
        }

    }

    public override fun onDestroy() {
        if (!(callbackCall == null || callbackCall!!.isCanceled)) {
            callbackCall!!.cancel()
        }
        super.onDestroy()
    }
}