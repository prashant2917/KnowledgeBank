package com.pocket.knowledge.adapter

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pocket.knowledge.R
import com.pocket.knowledge.config.AppConfig
import com.pocket.knowledge.models.Category
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.lsv_item_category.view.*
import java.util.*

class CategoryAdapter // Provide a suitable constructor (depends on the kind of dataset)
(private var items: List<Category>) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {
    private var mOnItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(view: View?, obj: Category?, position: Int)
    }

    fun setOnItemClickListener(mItemClickListener: OnItemClickListener?) {
        mOnItemClickListener = mItemClickListener
    }

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        // each data item is just a string in this case
        var name: TextView = v.name
        var postCount: TextView = v.post_count
        var lytParent: LinearLayout = v.recent_lyt_parent
        var categoryImage: ImageView = v.category_image

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // create a new view
        val v = LayoutInflater.from(parent.context).inflate(R.layout.lsv_item_category, parent, false)
        return ViewHolder(v)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val c = items[position]
        holder.name.text = Html.fromHtml(c.categoryName)
        holder.postCount.visibility = View.GONE
        Picasso.get()
                .load(AppConfig.ADMIN_PANEL_URL + "/upload/category/" + c.categoryImage.replace(" ", "%20"))
                .placeholder(R.drawable.ic_thumbnail)
                .into(holder.categoryImage)
        holder.lytParent.setOnClickListener { view: View? ->
            if (mOnItemClickListener != null) {
                mOnItemClickListener!!.onItemClick(view, c, position)
            }
        }
    }

    fun setListData(items: List<Category>) {
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