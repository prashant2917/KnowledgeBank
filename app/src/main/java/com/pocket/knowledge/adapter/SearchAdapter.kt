package com.pocket.knowledge.adapter

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.pocket.knowledge.R
import kotlinx.android.synthetic.main.lsv_item_suggestion.view.*
import java.io.Serializable
import java.util.*

class SearchAdapter(context: Context) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {
    private var items: List<String?>
    private var onItemClickListener: OnItemClickListener? = null
    private val prefs: SharedPreferences = context.getSharedPreferences("PREF_RECENT_SEARCH", Context.MODE_PRIVATE)

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var title: TextView = v.txt_title
        var lytParent: LinearLayout = v.recent_lyt_parent

    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.lsv_item_suggestion, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val p = items[position]
        holder.title.text = p
        holder.lytParent.setOnClickListener { v: View? -> onItemClickListener!!.onItemClick(v, p, position) }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return items.size
    }

    interface OnItemClickListener {
        fun onItemClick(view: View?, viewModel: String?, pos: Int)
    }

    fun refreshItems() {
        items = searchHistory
        //Collections.reverse(items)
        items.reversed()
        notifyDataSetChanged()
    }

    private inner class SearchObject(var items: MutableList<String?>) : Serializable

    /**
     * To save last state request
     */
    fun addSearchHistory(s: String?) {
        val searchObject = SearchObject(searchHistory)
        if (searchObject.items.contains(s)) searchObject.items.remove(s)
        searchObject.items.add(s)
        if (searchObject.items.size > MAX_HISTORY_ITEMS) searchObject.items.removeAt(0)
        val json = Gson().toJson(searchObject, SearchObject::class.java)
        prefs.edit().putString(SEARCH_HISTORY_KEY, json).apply()
    }

    private val searchHistory: MutableList<String?>
        private get() {
            val json = prefs.getString(SEARCH_HISTORY_KEY, "")
            if (json == "") return ArrayList()
            val searchObject = Gson().fromJson(json, SearchObject::class.java)
            return searchObject.items
        }

    companion object {
        private const val SEARCH_HISTORY_KEY = "_SEARCH_HISTORY_KEY"
        private const val MAX_HISTORY_ITEMS = 0
    }

    init {
        items = searchHistory
        //Collections.reverse(items)
        items.reversed()
    }
}