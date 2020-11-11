package com.pocket.knowledge.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.pocket.knowledge.R
import com.pocket.knowledge.activities.MainActivity
import com.pocket.knowledge.activities.PostDetailActivity
import com.pocket.knowledge.activities.PostDetailOfflineActivity
import com.pocket.knowledge.adapter.NewsAdapter
import com.pocket.knowledge.models.News
import com.pocket.knowledge.utils.Constant
import com.pocket.knowledge.utils.DbHandler
import com.pocket.knowledge.utils.NetworkCheck.Companion.isConnect
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_favorite.*

class FavoriteFragment : Fragment() {
    private var data: ArrayList<News> = ArrayList()
    private var rootView: View? = null
    private var parentView: View? = null

    private var mAdapter: NewsAdapter? = null
    private var mainActivity: MainActivity? = null

    private var databaseHandler: DbHandler? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_favorite, container,false)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parentView = activity!!.main_content

        recyclerView!!.layoutManager = LinearLayoutManager(activity)
        recyclerView!!.setHasFixedSize(true)
        loadDataFromDatabase()
    }

    override fun onResume() {
        super.onResume()
        loadDataFromDatabase()
    }

    private fun loadDataFromDatabase() {
        databaseHandler = DbHandler(activity)
        data = databaseHandler!!.allData as ArrayList<News>

        //set data and list adapter
        mAdapter = activity?.let { NewsAdapter(it, recyclerView, data) }
        recyclerView!!.adapter = mAdapter
        if (data.isEmpty()) {
            showNoItemView(true)
        } else {
            showNoItemView(false)
        }

        // on item list clicked
        mAdapter!!.setOnItemClickListener(itemClickListener)
    }

    private val itemClickListener= object : NewsAdapter.OnItemClickListener{
        override fun onItemClick(view: View?, obj: News?, position: Int) {
            if (isConnect(activity!!)) {
                val intent = Intent(activity, PostDetailActivity::class.java)
                intent.putExtra(Constant.EXTRA_OBJC, obj)
                startActivity(intent)
            } else {
                val intent = Intent(activity, PostDetailOfflineActivity::class.java)
                intent.putExtra(Constant.EXTRA_OBJC, obj)
                startActivity(intent)
            }
        }


    }

    private fun showNoItemView(show: Boolean) {
        val lytNoItem = rootView!!.findViewById<View>(R.id.lyt_no_item_later)
        (rootView!!.findViewById<View>(R.id.no_item_message) as TextView).setText(R.string.no_favorite_found)
        if (show) {
            recyclerView!!.visibility = View.GONE
            lytNoItem.visibility = View.VISIBLE
        } else {
            recyclerView!!.visibility = View.VISIBLE
            lytNoItem.visibility = View.GONE
        }
    }
}