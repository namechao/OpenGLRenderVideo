package com.zhang.openglrendervideo.openGl

import android.opengl.EGLContext
import android.opengl.EGLSurface
import java.util.*

/**
 * @Description: TODO
 * @Author: chao8
 * @Date: 2020-01-15 10:27:19
 * @Version: appVersionName, 2020-01-15
 * @LastEditors:
 * @LastEditTime: 2020-01-15 10:27:19
 * @Deprecated: false
 */
class EGLSurfaceHolder {
    private val TAG = "EGLSurfaceHolder"
    private lateinit var mEGLCore:EGLCore
    private var mEGLSurface:EGLSurface?=null
    fun  init( shareContex:EGLContext?=null,flags:Int){
        mEGLCore= EGLCore()
        mEGLCore.init(shareContex,flags)
    }
    //创建eglsurface
    fun createEGLSurface(surface:Any?,width: Int =-1,height:Int =-1){
        if (surface!=null){
            mEGLCore.createWindowSurface(surface)
        }else{
            mEGLCore.createOffScreenSurface(width,height)
        }
    }
    fun makeCurrent(){
        mEGLSurface?.let {  mEGLCore.makerCurrent(it) }

    }
    fun swapBuffers(){
        mEGLSurface?.let { mEGLCore.swapBuffers(it) }

    }

    fun setTimeStamp(timer: Long){
        mEGLSurface?.let { mEGLCore.setPresentationTime(it,timer*1000) }
    }
    fun destroyEGLSurface(){
       mEGLSurface?.let {
           mEGLCore.destroySurface(it)
           mEGLSurface=null
       }
    }
    fun release(){
        mEGLCore.release()
    }
}