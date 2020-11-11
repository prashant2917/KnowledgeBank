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
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.DexterError
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.ortiz.touchview.TouchImageView
import com.pocket.knowledge.R
import com.pocket.knowledge.config.AppConfig
import com.pocket.knowledge.utils.ThemePref
import com.pocket.knowledge.utils.Tools.getTheme

import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_full_screen_image.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class WebViewImageActivity : AppCompatActivity() {
    private var newsImage: TouchImageView? = null
    var strImage: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getTheme(this)
        setContentView(R.layout.activity_full_screen_image)
        strImage = intent.getStringExtra("image_url")
        newsImage = findViewById(R.id.image)
        Picasso.get()
                .load(strImage?.replace(" ", "%20"))
                .placeholder(R.drawable.ic_thumbnail)
                .into(newsImage)
        initToolbar()
    }

    private fun initToolbar() {

        setSupportActionBar(toolbar)
        val themePref = ThemePref(this)
        if (themePref.isDarkTheme!!) {
            toolbar.setBackgroundColor(resources.getColor(R.color.colorToolbarDark))
        } else {
            toolbar.setBackgroundColor(resources.getColor(R.color.colorPrimary))
        }
        val actionBar = supportActionBar
        if (actionBar != null) {
            supportActionBar!!.title = ""
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_image, menu)
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.close_image -> {
                Handler().postDelayed({ finish() }, 300)
                true
            }
            R.id.save_image -> {
                Handler().postDelayed({ requestStoragePermission() }, 300)
                true
            }
            else -> super.onOptionsItemSelected(menuItem)
        }
    }

    @TargetApi(16)
    private fun requestStoragePermission() {
        Dexter.withActivity(this@WebViewImageActivity)
                .withPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            SaveImage(this@WebViewImageActivity).execute(AppConfig.ADMIN_PANEL_URL + "/upload/" + strImage)
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
        val builder = AlertDialog.Builder(this@WebViewImageActivity)
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
                MediaScannerConnection.scanFile(this@WebViewImageActivity, `as`, null) { s1: String?, uri: Uri? -> }
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
}