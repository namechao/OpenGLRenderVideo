package com.zhang.openglrendervideo.openGl

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @Description: TODO
 * @Author: chao8
 * @Date: 2020-01-08 16:52:18
 * @Version: appVersionName, 2020-01-08
 * @LastEditors:
 * @LastEditTime: 2020-01-08 16:52:18
 * @Deprecated: false
 */
class SimpleRender() :GLSurfaceView.Renderer{

    private val drawers = mutableListOf<IDrawer>()
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
           GLES20.glClearColor(0f,0f,0f,0f)
              //开启混合
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        val textureIds = OpengGlTools.creatTextureId(drawers.size)
        for ((idx, drawer) in drawers.withIndex()) {
            drawer.setTextureId(textureIds[idx])
        }
    }


    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        for (drawer in drawers) {
            drawer.setWorldSize(width, height)
        }
    }
    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        drawers.forEach {
            it.draw()
        }
    }

    fun addDrawer(drawer: IDrawer) {
        drawers.add(drawer)
    }
}