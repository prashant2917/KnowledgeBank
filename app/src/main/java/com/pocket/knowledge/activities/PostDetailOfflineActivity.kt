package com.pocket.knowledge.activities

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import com.pocket.knowledge.R
import com.pocket.knowledge.config.AppConfig
import com.pocket.knowledge.models.News
import com.pocket.knowledge.utils.AppBarLayoutBehavior
import com.pocket.knowledge.utils.Constant
import com.pocket.knowledge.utils.DbHandler
import com.pocket.knowledge.utils.ThemePref
import com.pocket.knowledge.utils.Tools.getFormatedDate
import com.pocket.knowledge.utils.Tools.getTheme
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_post_detail.*
import kotlinx.android.synthetic.main.include_post_detail.*

class PostDetailOfflineActivity : AppCompatActivity() {
    private var post: News? = null

    private var menu: Menu? = null

    private var databaseHandler: DbHandler? = null
    private var bgParagraph: String? = null
    var themePref: ThemePref? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getTheme(this)
        setContentView(R.layout.activity_post_detail_offline)
        themePref = ThemePref(this)
        databaseHandler = DbHandler(applicationContext)
        val appBarLayout = findViewById<AppBarLayout>(R.id.appBarLayout)
        (appBarLayout.layoutParams as CoordinatorLayout.LayoutParams).behavior = AppBarLayoutBehavior()

        btn_comment.setOnClickListener { v: View? ->
            val intent = Intent(applicationContext, CommentsActivity::class.java)
            intent.putExtra("nid", post!!.nid)
            intent.putExtra("count", post!!.comments_count)
            startActivity(intent)
        }
        txt_comment_text.setOnClickListener { v: View? ->
            val intent = Intent(applicationContext, CommentsActivity::class.java)
            intent.putExtra("nid", post!!.nid)
            intent.putExtra("count", post!!.comments_count)
            startActivity(intent)
        }

