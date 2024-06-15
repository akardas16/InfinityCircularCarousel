package com.gigamole.infinitecycleviewpager


import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.gigamole.infinitecycleviewpager.InfiniteCyclePagerAdapter.OnNotifyDataSetChangedListener
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

/**
 * Created by GIGAMOLE on 7/27/16.
 */
@Suppress("unused")
internal class InfiniteCycleManager(
    private val mContext: Context,
    viewPageable: ViewPageable,
    attributeSet: AttributeSet?
) : OnNotifyDataSetChangedListener {
    // Infinite ViewPager and adapter
    private var mViewPageable: ViewPageable? = null
    private val mCastViewPageable: View
    var infiniteCyclePagerAdapter: InfiniteCyclePagerAdapter? = null
        private set

    // Inner and outer state of scrolling
    private var mInnerPageScrolledState = PageScrolledState.IDLE
    private var mOuterPageScrolledState = PageScrolledState.IDLE

    // Page scrolled info positions
    private var mPageScrolledPositionOffset = 0f
    private var mPageScrolledPosition = 0f

    // When item count equals to 3 we need stack count for know is item on position -2 or 2 is placed
    private var mStackCount = 0

    // Item count of original adapter
    private var mItemCount = 0

    // Flag to know is left page need bring to front for correct scrolling present
    private var mIsLeftPageBringToFront = false

    // Flag to know is right page need bring to front for correct scrolling present
    private var mIsRightPageBringToFront = false

    // Detect if was minus one position of transform page for correct handle of page bring to front
    private var mWasMinusOne = false

    // Detect if was plus one position of transform page for correct handle of page bring to front
    private var mWasPlusOne = false

    // Hit rect of view bounds to detect inside touch
    private val mHitRect = Rect()

    // Flag for invalidate transformer side scroll when use setCurrentItem() method
    private var mIsInitialItem = false

    // Flag for setCurrentItem to zero of half the virtual count when set adapter
    private var mIsAdapterInitialPosition = false

    // Flag for data set changed callback to invalidateTransformer()
    private var mIsDataSetChanged = false

    // Detect is ViewPager state
    var state: Int = 0
        private set

    // Custom transform listener
    var onInfiniteCyclePageTransformListener: OnInfiniteCyclePageTransformListener? = null

    // Page scale offset at minimum scale(left and right bottom pages)
    var minPageScaleOffset: Float = 0f

    // Page scale offset at maximum scale(center top page)
    var centerPageScaleOffset: Float = 0f

    // Minimum page scale(left and right pages)
    private var mMinPageScale = 0f

    // Maximum page scale(center page)
    private var mMaxPageScale = 0f

    // Center scale by when scroll position is on half
    private var mCenterScaleBy = 0f

    // Use medium scale or just from max to min
    var isMediumScaled: Boolean = false



    // Scroll duration of snapping
    private var mScrollDuration = 0

    // Duration for which a page will be shown before moving on to next one when {@link mIsAutoScroll} is TRUE
    private var mPageDuration = 0

    // Interpolator of snapping
    private var mInterpolator: Interpolator? = null

    // Auto scroll values
    private var mIsAutoScroll = false
    private var mIsAutoScrollPositive = false

    // Auto scroll handlers
    private val mAutoScrollHandler = Handler(Looper.getMainLooper())
    private val mAutoScrollRunnable: Runnable = object : Runnable {
        override fun run() {
            if (!mIsAutoScroll) return
            mViewPageable?.currentItem =
                realItem + (if (mIsAutoScrollPositive) 1 else -1)
            mAutoScrollHandler.postDelayed(this, mPageDuration.toLong())
        }
    }
    val isAutoScroll: Boolean
        get() = mIsAutoScroll


    private fun processAttributeSet(attributeSet: AttributeSet?) {
        if (attributeSet == null) return

        try {
            minPageScaleOffset = DEFAULT_MIN_PAGE_SCALE_OFFSET.toFloat()
            centerPageScaleOffset = DEFAULT_CENTER_PAGE_SCALE_OFFSET.toFloat()
            minPageScale =  DEFAULT_MIN_SCALE
            maxPageScale = DEFAULT_MAX_SCALE
            isMediumScaled = DEFAULT_IS_MEDIUM_SCALED
            scrollDuration = DEFAULT_SCROLL_DURATION
            pageDuration = DEFAULT_SCROLL_DURATION

            // Retrieve interpolator
            var interpolator: Interpolator? = null
            try {
                val interpolatorId = 0
                interpolator = if (interpolatorId == 0) null else AnimationUtils.loadInterpolator(
                    mContext,
                    interpolatorId
                )
            } catch (exception: Resources.NotFoundException) {
                interpolator = null
                exception.printStackTrace()
            } finally {
                this.interpolator = interpolator
            }
        } finally {

        }
    }

    var minPageScale: Float
        get() = mMinPageScale
        set(minPageScale) {
            mMinPageScale = minPageScale
            resetScaleBy()
        }

    var maxPageScale: Float
        get() = mMaxPageScale
        set(maxPageScale) {
            mMaxPageScale = maxPageScale
            resetScaleBy()
        }

    var scrollDuration: Int
        get() = mScrollDuration
        set(scrollDuration) {
            mScrollDuration = scrollDuration
            resetScroller()
        }

    var pageDuration: Int
        get() = mPageDuration
        set(pageDuration) {
            mPageDuration = pageDuration
            resetScroller()
        }

    var interpolator: Interpolator?
        get() = mInterpolator
        set(interpolator) {
            mInterpolator = interpolator ?: SpringInterpolator()
            resetScroller()
        }

    val infinityCyclePageTransformer: InfiniteCyclePageTransformer
        get() = InfiniteCyclePageTransformer()

    fun setAdapter(adapter: PagerAdapter?): PagerAdapter? {
        // If adapter count bigger then 2 need to set InfiniteCyclePagerAdapter
        if (adapter != null && adapter.count >= MIN_CYCLE_COUNT) {
            mItemCount = adapter.count
            infiniteCyclePagerAdapter = InfiniteCyclePagerAdapter(adapter)
            infiniteCyclePagerAdapter!!.setOnNotifyDataSetChangedListener(this)
            return infiniteCyclePagerAdapter
        } else {
            if (infiniteCyclePagerAdapter != null) {
                infiniteCyclePagerAdapter!!.setOnNotifyDataSetChangedListener(null)
                infiniteCyclePagerAdapter = null
            }
            return adapter
        }
    }

    // We are disable multitouch on ViewPager and settling scroll, also we disable outside drag
    fun onTouchEvent(event: MotionEvent): Boolean {
        if (mViewPageable!!.adapter == null || mViewPageable?.adapter?.count == 0) return false
        if (mIsAutoScroll || mIsInitialItem || mViewPageable?.isFakeDragging == true) return false
        if (event.pointerCount > MIN_POINTER_COUNT || !mViewPageable?.hasWindowFocus()!!) event.action =
            MotionEvent.ACTION_UP
        checkHitRect(event)
        return true
    }

    fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return onTouchEvent(event)
    }

    // When not has window focus clamp to nearest position
    fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        if (hasWindowFocus) invalidateTransformer()
    }

    // Set current item where you put original adapter position and this method calculate nearest
    // position to scroll from center if at first initial position or nearest position of old position
    fun setCurrentItem(item: Int): Int {
        mIsInitialItem = true

        if (mViewPageable!!.adapter == null ||
            mViewPageable?.adapter?.count!! < MIN_CYCLE_COUNT
        ) return item

        val count = mViewPageable?.adapter?.count
        if (mIsAdapterInitialPosition) {
            mIsAdapterInitialPosition = false
            return ((infiniteCyclePagerAdapter!!.count / 2) / count!!) * count
        } else return (mViewPageable?.currentItem!! + min(
            count!!.toDouble(),
            item.toDouble()
        ) - realItem).toInt()
    }

    val realItem: Int
        // Need to get current position of original adapter. We cant override getCurrentItem() method,
        get() {
            if (mViewPageable!!.adapter == null ||
                mViewPageable!!.adapter!!.count < MIN_CYCLE_COUNT
            ) return mViewPageable!!.currentItem
            return infiniteCyclePagerAdapter!!.getVirtualPosition(mViewPageable!!.currentItem)
        }

    // Now you can call notify data on ViewPager nor adapter to invalidate all of positions
    fun notifyDataSetChanged() {
        if (infiniteCyclePagerAdapter == null) {
            mViewPageable!!.adapter?.notifyDataSetChanged()
            mIsDataSetChanged = true
        } else infiniteCyclePagerAdapter!!.notifyDataSetChanged()
        postInvalidateTransformer()
    }

    // If you need to update transformer call this method, which is trigger fake scroll
    fun invalidateTransformer() {
        if (mViewPageable!!.adapter == null || mViewPageable!!.adapter?.count == 0 || mViewPageable!!.childCount == 0) return
        if (mViewPageable!!.beginFakeDrag()) {
            mViewPageable?.apply {
                fakeDragBy(0.0f)
                endFakeDrag()
            }

        }
    }

    fun postInvalidateTransformer() {
        mViewPageable!!.post {
            invalidateTransformer()
            mIsDataSetChanged = false
        }
    }

    // Enable hardware layer when transform pages
    private fun enableHardwareLayer(v: View) {
        val layerType = View.LAYER_TYPE_NONE
        if (v.layerType != layerType) v.setLayerType(layerType, null)
    }

    // Disable hardware layer when idle


    // Detect is we are idle in pageScrolled() callback, not in scrollStateChanged()
    private fun isSmallPositionOffset(positionOffset: Float): Boolean {
        return abs(positionOffset.toDouble()) < 0.0001f
    }

    // Check view bounds touch
    private fun checkHitRect(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_DOWN) {
            mHitRect[mCastViewPageable.left, mCastViewPageable.top, mCastViewPageable.right] =
                mCastViewPageable.bottom
        } else if (event.action == MotionEvent.ACTION_MOVE && !mHitRect.contains(
                mCastViewPageable.left + event.x.toInt(),
                mCastViewPageable.top + event.y.toInt()
            )
        ) event.action = MotionEvent.ACTION_UP
    }

    // Reset scroller to own
    private fun resetScroller() {
        if (mViewPageable == null) return
        try {
            val scroller =
                ViewPager::class.java.getDeclaredField(
                    "mScroller"
                )
            scroller.isAccessible = true
            val infiniteCycleScroller =
                InfiniteCycleScroller(mContext, mInterpolator)
            infiniteCycleScroller.duration = mScrollDuration
            scroller[mViewPageable] = infiniteCycleScroller
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Reset pager when reset adapter
    fun resetPager() {
        mIsAdapterInitialPosition = true
        mViewPageable!!.currentItem = 0
        postInvalidateTransformer()
    }

    // Recalculate scale by variable
    private fun resetScaleBy() {
        mCenterScaleBy = (mMaxPageScale - mMinPageScale) * 0.5f
    }

    // Start auto scroll
    fun startAutoScroll(isAutoScrollPositive: Boolean) {
        if (mIsAutoScroll && isAutoScrollPositive == mIsAutoScrollPositive) return
        mIsAutoScroll = true
        mIsAutoScrollPositive = isAutoScrollPositive

        mAutoScrollHandler.removeCallbacks(mAutoScrollRunnable)
        mAutoScrollHandler.post(mAutoScrollRunnable)
    }

    // Stop auto scroll
    fun stopAutoScroll() {
        if (!mIsAutoScroll) return
        mIsAutoScroll = false
        mAutoScrollHandler.removeCallbacks(mAutoScrollRunnable)
    }

    override fun onChanged() {
        mIsDataSetChanged = true
    }

    // The main presenter feature of this library is this InfiniteCyclePageTransformer.
    // The logic is based to cycle items like carousel mode. There we don't have direct method
    // to set z-index, so we need to handle only one method bringToFront().
    inner class InfiniteCyclePageTransformer : ViewPager.PageTransformer {
        override fun transformPage(page: View, position: Float) {
            if (onInfiniteCyclePageTransformListener != null) onInfiniteCyclePageTransformListener!!.onPreTransform(
                page,
                position
            )

            // Handle page layer and bounds visibility
            enableHardwareLayer(page)
            if (mItemCount == MIN_CYCLE_COUNT) {
                if (position > 2.0f || position < -2.0f ||
                    (mStackCount != 0 && position > 1.0f) ||
                    (mStackCount != 0 && position < -1.0f)
                ) {
                    page.visibility = View.GONE
                    return
                } else page.visibility = View.VISIBLE
            }

            val pageSize =
                (page.measuredWidth).toFloat()

            // Page offsets relative to scale
            val pageMinScaleOffset = pageSize * mMinPageScale
            val pageSubScaleByOffset = pageSize * mCenterScaleBy

            // Page offsets from bounds
            val pageMinScaleEdgeOffset = (pageSize - pageMinScaleOffset) * 0.5f
            val pageMaxScaleEdgeOffset = (pageSize - (pageSize * mMaxPageScale)) * 0.5f
            val pageSubScaleEdgeOffset =
                (pageSize - (pageSize * (mMinPageScale + mCenterScaleBy))) * 0.5f

            val scale: Float
            val translation: Float

            // Detect when the count <= 3 and another page of side stack not placed
            if (mItemCount < MIN_CYCLE_COUNT + 1 && mStackCount == 0 && position > -2.0f && position < -1.0f) {
                val fraction = -1.0f - position

                scale = mMinPageScale
                translation = (pageSize - pageMinScaleEdgeOffset + minPageScaleOffset) +
                        (pageSize * 2.0f - pageMinScaleOffset - minPageScaleOffset * 2.0f) * fraction

                mStackCount++
            } else if (mItemCount > MIN_CYCLE_COUNT && position >= -2.0f && position < -1.0f) {
                val fraction = 1.0f + (position + 1.0f)

                scale = mMinPageScale
                translation = (pageSize * 2.0f) - ((pageSize +
                        pageMinScaleEdgeOffset - minPageScaleOffset) * fraction)
            } else if (position >= -1.0f && position <= -0.5f) {
                val positiveFraction = 1.0f + (position + 0.5f) * 2.0f
                val negativeFraction = 1.0f - positiveFraction

                if (isMediumScaled) {
                    val startOffset: Float = pageSize - pageSubScaleByOffset -
                            pageMaxScaleEdgeOffset + minPageScaleOffset

                    scale = (mMinPageScale + mCenterScaleBy) - (mCenterScaleBy * negativeFraction)
                    translation = startOffset - ((startOffset - pageSubScaleEdgeOffset +
                            centerPageScaleOffset) * positiveFraction)
                } else {
                    val startOffset: Float =
                        pageSize - pageMinScaleEdgeOffset + minPageScaleOffset

                    scale = (mMaxPageScale) - ((mMaxPageScale - mMinPageScale) * negativeFraction)
                    translation = (startOffset) - ((startOffset - pageMaxScaleEdgeOffset +
                            centerPageScaleOffset) * positiveFraction)
                }
            } else if (position >= -0.5f && position <= 0.0f) {
                val fraction = -position * 2.0f

                scale =
                    mMaxPageScale - (if (isMediumScaled) mCenterScaleBy * fraction else 0.0f)
                translation =
                    ((if (isMediumScaled) pageSubScaleEdgeOffset else pageMaxScaleEdgeOffset) -
                            centerPageScaleOffset) * fraction
            } else if (position in 0.0f..0.5f) {
                val negativeFraction = position * 2.0f
                val positiveFraction = 1.0f - negativeFraction

                scale =
                    if (!isMediumScaled) mMaxPageScale else (mMinPageScale + mCenterScaleBy) + (mCenterScaleBy * positiveFraction)
                translation =
                    (-(if (isMediumScaled) pageSubScaleEdgeOffset else pageMaxScaleEdgeOffset) +
                            centerPageScaleOffset) * negativeFraction
            } else if (position in 0.5f..1.0f) {
                val negativeFraction = (position - 0.5f) * 2.0f
                val positiveFraction = 1.0f - negativeFraction

                if (isMediumScaled) {
                    scale = mMinPageScale + (mCenterScaleBy * positiveFraction)
                    translation =
                        (-pageSubScaleEdgeOffset + centerPageScaleOffset) + ((-pageSize +
                                pageSubScaleByOffset + pageMaxScaleEdgeOffset + pageSubScaleEdgeOffset
                                - minPageScaleOffset - centerPageScaleOffset) * negativeFraction)
                } else {
                    scale = mMinPageScale + ((mMaxPageScale - mMinPageScale) * positiveFraction)
                    translation = (-pageMaxScaleEdgeOffset + centerPageScaleOffset) +
                            ((-pageSize + pageMaxScaleEdgeOffset + pageMinScaleEdgeOffset -
                                    minPageScaleOffset - centerPageScaleOffset) * negativeFraction)
                }
            } else if (mItemCount > MIN_CYCLE_COUNT && position > 1.0f && position <= 2.0f) {
                val negativeFraction = 1.0f + (position - 1.0f)
                val positiveFraction = 1.0f - negativeFraction

                scale = mMinPageScale
                translation = -(pageSize - pageMinScaleEdgeOffset + minPageScaleOffset) +
                        ((pageSize + pageMinScaleEdgeOffset - minPageScaleOffset) * positiveFraction)
            } else if (mItemCount < MIN_CYCLE_COUNT + 1 && mStackCount == 0 && position > 1.0f && position < 2.0f) {
                val fraction = 1.0f - position

                scale = mMinPageScale
                translation = -(pageSize - pageMinScaleEdgeOffset + minPageScaleOffset) +
                        ((pageSize * 2.0f - pageMinScaleOffset - minPageScaleOffset * 2.0f) * fraction)

                mStackCount++
            } else {
                // Reset values
                scale = mMinPageScale
                translation = 0.0f
            }

            // Scale page
            page.scaleX = scale
            page.scaleY = scale


            // Translate page
            page.translationX = translation

            var needBringToFront = false
            if (mItemCount == MIN_CYCLE_COUNT - 1) mIsLeftPageBringToFront = true

            when (mOuterPageScrolledState) {
                PageScrolledState.GOING_LEFT -> {
                    // Reset left page is bring
                    mIsLeftPageBringToFront = false
                    // Now we handle where we scroll in outer and inner left direction
                    if (mInnerPageScrolledState == PageScrolledState.GOING_LEFT) {
                        // This is another flag which detect if right was not bring to front
                        // and set positive flag
                        if (position > -0.5f && position <= 0.0f) {
                            if (!mIsRightPageBringToFront) {
                                mIsRightPageBringToFront = true
                                needBringToFront = true
                            }
                        } else if (position >= 0.0f && position < 0.5f) needBringToFront = true
                        else if (position > 0.5f && position < 1.0f && !mIsRightPageBringToFront && mViewPageable!!.childCount > MIN_CYCLE_COUNT) needBringToFront =
                            true
                    } else {
                        // We move to the right and detect if position if under half of path
                        if (mPageScrolledPositionOffset < 0.5f && position > -0.5f && position <= 0.0f) needBringToFront =
                            true
                    }
                }

                PageScrolledState.GOING_RIGHT -> {
                    // Reset right page is bring
                    mIsRightPageBringToFront = false
                    // Now we handle where we scroll in outer and inner right direction
                    if (mInnerPageScrolledState == PageScrolledState.GOING_RIGHT) {
                        // This is another flag which detect if left was not bring to front
                        // and set positive flag
                        if (position >= 0.0f && position < 0.5f) {
                            if (!mIsLeftPageBringToFront) {
                                mIsLeftPageBringToFront = true
                                needBringToFront = true
                            }
                        } else if (position > -0.5f && position <= 0.0f) needBringToFront = true
                        else if (position > -1.0f && position < -0.5f && !mIsLeftPageBringToFront && mViewPageable!!.childCount > MIN_CYCLE_COUNT) needBringToFront =
                            true
                    } else {
                        // We move to the left and detect if position if over half of path
                        if (mPageScrolledPositionOffset > 0.5f && position >= 0.0f && position < 0.5f) needBringToFront =
                            true
                    }
                }

                else -> {
                    // If is data set changed we need to hard reset page bring flags
                    if (mIsDataSetChanged) {
                        mIsLeftPageBringToFront = false
                        mIsRightPageBringToFront = false
                    } else {
                        // Detect different situations of is there a page was bring or
                        // we just need to bring it again to override drawing order

                        if (!mWasPlusOne && position == 1.0f) mWasPlusOne = true
                        else if (mWasPlusOne && position == -1.0f) mIsLeftPageBringToFront = true
                        else if ((!mWasPlusOne && position == -1.0f) ||
                            (mWasPlusOne && mIsLeftPageBringToFront && position == -2.0f)
                        ) mIsLeftPageBringToFront = false

                        if (!mWasMinusOne && position == -1.0f) mWasMinusOne = true
                        else if (mWasMinusOne && position == 1.0f) mIsRightPageBringToFront = true
                        else if ((!mWasMinusOne && position == 1.0f) ||
                            (mWasMinusOne && mIsRightPageBringToFront && position == 2.0f)
                        ) mIsRightPageBringToFront = false
                    }

                    // Always bring to front is center position
                    if (position == 0.0f) needBringToFront = true
                }
            }
            // Bring to front if needed
            if (needBringToFront) {
                page.bringToFront()
                mCastViewPageable.invalidate()
            }

            Log.i("sdfsdfsdfsd", "transformPage: $position")
            if (onInfiniteCyclePageTransformListener != null) onInfiniteCyclePageTransformListener!!.onPostTransform(
                page,
                position
            )
        }
    }

    // OnPageChangeListener which is retrieve info about scroll direction and scroll state
    protected val mInfinityCyclePageChangeListener: ViewPager.OnPageChangeListener =
        object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageScrolled(
                position: Int, positionOffset: Float, positionOffsetPixels: Int
            ) {
                // Reset stack count on each scroll offset
                mStackCount = 0

                // We need to rewrite states when is dragging and when setCurrentItem() from idle
                if (state != ViewPager.SCROLL_STATE_SETTLING || mIsInitialItem) {
                    // Detect first state from idle
                    if (mOuterPageScrolledState == PageScrolledState.IDLE && positionOffset > 0) {
                        mPageScrolledPosition = mViewPageable!!.currentItem.toFloat()
                        mOuterPageScrolledState =
                            if (position.toFloat() == mPageScrolledPosition) PageScrolledState.GOING_LEFT else PageScrolledState.GOING_RIGHT
                    }

                    // Rewrite scrolled state when switch to another edge
                    val goingRight = position.toFloat() == mPageScrolledPosition
                    if (mOuterPageScrolledState == PageScrolledState.GOING_LEFT && !goingRight) mOuterPageScrolledState =
                        PageScrolledState.GOING_RIGHT
                    else if (mOuterPageScrolledState == PageScrolledState.GOING_RIGHT && goingRight) mOuterPageScrolledState =
                        PageScrolledState.GOING_LEFT
                }

                // Rewrite inner dynamic scrolled state
                mInnerPageScrolledState =
                    if (mPageScrolledPositionOffset <= positionOffset) PageScrolledState.GOING_LEFT
                    else PageScrolledState.GOING_RIGHT
                mPageScrolledPositionOffset = positionOffset

                // Detect if is idle in pageScrolled() callback to transform pages last time
                if ((if (isSmallPositionOffset(positionOffset)) 0f else positionOffset) == 0f) {
                    // Reset states and flags on idle
                    //disableHardwareLayers()

                    mInnerPageScrolledState = PageScrolledState.IDLE
                    mOuterPageScrolledState = PageScrolledState.IDLE

                    mWasMinusOne = false
                    mWasPlusOne = false
                    mIsLeftPageBringToFront = false
                    mIsRightPageBringToFront = false

                    mIsInitialItem = false
                }
            }

            override fun onPageScrollStateChanged(states: Int) {
                state = states
            }
        }

    init {
        mViewPageable = viewPageable
        mCastViewPageable = viewPageable as View

        // Set default InfiniteViewPager
        mViewPageable?.apply {
            setPageTransformer(false, infinityCyclePageTransformer)
            addOnPageChangeListener(mInfinityCyclePageChangeListener)
            setClipChildren(DEFAULT_DISABLE_FLAG)
            setDrawingCacheEnabled(DEFAULT_DISABLE_FLAG)
            setWillNotCacheDrawing(DEFAULT_ENABLE_FLAG)
            setPageMargin(DEFAULT_PAGE_MARGIN)
            setOffscreenPageLimit(DEFAULT_OFFSCREEN_PAGE_LIMIT)
            setOverScrollMode(View.OVER_SCROLL_NEVER)
        }


        // Reset scroller and process attribute set
        resetScroller()
        processAttributeSet(attributeSet)
    }

    // Page scrolled state
    private enum class PageScrolledState {
        IDLE, GOING_LEFT, GOING_RIGHT
    }

    // Default spring interpolator
    private inner class SpringInterpolator : Interpolator {
        override fun getInterpolation(input: Float): Float {
            2f.pow(4.0f * input)
            return (2.0f.pow((-10.0f * input)) * sin((input - factor / 4.0f) * (2.0f * Math.PI) / factor) + 1.0f).toFloat()
        }

         val factor = 0.5f
    }

    companion object {
        // InfiniteCycleManager constants
        private const val MIN_CYCLE_COUNT = 3
        private const val MIN_POINTER_COUNT = 1

        // Default ViewPager constants and flags
        const val DEFAULT_OFFSCREEN_PAGE_LIMIT: Int = 2
        const val DEFAULT_PAGE_MARGIN: Int = 0
        const val DEFAULT_DISABLE_FLAG: Boolean = false
        const val DEFAULT_ENABLE_FLAG: Boolean = true

        // Default attributes constants
        private const val DEFAULT_MIN_SCALE = 0.55f
        private const val DEFAULT_MAX_SCALE = 0.8f
        private const val DEFAULT_MIN_PAGE_SCALE_OFFSET = 30
        private const val DEFAULT_CENTER_PAGE_SCALE_OFFSET = 50
        private const val DEFAULT_IS_MEDIUM_SCALED = true
        private const val DEFAULT_SCROLL_DURATION = 500
    }
}
