package com.zhang.openglrendervideo.openGl

import android.graphics.SurfaceTexture

/**
 * @Description: TODO
 * @Author: chao8
 * @Date: 2020-01-08 15:34:37
 * @Version: appVersionName, 2020-01-08
 * @LastEditors:
 * @LastEditTime: 2020-01-08 15:34:37
 * @Deprecated: false
 */
interface IDrawer{
    fun setAlpha(alpha:Float)
    fun setVideoSize(videoW: Int, videoH: Int)
    fun setWorldSize(worldW: Int, worldH: Int)
    fun draw()
    fun setTextureId(id: Int)
    fun release()
    //新增函数获取surfacetexture
    fun getSurfaceTexture(cb:(st:SurfaceTexture)->Unit){}
}