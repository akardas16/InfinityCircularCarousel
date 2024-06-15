package com.gigamole.infinitecycleviewpager

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.viewpager.widget.PagerAdapter


/**
 * Created by GIGAMOLE on 7/27/16.
 */
class HorizontalPagerAdapterCompose(
    mContext: Context, private val list: List<Any>,
    val compose: (cmp: ComposeView, item: Any) -> Unit = { _, _ -> },
) :
    PagerAdapter() {


    private val mLayoutInflater: LayoutInflater = LayoutInflater.from(mContext)

    override fun getCount(): Int {
        return list.size
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }


    override fun instantiateItem(container: ViewGroup, position: Int): Any {


        val view: View = mLayoutInflater.inflate(R.layout.item_compose, container, false)

        compose(view.findViewById(R.id.composevv), list[position])
        container.addView(view)
        return view
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }


    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}
