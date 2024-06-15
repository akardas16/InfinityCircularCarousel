package com.gigamole.infinitecycleviewpager

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Interpolator
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager


/**
 * Created by GIGAMOLE on 7/27/16.
 */
class HorizontalInfiniteCycleViewPager : ViewPager, ViewPageable {
    private var mInfiniteCycleManager: InfiniteCycleManager? = null

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    private fun init(context: Context, attributeSet: AttributeSet?) {
        mInfiniteCycleManager = InfiniteCycleManager(context, this, attributeSet)
    }

    var minPageScaleOffset: Float
        get() = if (mInfiniteCycleManager == null) 0.0f else mInfiniteCycleManager!!.minPageScaleOffset
        set(minPageScaleOffset) {
            if (mInfiniteCycleManager != null) mInfiniteCycleManager!!.minPageScaleOffset =
                minPageScaleOffset
        }

    var centerPageScaleOffset: Float
        get() = if (mInfiniteCycleManager == null) 0.0f else mInfiniteCycleManager!!.centerPageScaleOffset
        set(centerPageScaleOffset) {
            if (mInfiniteCycleManager != null) mInfiniteCycleManager!!.centerPageScaleOffset =
                centerPageScaleOffset
        }

    var minPageScale: Float
        get() = if (mInfiniteCycleManager == null) 0.0f else mInfiniteCycleManager!!.minPageScale
        set(minPageScale) {
            if (mInfiniteCycleManager != null) mInfiniteCycleManager!!.minPageScale = minPageScale
        }

    var maxPageScale: Float
        get() = if (mInfiniteCycleManager == null) 0.0f else mInfiniteCycleManager!!.maxPageScale
        set(maxPageScale) {
            if (mInfiniteCycleManager != null) mInfiniteCycleManager!!.maxPageScale = maxPageScale
        }

    var isMediumScaled: Boolean
        get() = mInfiniteCycleManager != null && mInfiniteCycleManager!!.isMediumScaled
        set(mediumScaled) {
            if (mInfiniteCycleManager != null) mInfiniteCycleManager!!.isMediumScaled = mediumScaled
        }

    var scrollDuration: Int
        get() = if (mInfiniteCycleManager == null) 0 else mInfiniteCycleManager!!.scrollDuration
        set(scrollDuration) {
            if (mInfiniteCycleManager != null) mInfiniteCycleManager!!.scrollDuration =
                scrollDuration
        }

    var pageDuration: Int
        get() = if (mInfiniteCycleManager == null) 0 else mInfiniteCycleManager!!.pageDuration
        set(pageDuration) {
            if (mInfiniteCycleManager != null) mInfiniteCycleManager!!.pageDuration = pageDuration
        }

    var interpolator: Interpolator?
        get() = if (mInfiniteCycleManager == null) null else mInfiniteCycleManager!!.interpolator
        set(interpolator) {
            if (mInfiniteCycleManager != null) mInfiniteCycleManager!!.interpolator = interpolator
        }



    var onInfiniteCyclePageTransformListener: OnInfiniteCyclePageTransformListener?
        get() = if (mInfiniteCycleManager == null) null else mInfiniteCycleManager!!.onInfiniteCyclePageTransformListener
        set(onInfiniteCyclePageTransformListener) {
            if (mInfiniteCycleManager != null) mInfiniteCycleManager!!.onInfiniteCyclePageTransformListener =
                onInfiniteCyclePageTransformListener
        }

    override fun setPageTransformer(reverseDrawingOrder: Boolean, transformer: PageTransformer?) {
        super.setPageTransformer(
            false,
            if (mInfiniteCycleManager == null) transformer else mInfiniteCycleManager!!.infinityCyclePageTransformer
        )
    }

    override fun setChildrenDrawingOrderEnabled(enabled: Boolean) {
        super.setChildrenDrawingOrderEnabled(InfiniteCycleManager.DEFAULT_DISABLE_FLAG)
    }

    override fun setClipChildren(clipChildren: Boolean) {
        super.setClipChildren(InfiniteCycleManager.DEFAULT_DISABLE_FLAG)
    }

    override fun setDrawingCacheEnabled(enabled: Boolean) {
        super.setDrawingCacheEnabled(InfiniteCycleManager.DEFAULT_DISABLE_FLAG)
    }


