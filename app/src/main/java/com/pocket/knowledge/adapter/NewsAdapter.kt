package com.pocket.knowledge.adapter

import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
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
import kotlinx.android.synthetic.main.lsv_item_load_more.view.*
import kotlinx.android.synthetic.main.lsv_item_news.view.*

class NewsAdapter(private val context: Context, view: RecyclerView, private var items: ArrayList<News>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val newsProg = 0
    private val newsItem = 1
    private var loading = false
    private var onLoadMoreListener: OnLoadMoreListener? = null
    private var mOnItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(view: View?, obj: News?, position: Int)
    }

    fun setOnItemClickListener(mItemClickListener: OnItemClickListener?) {
        mOnItemClickListener = mItemClickListener
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
        val vh: RecyclerView.ViewHolder
        vh = if (viewType == newsItem) {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.lsv_item_news, parent, false)
            OriginalViewHolder(v)
        } else {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.lsv_item_load_more, parent, false)
            ProgressViewHolder(v)
        }
        return vh
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is OriginalViewHolder) {
            val p = items[position]
            if (holder.getAdapterPosition() % AdsConfig.NATIVE_AD_NEWS_FEED_INTERVAL == AdsConfig.NATIVE_AD_NEWS_FEED_INDEX) {
                holder.bindNativeAdView()
            } else {
                holder.nativeTemplate.visibility = View.GONE
            }
            holder.title.text = Html.fromHtml(p.news_title)
            holder.date.visibility = View.VISIBLE
            holder.icDate.visibility = View.VISIBLE
            holder.date.text = getFormatedDateSimple(p.news_date)
            holder.category.text = Html.fromHtml(p.news_description)
            holder.comment.text = p.comments_count.toString() + ""
            if (p.content_type != null && p.content_type == "Post") {
                holder.thumbnailVideo.visibility = View.GONE
            } else {
                holder.thumbnailVideo.visibility = View.VISIBLE
            }
            if (p.content_type != null && p.content_type == "youtube") {
                Picasso.get()
                        .load(Constant.YOUTUBE_IMG_FRONT + p.video_id + Constant.YOUTUBE_IMG_BACK)
                        .placeholder(R.drawable.ic_thumbnail)
                        .into(holder.image)
            } else {
                Picasso.get()
                        .load(AppConfig.ADMIN_PANEL_URL + "/upload/" + p.news_image.replace(" ", "%20"))
                        .placeholder(R.drawable.ic_thumbnail)
                        .into(holder.image)
            }
            holder.lytParent.setOnClickListener { view: View? ->
                if (mOnItemClickListener != null) {
                    mOnItemClickListener!!.onItemClick(view, p, position)
                }
            }
        } else {
            (holder as ProgressViewHolder).progressBar.isIndeterminate = true
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        //return this.items.get(position) != null ? VIEW_ITEM : VIEW_PROG;
        return if (items[position] != null) {
            newsItem
        } else {
            newsProg
        }
    }

    fun insertData(items: List<News?>) {
        setLoaded()
        val positionStart = itemCount
        val itemCount = items.size
        this.items.addAll(items.filterNotNull())
        notifyItemRangeInserted(positionStart, itemCount)
    }

    private fun setLoaded() {
        loading = false
        for (i in 0 until itemCount) {
            if (items[i] == null) {
                items.removeAt(i)
                notifyItemRemoved(i)
            }
        }
    }

    fun resetListData() {
        items = ArrayList()
        notifyDataSetChanged()
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