        // get extra object
        post = intent.getSerializableExtra(Constant.EXTRA_OBJC) as News
        initToolbar()
        displayData()
    }

    private fun initToolbar() {
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
            supportActionBar!!.title = post!!.category_name
        }
    }

    private fun displayData() {
        txt_title!!.text = Html.fromHtml(post!!.news_title)
        txt_comment_count!!.text = "" + post!!.comments_count
        Handler().postDelayed({
            if (post!!.comments_count == 0L) {
                txt_comment_text!!.setText(R.string.txt_no_comment)
            }
            if (post!!.comments_count == 1L) {
                txt_comment_text!!.text = resources.getString(R.string.txt_read) + " " + post!!.comments_count + " " + resources.getString(R.string.txt_comment)
            } else if (post!!.comments_count > 1) {
                txt_comment_text!!.text = resources.getString(R.string.txt_read) + " " + post!!.comments_count + " " + resources.getString(R.string.txt_comments)
            }
        }, 1000)

        //news_description.setBackgroundColor(Color.parseColor("#ffffff"));
        news_description!!.setBackgroundColor(Color.TRANSPARENT)
        news_description!!.settings.defaultTextEncodingName = "UTF-8"
        news_description!!.isFocusableInTouchMode = false
        news_description!!.isFocusable = false
        news_description!!.settings.javaScriptEnabled = true
        val webSettings = news_description!!.settings
        val res = resources
        val fontSize = res.getInteger(R.integer.font_size)
        webSettings.defaultFontSize = fontSize
        val mimeType = "text/html; charset=UTF-8"
        val encoding = "utf-8"
        val htmlText = post!!.news_description
        bgParagraph = if (themePref!!.isDarkTheme!!) {
            //news_description.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBackgroundDark));
            "<style type=\"text/css\">body{color: #eeeeee;}"
        } else {
            "<style type=\"text/css\">body{color: #000000;}"
        }
        val text = ("<html><head>"
                + "<style>img{max-width:100%;height:auto;} figure{max-width:100%;height:auto;} iframe{width:100%;}</style> "
                + bgParagraph
                + "</style></head>"
                + "<body>"
                + htmlText
                + "</body></html>")
        val textRtl = ("<html dir='rtl'><head>"
                + "<style>img{max-width:100%;height:auto;} figure{max-width:100%;height:auto;} iframe{width:100%;}</style> "
                + bgParagraph
                + "</style></head>"
                + "<body>"
                + htmlText
                + "</body></html>")
        news_description!!.loadDataWithBaseURL(null, text, mimeType, encoding, null)
        news_description!!.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (url.startsWith("http://")) {
                    val intent = Intent(applicationContext, WebViewActivity::class.java)
                    intent.putExtra("url", url)
                    startActivity(intent)
                }
                if (url.startsWith("https://")) {
                    val intent = Intent(applicationContext, WebViewActivity::class.java)
                    intent.putExtra("url", url)
                    startActivity(intent)
                }
                if (url.endsWith(".jpg") || url.endsWith(".jpeg") || url.endsWith(".png") || url.endsWith(".JPG") || url.endsWith(".JPEG")) {
                    val intent = Intent(applicationContext, WebViewImageActivity::class.java)
                    intent.putExtra("image_url", url)
                    startActivity(intent)
                }
                if (url.endsWith(".pdf")) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(url)
                    startActivity(intent)
                }
                return true
            }
        }
        txt_category!!.text = post!!.category_name
        txt_category!!.setBackgroundColor(ContextCompat.getColor(this, R.color.colorCategory))
        txt_date!!.visibility = View.VISIBLE
        txt_date!!.text = getFormatedDate(post!!.news_date)
        val newsImage = findViewById<ImageView>(R.id.image)
        if (post!!.content_type != null && post!!.content_type == "youtube") {
            Picasso.get()
                    .load(Constant.YOUTUBE_IMG_FRONT + post!!.video_id + Constant.YOUTUBE_IMG_BACK)
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(newsImage)
            newsImage.setOnClickListener { v: View? ->
                val intent = Intent(applicationContext, YoutubePlayerActivity::class.java)
                intent.putExtra("video_id", post!!.video_id)
                startActivity(intent)
            }
        } else if (post!!.content_type != null && post!!.content_type == "Url") {
            Picasso.get()
                    .load(AppConfig.ADMIN_PANEL_URL + "/upload/" + post!!.news_image.replace(" ", "%20"))
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(newsImage)
            newsImage.setOnClickListener { v: View? ->
                val intent = Intent(applicationContext, VideoPlayerActivity::class.java)
                intent.putExtra("video_url", post!!.video_url)
                startActivity(intent)
            }
        } else if (post!!.content_type != null && post!!.content_type == "Upload") {
            Picasso.get()
                    .load(AppConfig.ADMIN_PANEL_URL + "/upload/" + post!!.news_image.replace(" ", "%20"))
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(newsImage)
            newsImage.setOnClickListener { v: View? ->
                val intent = Intent(applicationContext, VideoPlayerActivity::class.java)
                intent.putExtra("video_url", AppConfig.ADMIN_PANEL_URL + "/upload/video/" + post!!.video_url)
                startActivity(intent)
            }
        } else {
            Picasso.get()
                    .load(AppConfig.ADMIN_PANEL_URL + "/upload/" + post!!.news_image.replace(" ", "%20"))
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(newsImage)
            newsImage.setOnClickListener { view: View? ->
                val intent = Intent(applicationContext, FullScreenImageActivity::class.java)
                intent.putExtra("image", post!!.news_image)
                startActivity(intent)
            }
        }
        if (post!!.content_type != "Post") {
            thumbnail_video!!.visibility = View.VISIBLE
        } else {
            thumbnail_video!!.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_news_detail, menu)
        this.menu = menu
        addToFavorite()
        return true
    }

    private fun addToFavorite() {
        val data = databaseHandler!!.getFavRow(post!!.nid)
        if (data.isEmpty()) {
            menu!!.getItem(0).icon = ContextCompat.getDrawable(applicationContext, R.drawable.ic_favorite_outline_white)
        } else {
            if (data[0].nid == post!!.nid) {
                menu!!.getItem(0).icon = ContextCompat.getDrawable(applicationContext, R.drawable.ic_favorite_white)
            }
        }
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.action_later -> {
                val data = databaseHandler!!.getFavRow(post!!.nid)
                if (data.isEmpty()) {
                    databaseHandler!!.addToFavorite(News(
                            post!!.nid,
                            post!!.news_title,
                            post!!.category_name,
                            post!!.news_date,
                            post!!.news_image,
                            post!!.news_description,
                            post!!.content_type,
                            post!!.video_url,
                            post!!.video_id,
                            post!!.comments_count
                    ))
                    Snackbar.make(coordinatorLayout, R.string.favorite_added, Snackbar.LENGTH_SHORT).show()
                    menu!!.getItem(0).icon = ContextCompat.getDrawable(applicationContext, R.drawable.ic_favorite_white)
                } else {
                    if (data[0].nid == post!!.nid) {
                        databaseHandler!!.removeFav(News(post!!.nid))
                        Snackbar.make(coordinatorLayout, R.string.favorite_removed, Snackbar.LENGTH_SHORT).show()
                        menu!!.getItem(0).icon = ContextCompat.getDrawable(applicationContext, R.drawable.ic_favorite_outline_white)
                    }
                }
            }
            R.id.action_share -> {
                val formattedString = Html.fromHtml(post!!.news_description).toString()
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(Intent.EXTRA_TEXT, """
     ${post!!.news_title}
     $formattedString
     ${resources.getString(R.string.share_content)}https://play.google.com/store/apps/details?id=$packageName
     """.trimIndent())
                sendIntent.type = "text/plain"
                startActivity(sendIntent)
            }
            else -> return super.onOptionsItemSelected(menuItem)
        }
        return true
    }
}