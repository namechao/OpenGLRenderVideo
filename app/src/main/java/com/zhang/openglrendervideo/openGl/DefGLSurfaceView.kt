package com.zhang.openglrendervideo.openGl

import android.content.Context
import android.graphics.PointF
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent

/**
 * @Description: TODO
 * @Author: chao8
 * @Date: 2020-01-10 16:31:40
 * @Version: appVersionName, 2020-01-10
 * @LastEditors:
 * @LastEditTime: 2020-01-10 16:31:40
 * @Deprecated: false
 */
class DefGLSurfaceView : GLSurfaceView {

    constructor(context: Context): super(context)

    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    private var mPrePoint = PointF()

    private var mDrawer: VideoDrawer? = null

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mPrePoint.x = event.x
                mPrePoint.y = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = (event.x - mPrePoint.x) / width
                val dy = (event.y - mPrePoint.y) / height
                mDrawer?.translate(dx, dy)
                mPrePoint.x = event.x
                mPrePoint.y = event.y
            }
        }
        return true
    }

    fun addDrawer(drawer: VideoDrawer) {
        mDrawer = drawer
    }
}
