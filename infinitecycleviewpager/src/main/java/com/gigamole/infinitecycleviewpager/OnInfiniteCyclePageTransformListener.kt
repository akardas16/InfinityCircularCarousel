package com.gigamole.infinitecycleviewpager

import android.view.View

/**
 * Created by GIGAMOLE on 7/27/16.
 */
interface OnInfiniteCyclePageTransformListener {
    fun onPreTransform(page: View?, position: Float)

    fun onPostTransform(page: View?, position: Float)
}