package com.pocket.knowledge.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.pocket.knowledge.R
import com.pocket.knowledge.activities.VideoPlayerActivity
import com.pocket.knowledge.activities.YoutubePlayerActivity
import com.pocket.knowledge.config.AppConfig
import com.pocket.knowledge.models.Images
import com.pocket.knowledge.utils.Constant
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.lsv_item_image_slider.view.*

class AdapterImageSlider(private val context: Context, private val items: List<Images>) : PagerAdapter() {
    private var onItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(view: View?, images: Images?)
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view === obj
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    override fun instantiateItem(viewGroup: ViewGroup, position: Int): Any {
        val post = items[position]
        val inflate = LayoutInflater.from(viewGroup.context).inflate(R.layout.lsv_item_image_slider, viewGroup, false)
        //val news_image: TouchImageView = inflate.findViewById(R.id.image_detail)
        if (post.content_type != null && post.content_type == "youtube") {
            Picasso.get()
                    .load(Constant.YOUTUBE_IMG_FRONT + post.video_id + Constant.YOUTUBE_IMG_BACK)
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(inflate.image_detail)
            inflate.image_detail.setOnClickListener { v: View? ->
                val intent = Intent(context, YoutubePlayerActivity::class.java)
                intent.putExtra("video_id", post.video_id)
                context.startActivity(intent)
            }
        } else if (post.content_type != null && post.content_type == "Url") {
            Picasso.get()
                    .load(AppConfig.ADMIN_PANEL_URL + "/upload/" + post.image_name.replace(" ", "%20"))
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(inflate.image_detail)
            inflate.image_detail.setOnClickListener { v: View? ->
                val intent = Intent(context, VideoPlayerActivity::class.java)
                intent.putExtra("video_url", post.video_url)
                context.startActivity(intent)
            }
        } else if (post.content_type != null && post.content_type == "Upload") {
            Picasso.get()
                    .load(AppConfig.ADMIN_PANEL_URL + "/upload/" + post.image_name.replace(" ", "%20"))
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(inflate.image_detail)
            inflate.image_detail.setOnClickListener { v: View? ->
                val intent = Intent(context, VideoPlayerActivity::class.java)
                intent.putExtra("video_url", AppConfig.ADMIN_PANEL_URL + "/upload/video/" + post.video_url)
                context.startActivity(intent)
            }
        } else {
            Picasso.get()
                    .load(AppConfig.ADMIN_PANEL_URL + "/upload/" + post.image_name.replace(" ", "%20"))
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(inflate.image_detail)
            inflate.image_detail.setOnClickListener { view: View? ->
                if (onItemClickListener != null) {
                    onItemClickListener!!.onItemClick(view, post)
                }
            }
        }
        viewGroup.addView(inflate)
        return inflate
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun destroyItem(viewGroup: ViewGroup, i: Int, obj: Any) {
        viewGroup.removeView(obj as View)
    }

}