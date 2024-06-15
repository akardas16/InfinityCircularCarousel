package com.gigamole.infinitecycleviewpager

import android.content.Context
import android.view.animation.Interpolator
import android.widget.Scroller

/**
 * Created by GIGAMOLE on 8/4/16.
 */
// Custom scroller for setting own snap duration and interpolator
internal class InfiniteCycleScroller : Scroller {
    private var mDuration = 0

    constructor(context: Context?) : super(context)

    constructor(context: Context?, interpolator: Interpolator?) : super(context, interpolator)

    constructor(context: Context?, interpolator: Interpolator?, flywheel: Boolean) : super(
        context,
        interpolator,
        flywheel
    )

    fun setDuration(duration: Int) {
        mDuration = duration
    }

    override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
        super.startScroll(startX, startY, dx, dy, mDuration)
    }

    override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int) {
        super.startScroll(startX, startY, dx, dy, mDuration)
    }
}