    override fun setChildrenDrawingCacheEnabled(enabled: Boolean) {
        super.setChildrenDrawingCacheEnabled(InfiniteCycleManager.DEFAULT_DISABLE_FLAG)
    }

    override fun setWillNotCacheDrawing(willNotCacheDrawing: Boolean) {
        super.setWillNotCacheDrawing(InfiniteCycleManager.DEFAULT_ENABLE_FLAG)
    }

    override fun setPageMargin(marginPixels: Int) {
        super.setPageMargin(InfiniteCycleManager.DEFAULT_PAGE_MARGIN)
    }

    override fun setOffscreenPageLimit(limit: Int) {
        super.setOffscreenPageLimit(InfiniteCycleManager.DEFAULT_OFFSCREEN_PAGE_LIMIT)
    }

    override fun setOverScrollMode(overScrollMode: Int) {
        super.setOverScrollMode(OVER_SCROLL_NEVER)
    }

    override fun addViewInLayout(child: View, index: Int, params: ViewGroup.LayoutParams): Boolean {
        return super.addViewInLayout(child, 0, params)
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        super.addView(child, 0, params)
    }

    override fun setAdapter(adapter: PagerAdapter?) {
        if (mInfiniteCycleManager == null) super.setAdapter(adapter)
        else {
            super.setAdapter(mInfiniteCycleManager!!.setAdapter(adapter))
            mInfiniteCycleManager!!.resetPager()
        }
    }

    override fun getAdapter(): PagerAdapter? {
        if (mInfiniteCycleManager == null) return super.getAdapter()
        return if (mInfiniteCycleManager!!.infiniteCyclePagerAdapter == null) super.getAdapter() else mInfiniteCycleManager!!.infiniteCyclePagerAdapter!!.pagerAdapter
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return try {
            if (mInfiniteCycleManager == null) super.onTouchEvent(ev) else mInfiniteCycleManager!!.onTouchEvent(
                ev
            ) && super.onTouchEvent(ev)
        } catch (e: IllegalArgumentException) {
            true
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return try {
            if (mInfiniteCycleManager == null) super.onInterceptTouchEvent(ev) else mInfiniteCycleManager!!.onInterceptTouchEvent(
                ev
            ) && super.onInterceptTouchEvent(ev)
        } catch (e: IllegalArgumentException) {
            true
        }
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        if (mInfiniteCycleManager != null) mInfiniteCycleManager!!.onWindowFocusChanged(
            hasWindowFocus
        )
        super.onWindowFocusChanged(hasWindowFocus)
    }

    override fun onDetachedFromWindow() {
        if (mInfiniteCycleManager != null) mInfiniteCycleManager!!.stopAutoScroll()
        super.onDetachedFromWindow()
    }

    override fun setCurrentItem(item: Int) {
        setCurrentItem(item, true)
    }

    override fun setCurrentItem(item: Int, smoothScroll: Boolean) {
        if (mInfiniteCycleManager != null) super.setCurrentItem(
            mInfiniteCycleManager!!.setCurrentItem(
                item
            ), true
        )
    }

    val realItem: Int
        get() = if (mInfiniteCycleManager == null) currentItem else mInfiniteCycleManager!!.realItem

    val state: Int
        get() = if (mInfiniteCycleManager == null) SCROLL_STATE_IDLE else mInfiniteCycleManager!!.state

    fun notifyDataSetChanged() {
        if (mInfiniteCycleManager != null) mInfiniteCycleManager!!.notifyDataSetChanged()
    }

    fun invalidateTransformer() {
        if (mInfiniteCycleManager != null) mInfiniteCycleManager!!.invalidateTransformer()
    }

    fun postInvalidateTransformer() {
        if (mInfiniteCycleManager != null) mInfiniteCycleManager!!.postInvalidateTransformer()
    }

    fun startAutoScroll(isAutoScrollPositive: Boolean) {
        if (mInfiniteCycleManager != null) mInfiniteCycleManager!!.startAutoScroll(
            isAutoScrollPositive
        )
    }
    fun isAutoScrolling(): Boolean = if (mInfiniteCycleManager != null) mInfiniteCycleManager!!.isAutoScroll else false


    fun stopAutoScroll() {
        if (mInfiniteCycleManager != null) mInfiniteCycleManager!!.stopAutoScroll()
    }
}
