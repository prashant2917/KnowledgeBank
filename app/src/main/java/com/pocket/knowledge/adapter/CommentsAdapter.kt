package com.pocket.knowledge.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pocket.knowledge.R
import com.pocket.knowledge.activities.MyApplication
import com.pocket.knowledge.config.AppConfig
import com.pocket.knowledge.models.Comments
import com.pocket.knowledge.utils.Tools.timeStringtoMilis
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.lsv_item_comments.view.*
import org.ocpsoft.prettytime.PrettyTime
import java.util.*

class CommentsAdapter // Provide a suitable constructor (depends on the kind of dataset)
(private val ctx: Context, private var items: List<Comments>) : RecyclerView.Adapter<CommentsAdapter.ViewHolder>() {
    private var myApplication: MyApplication? = null
    private var mOnItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(view: View?, obj: Comments?, position: Int, context: Context?)
    }

    fun setOnItemClickListener(mItemClickListener: OnItemClickListener?) {
        mOnItemClickListener = mItemClickListener
    }

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        // each data item is just a string in this case
        var userName: TextView? = v.user_name
        var userImage:ImageView?= v.user_image
        var commentDate:TextView?=v.comment_date
        var commentMessage:TextView? = v.edt_comment_message
        var lytParent: LinearLayout? = v.recent_lyt_parent

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // create a new view
        val v = LayoutInflater.from(parent.context).inflate(R.layout.lsv_item_comments, parent, false)
        val vh = ViewHolder(v)
        myApplication = MyApplication.instance
        return vh
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val c = items[position]
        if (myApplication!!.isLogin && myApplication!!.userId == c.user_id) {
            holder.userName?.text = c.name + " ( " + ctx.resources.getString(R.string.txt_you) + " )"
        } else {
            holder.userName?.text = c.name
        }
        Picasso.get()
                .load(AppConfig.ADMIN_PANEL_URL + "/upload/avatar/" + c.image.replace(" ", "%20"))
                .resize(200, 200)
                .centerCrop()
                .placeholder(R.drawable.ic_user_account)
                .into(holder.userImage)


        //holder.comment_date.setText(c.date_time);
        val prettyTime = PrettyTime()
        val timeAgo = timeStringtoMilis(c.date_time)
        holder.commentDate?.text = prettyTime.format(Date(timeAgo))
        holder.commentMessage?.text = c.content
        holder.lytParent?.setOnClickListener { view: View? ->
            if (mOnItemClickListener != null) {
                mOnItemClickListener!!.onItemClick(view, c, position, ctx)
            }
        }
    }

    fun setListData(items: List<Comments>) {
        this.items = items
        notifyDataSetChanged()
    }

    fun resetListData() {
        items = ArrayList()
        notifyDataSetChanged()
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return items.size
    }

}