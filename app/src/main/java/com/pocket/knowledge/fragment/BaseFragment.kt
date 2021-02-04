package com.pocket.knowledge.fragment

import androidx.fragment.app.Fragment
import com.pocket.knowledge.activities.MainActivity

open class BaseFragment : Fragment() {
    private val mainActivity: MainActivity
        get() = activity as MainActivity


    open fun setToolbarTitle(title: String) {
        mainActivity.setToolBarTitle(title)

    }
}
