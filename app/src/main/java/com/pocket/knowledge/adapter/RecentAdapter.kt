package com.pocket.knowledge.adapter


import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.pocket.knowledge.R
import com.pocket.knowledge.config.AdsConfig
import com.pocket.knowledge.config.AppConfig
import com.pocket.knowledge.config.UiConfig
import com.pocket.knowledge.models.News
import com.pocket.knowledge.utils.Constant
import com.pocket.knowledge.utils.NativeTemplateStyle
import com.pocket.knowledge.utils.TemplateView
import com.pocket.knowledge.utils.ThemePref
import com.pocket.knowledge.utils.Tools.getAdRequest
import com.pocket.knowledge.utils.Tools.getFormatedDateSimple
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.lsv_item_heading.view.*
import kotlinx.android.synthetic.main.lsv_item_load_more.view.*
import kotlinx.android.synthetic.main.lsv_item_recent.view.*
import java.util.*

class RecentAdapter(private val context: Context, view: RecyclerView, private val items: ArrayList<News>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val viewProg = 0
    private val viewHead = 1
    private val viewItem = 2
    private var loading = false
    private var onLoadMoreListener: OnLoadMoreListener? = null
    private var mOnItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(view: View?, obj: News?, position: Int)
    }

    fun setOnItemClickListener(mItemClickListener: OnItemClickListener?) {
        mOnItemClickListener = mItemClickListener
    }

    inner class HeadingViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        // each data item is just a string in this case
        var title: TextView = v.heading_txt_title
        var icDate: ImageView = v.heading_ic_date
        var date: TextView = v.heading_txt_date
        var category: TextView = v.heading_category_name
        var comment: TextView = v.heading_comment
        var image: ImageView = v.heading_image
        var thumbnailVideo: ImageView = v.heading_thumbnail_video
        var lytParent: RelativeLayout =v.heading_lyt_parent

    }

    inner class OriginalViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        // each data item is just a string in this case
        var title: TextView = v.txt_title
        var icDate: ImageView = v.ic_date
        var date: TextView = v.txt_date
        var category: TextView = v.category_name
        var comment: TextView = v.comment
        var image: ImageView = v.image
        var thumbnailVideo: ImageView = v.thumbnail_video
        var lytParent: LinearLayout = v.recent_lyt_parent
        var nativeTemplate: TemplateView = v.recent_native_template
        fun bindNativeAdView() {
            val adLoader = AdLoader.Builder(context, context.getString(R.string.admob_native_unit_id))
                    .forUnifiedNativeAd { unifiedNativeAd: UnifiedNativeAd? ->
                        val themePref = ThemePref(context)
                        if (themePref.isDarkTheme!!) {
                            val colorDrawable = ColorDrawable(ContextCompat.getColor(context, R.color.colorBackgroundDark))
                            val styles = NativeTemplateStyle.Builder().withMainBackgroundColor(colorDrawable).build()
                            nativeTemplate.setStyles(styles)
                        } else {
                            val colorDrawable = ColorDrawable(ContextCompat.getColor(context, R.color.colorBackgroundLight))
                            val styles = NativeTemplateStyle.Builder().withMainBackgroundColor(colorDrawable).build()
                            nativeTemplate.setStyles(styles)
                        }
                        nativeTemplate.setNativeAd(unifiedNativeAd!!)
                    }.withAdListener(object : AdListener() {
                        override fun onAdLoaded() {
                            super.onAdLoaded()
                            if (adapterPosition % AdsConfig.NATIVE_AD_NEWS_FEED_INTERVAL == AdsConfig.NATIVE_AD_NEWS_FEED_INDEX) {
                                nativeTemplate.visibility = View.VISIBLE
                            } else {
                                nativeTemplate.visibility = View.GONE
                            }
                        }

                        override fun onAdFailedToLoad(errorCode: Int) {
                            nativeTemplate.visibility = View.GONE
                        }
                    })
                    .build()
            adLoader.loadAd(getAdRequest(context as Activity))
        }

    }

    class ProgressViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var progressBar: ProgressBar = v.progressBar1

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            viewHead -> {
                val headingItemView = LayoutInflater.from(parent.context).inflate(R.layout.lsv_item_heading, parent, false)
                HeadingViewHolder(headingItemView)
            }
            viewItem -> {
                val menuItemView = LayoutInflater.from(parent.context).inflate(R.layout.lsv_item_recent, parent, false)
                OriginalViewHolder(menuItemView)
            }
            viewProg -> {
                val loadMoreView = LayoutInflater.from(parent.context).inflate(R.layout.lsv_item_load_more, parent, false)
                ProgressViewHolder(loadMoreView)
            }
            else -> {
                val loadMoreView = LayoutInflater.from(parent.context).inflate(R.layout.lsv_item_load_more, parent, false)
                ProgressViewHolder(loadMoreView)
            }
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            viewHead -> {
                val p = items[position]
                val vItem = holder as HeadingViewHolder
                vItem.title.text = Html.fromHtml(p!!.news_title)
                vItem.date.visibility = View.VISIBLE
                vItem.icDate.visibility = View.VISIBLE
                vItem.date.text = getFormatedDateSimple(p.news_date)
                vItem.category.text = Html.fromHtml(p.news_description)
                vItem.comment.text = p.comments_count.toString() + ""
                if (p.content_type != null && p.content_type == "Post") {
                    vItem.thumbnailVideo.visibility = View.GONE
                } else {
                    vItem.thumbnailVideo.visibility = View.VISIBLE
                }
                if (p.content_type != null && p.content_type == "youtube") {
                    Picasso.get()
                            .load(Constant.YOUTUBE_IMG_FRONT + p.video_id + Constant.YOUTUBE_IMG_BACK)
                            .placeholder(R.drawable.ic_thumbnail)
                            .into(vItem.image)
                } else {
                    Picasso.get()
                            .load(AppConfig.ADMIN_PANEL_URL + "/upload/" + p.news_image.replace(" ", "%20"))
                            .placeholder(R.drawable.ic_thumbnail)
                            .into(vItem.image)
                }
                vItem.lytParent.setOnClickListener { view: View? ->
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener!!.onItemClick(view, p, position)
                    }
                }
            }
            viewItem -> {
                val newsItem = items[position] as News?
                val itemHolder = holder as OriginalViewHolder
                if (holder.getAdapterPosition() % AdsConfig.NATIVE_AD_NEWS_FEED_INTERVAL == AdsConfig.NATIVE_AD_NEWS_FEED_INDEX) {
                    itemHolder.bindNativeAdView()
                } else {
                    itemHolder.nativeTemplate.visibility = View.GONE
                }
                itemHolder.title.text = Html.fromHtml(newsItem!!.news_title)
                itemHolder.date.visibility = View.VISIBLE
                itemHolder.icDate.visibility = View.VISIBLE
                itemHolder.date.text = getFormatedDateSimple(newsItem.news_date)
                itemHolder.category.text = Html.fromHtml(newsItem.news_description)
                itemHolder.comment.text = newsItem.comments_count.toString() + ""
                if (newsItem.content_type != null && newsItem.content_type == "Post") {
                    itemHolder.thumbnailVideo.visibility = View.GONE
                } else {
                    itemHolder.thumbnailVideo.visibility = View.VISIBLE
                }
                if (newsItem.content_type != null && newsItem.content_type == "youtube") {
                    Picasso.get()
                            .load(Constant.YOUTUBE_IMG_FRONT + newsItem.video_id + Constant.YOUTUBE_IMG_BACK)
                            .placeholder(R.drawable.ic_thumbnail)
                            .into(itemHolder.image)
                } else {
                    Picasso.get()
                            .load(AppConfig.ADMIN_PANEL_URL + "/upload/" + newsItem.news_image.replace(" ", "%20"))
                            .placeholder(R.drawable.ic_thumbnail)
                            .into(itemHolder.image)
                }
                itemHolder.lytParent.setOnClickListener { view: View? ->
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener!!.onItemClick(view, newsItem, position)
                    }
                }
            }
            viewProg -> (holder as ProgressViewHolder).progressBar.isIndeterminate = true
            else -> (holder as ProgressViewHolder).progressBar.isIndeterminate = true
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position] != null) {
            if (position == 0) {
                viewHead
            } else {
                viewItem
            }
        } else {
            viewProg
        }
    }

    fun insertData(items: List<News?>) {
        setLoaded()
        val positionStart = itemCount
        val itemCount = items.size
        this.items.addAll(items.filterNotNull())
        notifyItemRangeInserted(positionStart, itemCount)
    }

    fun setLoaded() {
        loading = false
        for (i in 0 until itemCount) {
            if (items[i] == null) {
                items.removeAt(i)
                notifyItemRemoved(i)
            }
        }
    }

    fun setLoading() {
        if (itemCount != 0) {
            //items.add(null)
            notifyItemInserted(itemCount - 1)
            loading = true
        }
    }

    fun resetListData() {
        items.clear()
        notifyDataSetChanged()
    }

    fun setOnLoadMoreListener(onLoadMoreListener: OnLoadMoreListener?) {
        this.onLoadMoreListener = onLoadMoreListener
    }

    private fun lastItemViewDetector(recyclerView: RecyclerView) {
        if (recyclerView.layoutManager is LinearLayoutManager) {
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val lastPos = layoutManager!!.findLastVisibleItemPosition()
                    if (!loading && lastPos == itemCount - 1 && onLoadMoreListener != null) {
                        if (onLoadMoreListener != null) {
                            val currentPage = itemCount / UiConfig.LOAD_MORE
                            onLoadMoreListener!!.onLoadMore(currentPage)
                        }
                        loading = true
                    }
                }
            })
        }
    }

    interface OnLoadMoreListener {
        fun onLoadMore(current_page: Int)
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    init {
        lastItemViewDetector(view)
    }
}