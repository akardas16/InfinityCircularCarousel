package com.gigamole.infinitecycleviewpager

import android.database.DataSetObserver
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter

/**
 * Created by GIGAMOLE on 7/27/16.
 */
// PagerAdapter that wrap original ViewPager adapter with infinite scroll feature.
// There is VIRTUAL_ITEM_COUNT which equals to 10_000_000.
// At start, ViewPager set position to half of virtual and find nearest zero position.
internal class InfiniteCyclePagerAdapter(val pagerAdapter: PagerAdapter) : PagerAdapter() {
    private var mOnNotifyDataSetChangedListener: OnNotifyDataSetChangedListener? = null

    fun setOnNotifyDataSetChangedListener(onNotifyDataSetChangedListener: OnNotifyDataSetChangedListener?) {
        mOnNotifyDataSetChangedListener = onNotifyDataSetChangedListener
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        pagerAdapter.destroyItem(container, getVirtualPosition(position), `object`)
    }

    override fun finishUpdate(container: ViewGroup) {
        pagerAdapter.finishUpdate(container)
    }

    override fun getCount(): Int {
        if (pagerAdapter.count == 0) return 0
        return VIRTUAL_ITEM_COUNT
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return pagerAdapter.getPageTitle(getVirtualPosition(position))
    }

    override fun getPageWidth(position: Int): Float {
        return pagerAdapter.getPageWidth(getVirtualPosition(position))
    }

    override fun isViewFromObject(view: View, o: Any): Boolean {
        return pagerAdapter.isViewFromObject(view, o)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        return pagerAdapter.instantiateItem(container, getVirtualPosition(position))
    }

    override fun saveState(): Parcelable? {
        return pagerAdapter.saveState()
    }


    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {
        pagerAdapter.restoreState(state, loader)
    }

    override fun startUpdate(container: ViewGroup) {
        pagerAdapter.startUpdate(container)
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver) {
        pagerAdapter.unregisterDataSetObserver(observer)
    }

    override fun registerDataSetObserver(observer: DataSetObserver) {
        pagerAdapter.registerDataSetObserver(observer)
    }

    override fun notifyDataSetChanged() {
        pagerAdapter.notifyDataSetChanged()
        // Callback for invalidating transformer position
        if (mOnNotifyDataSetChangedListener != null) mOnNotifyDataSetChangedListener!!.onChanged()
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        pagerAdapter.setPrimaryItem(container, position, `object`)
    }

    override fun getItemPosition(`object`: Any): Int {
        return pagerAdapter.getItemPosition(`object`)
    }

    // Main feature of this adapter which return virtual position
    // relative to virtual count and original count
    fun getVirtualPosition(realPosition: Int): Int {
        return realPosition % pagerAdapter.count
    }

    interface OnNotifyDataSetChangedListener {
        fun onChanged()
    }

    companion object {
        private const val VIRTUAL_ITEM_COUNT = 10000000
    }
}