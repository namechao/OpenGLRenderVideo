package com.zhang.openglrendervideo.openGl

import android.graphics.SurfaceTexture
import android.opengl.*
import android.util.Log
import android.view.Surface
import java.lang.RuntimeException
import javax.microedition.khronos.egl.EGL

/**
 * @Description: TODO
 * @Author: chao8
 * @Date: 2020-01-14 15:26:01
 * @Version: appVersionName, 2020-01-14
 * @LastEditors:
 * @LastEditTime: 2020-01-14 15:26:01
 * @Deprecated: false
 */
const val FLAG_RECORDABLE = 0x01

const val EGL_RECORDABLE_ANDROID = 0x3142

class EGLCore {
    private val TAG="EGLCore"
    //egl相关变量
    private var mEGLDisplay:EGLDisplay=EGL14.EGL_NO_DISPLAY
    private var mEGLContext=EGL14.EGL_NO_CONTEXT
    private var mEGLConfig:EGLConfig?=null
    /*
    初始化eglcontext
     */
   fun  init(eglContext: EGLContext?, flags: Int ) {
        if (mEGLDisplay!=EGL14.EGL_NO_DISPLAY) throw RuntimeException("EGL already set up")
        var sharedContext=eglContext ?: EGL14.EGL_NO_CONTEXT
        //创建EGLDisplay
        mEGLDisplay=EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if(mEGLDisplay==EGL14.EGL_NO_DISPLAY) throw RuntimeException("Unable to get EGL14 display")
        //初始化EGlDispaly
        val version=IntArray(2)
         if (!EGL14.eglInitialize(mEGLDisplay,version,0,version,1)){
             mEGLDisplay=EGL14.EGL_NO_DISPLAY
             throw RuntimeException("unable to initialize EGL14")
         }
        //初始化eglcontext，eglconfig
        if (mEGLContext==EGL14.EGL_NO_CONTEXT){
          val config=getEGLConfig(flags,2) ?:throw RuntimeException("Unable to find a suitable EGLConfig")
          val attrs2List= intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION,2,EGL14.EGL_NONE)
          val context=EGL14.eglCreateContext(mEGLDisplay,config,
              sharedContext,attrs2List,0)
            mEGLConfig=config
            mEGLContext=context
        }

    }
   /*获取egl配置信息
     @params flags 初始化标记
     @params version 版本
    */
    private fun getEGLConfig(flags: Int,version:Int):EGLConfig?{
        var renderableType=EGL14.EGL_OPENGL_ES2_BIT
       if (version >= 3){
           //配置egl3
           renderableType =renderableType or EGLExt.EGL_OPENGL_ES3_BIT_KHR
       }
       //配置数组 主要配置RGBA的位数和深度
       // 俩一对 前边为key  后边为vaule
       //最后一个必须以EGL14.EGL_NONE
          val attrList= intArrayOf(
              EGL14.EGL_RED_SIZE,8,
              EGL14.EGL_GREEN_SIZE,8,
              EGL14.EGL_BLUE_SIZE,8,
              EGL14.EGL_ALPHA_SIZE,8,
              //EGL14.EGL_DEPTH_SIZE, 16,
              //EGL14.EGL_STENCIL_SIZE, 8,
              EGL14.EGL_RENDERABLE_TYPE,renderableType,
              EGL14.EGL_NONE,0,
              EGL14.EGL_NONE
          )
         if (flags and FLAG_RECORDABLE !=0){
             attrList[attrList.size-3]= EGL_RECORDABLE_ANDROID
             attrList[attrList.size-2]=1
         }
         val configs= arrayOfNulls<EGLConfig>(1)
         val numConfigs=IntArray(1)
         if (!EGL14.eglChooseConfig(mEGLDisplay,attrList,0,configs,0,configs.size,numConfigs,0)){
             Log.w(TAG, "Unable to find RGB8888 / $version EGLConfig")
             return null
         }
       return configs[0]
   }

    //创建可显示的渲染缓存

    fun createWindowSurface(surface:Any):EGLSurface{
        if (surface !is Surface && surface !is SurfaceTexture){
            throw RuntimeException("Invalid surface: $surface")
        }
        val surfaceAttr= intArrayOf(EGL14.EGL_NONE)
        val eglSurface=EGL14.eglCreateWindowSurface(mEGLDisplay,mEGLConfig,surface,surfaceAttr,0)
       if (eglSurface==null){
           throw RuntimeException("suface is null")
       }
      return eglSurface
    }
    //创建离屏渲染
    //@params 离屏窗口的宽高
  fun createOffScreenSurface(width:Int,height:Int):EGLSurface{
     val surfaceAttr= intArrayOf( EGL14.EGL_WIDTH,width,EGL14.EGL_HEIGHT,height,EGL14.EGL_NONE)
     val eglSurface=EGL14.eglCreatePbufferSurface(mEGLDisplay,mEGLConfig,surfaceAttr,0)
        if (eglSurface == null) {
            throw RuntimeException("Surface was null")
        }

        return eglSurface
    }
    //将当前线程与上下文进行绑定
    fun makerCurrent(eglSurface: EGLSurface){
        if (mEGLDisplay==EGL14.EGL_NO_DISPLAY){
            throw RuntimeException("请先初始化EGLSurface")
        }
        if (!EGL14.eglMakeCurrent(mEGLDisplay,eglSurface,eglSurface,mEGLContext)){
            throw RuntimeException("makeCurrent(eglSurface) failed")
        }

    }
    //将当前线程与上下文进行绑定
    fun makerCurrent(drawSurface: EGLSurface,readSurface: EGLSurface){
        if (mEGLDisplay==EGL14.EGL_NO_DISPLAY){
            throw RuntimeException("请先初始化EGLSurface")
        }
        if (!EGL14.eglMakeCurrent(mEGLDisplay,drawSurface,readSurface,mEGLContext)){
            throw RuntimeException("eglMakeCurrent(draw,read) failed")
        }

    }
         //将缓存数据发送到显示设备进行显示
    fun swapBuffers(eglSurface: EGLSurface):Boolean{
     return EGL14.eglSwapBuffers(mEGLDisplay,eglSurface)
         }
    //设置当前帧时间单位纳秒
    fun setPresentationTime(eglSurface: EGLSurface,nsecs:Long){
      EGLExt.eglPresentationTimeANDROID(mEGLDisplay,eglSurface,nsecs)
    }
    //销毁egls并且解除绑定
    fun destroySurface(eglSurface: EGLSurface){
        EGL14.eglMakeCurrent(mEGLDisplay,EGL14.EGL_NO_SURFACE,EGL14.EGL_NO_SURFACE,EGL14.EGL_NO_CONTEXT)
        EGL14.eglDestroySurface(mEGLDisplay,eglSurface)
    }
    //释放资源
    fun  release(){
        if (mEGLDisplay!=EGL14.EGL_NO_DISPLAY){
            EGL14.eglMakeCurrent(mEGLDisplay,EGL14.EGL_NO_SURFACE,EGL14.EGL_NO_SURFACE,EGL14.EGL_NO_CONTEXT)
            EGL14.eglDestroyContext(mEGLDisplay,mEGLContext)
            EGL14.eglReleaseThread()
            EGL14.eglTerminate(mEGLDisplay)
        }
        mEGLDisplay=EGL14.EGL_NO_DISPLAY
        mEGLContext=EGL14.EGL_NO_CONTEXT
        mEGLConfig=null
    }
}