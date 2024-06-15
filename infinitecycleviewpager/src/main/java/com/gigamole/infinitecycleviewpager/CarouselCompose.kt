package com.gigamole.infinitecycleviewpager

import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.viewpager.widget.ViewPager

@Composable
fun CarouselCompose(
    modifier: Modifier,
    list: List<Any>,
    onChangedIndex: (Int) -> Unit,
    isAutoScroll: Boolean = true,
    itemDuration: Int = 1800,
    content: @Composable (item: Any) -> Unit,
) {

    AndroidView(factory = { ctx ->

        //  Initialize a View or View hierarchy here

        HorizontalInfiniteCycleViewPager(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            adapter =
                HorizontalPagerAdapterCompose(context, list) { composeView, item ->
                    composeView.setContent {
                        content(item)
                    }
                }

            pageDuration = itemDuration
            if (isAutoScroll) {
                startAutoScroll(true)
            }
            scrollDuration = if (isAutoScrolling()) 600 else 400

            interpolator = AnimationUtils.loadInterpolator(
                context,
                android.R.anim.accelerate_decelerate_interpolator
            )
            isMediumScaled = true
            maxPageScale = 0.90f
            minPageScale = 0.7f
            centerPageScaleOffset = 30f
            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int,
                ) {
                }

                override fun onPageSelected(position: Int) {
                    onChangedIndex(position % list.size)
                }

                override fun onPageScrollStateChanged(state: Int) {}

            })
        }

    }, modifier = modifier)
}