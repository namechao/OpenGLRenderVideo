package com.zhang.openglrendervideo.openGl

import android.opengl.GLES20
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import java.lang.ref.WeakReference
import java.util.*

/**
 * @Description: TODO
 * @Author: chao8
 * @Date: 2020-01-15 11:07:29
 * @Version: appVersionName, 2020-01-15
 * @LastEditors:
 * @LastEditTime: 2020-01-15 11:07:29
 * @Deprecated: false
 */

class CustomerGLRenderer :SurfaceHolder.Callback{
    //创建OpenGL渲染线程
    private val mThread=RenderThread()
    private var mSurfaceView:WeakReference<SurfaceView>?=null
    private var mSurface:Surface?=null
    private val mDrawers= mutableListOf<IDrawer>()
    init {
        mThread.start()
    }

    fun  setSurface(surface: SurfaceView){
      mSurfaceView= WeakReference(surface)
      surface.holder.addCallback(this)
        surface.addOnAttachStateChangeListener(object :View.OnAttachStateChangeListener{
            override fun onViewDetachedFromWindow(v: View?) {
                mThread.onSurfaceStop()
                mSurface = null
            }

            override fun onViewAttachedToWindow(v: View?) {
            }
        })
    }
    fun setSurface(surface: Surface, width: Int, height: Int) {
        mSurface = surface
        mThread.onSurfaceCreate()
        mThread.onSurfaceChange(width, height)
    }
    //添加绘制器
    fun addDrawer( drawer: IDrawer){
        mDrawers.add(drawer)
    }
    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        mThread.onSurfaceChange(width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        mThread.onSurfaceDestroy()
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        mSurface=holder?.surface
        mThread.onSurfaceCreate()
    }

    inner class RenderThread:Thread(){
        //渲染状态
        private var mState =RenderState.NO_SURFACE
        private var mEGLSurface:EGLSurfaceHolder?=null
        //是否绑定了eglsurface
        private  var mHaveBindEglContext=false
        //是否新建国egl上下文
        private var mNeverCreateEGLContext=true
        private var mWidth=0
        private var mHeight=0
        private val mWaitLock=Object()
        private var mCurTimestamp = 0L

        private var mLastTimestamp = 0L

        private var mRenderMode = RenderMode.RENDER_WHEN_DIRTY
     //线程等待与解锁
        private fun holdOn(){
         synchronized(mWaitLock){
             mWaitLock.wait()
         }
     }
       private fun notifyGo(){
           synchronized(mWaitLock) {
               mWaitLock.notify()
           }
       }

        fun setRenderMode(mode:RenderMode){
            mRenderMode=mode
        }
        //surface创建转发函数
       fun onSurfaceCreate(){
            mState=RenderState.FRESH_SURFACE
            notifyGo()
        }
       fun onSurfaceChange(width: Int,height: Int){
           mWidth=width
           mHeight=height
           mState=RenderState.SURFACE_CHANGE
           notifyGo()
       }
        fun onSurfaceDestroy(){
            mState=RenderState.SURFACE_DESTROY
            notifyGo()
        }
        fun onSurfaceStop(){
            mState=RenderState.STOP
            notifyGo()
        }
       fun  notifySwap(timesUs:Long){
           synchronized(mCurTimestamp){
               mCurTimestamp=timesUs
           }
           notifyGo()
       }
        override fun run() {
            //【1】初始化egl
            initEGL()
            while (true){
                when(mState){
                    RenderState.FRESH_SURFACE->{
                        //【2】使用surface 初始化eglsurface并且绑定上下文
                        createEGLSurfaceFirst()
                        holdOn()
                    }
                    RenderState.SURFACE_CHANGE->{
                        createEGLSurfaceFirst()
                        //【3】初始化世界坐标系
                        GLES20.glViewport(0,0, mWidth,mHeight)
                        configWordSize()
                        mState=RenderState.RENDERING
                    }
                    RenderState.RENDERING->{
                      //【4】进入循环渲染
                        render()
                    }
                    RenderState.SURFACE_DESTROY->{
                        //【5】销毁eglsuface并且解绑上下文
                       destroyEGLSurface()
                        mState=RenderState.NO_SURFACE
                    }
                    RenderState.STOP->{
                         //【6】释放所有资源
                        releaseEGL()
                        return
                    }
                    else ->{
                        holdOn()
                    }

                }
                sleep(20)
            }
        }
        private fun createEGLSurfaceFirst() {
                   if (!mHaveBindEglContext){
                       mHaveBindEglContext=true
                       createEGLSurface()
                       if (mNeverCreateEGLContext){
                           mNeverCreateEGLContext=false
                           GLES20.glClearColor(0f,0f,0f,0f)
                           //开启半透明
                           GLES20.glEnable(GLES20.GL_BLEND)
                           GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
                           generateTextureID()
                       }
                   }
        }

        private fun generateTextureID() {
          val textureId=OpengGlTools.creatTextureId(mDrawers.size)
            for ((idx,drawer) in mDrawers.withIndex()){
                drawer.setTextureId(textureId[idx])
            }
        }

        private fun createEGLSurface() {
          mEGLSurface?.createEGLSurface(mSurface)
          mEGLSurface?.makeCurrent()
        }

        private fun releaseEGL() {
           mEGLSurface?.release()
        }

        private fun destroyEGLSurface() {
         mEGLSurface?.destroyEGLSurface()
            mHaveBindEglContext=false
        }

        private fun render() {
            val render = if ( mRenderMode==RenderMode.RENDER_WHEN_DIRTY){
                true
            }else{
                synchronized(mCurTimestamp){
                    if (mCurTimestamp>mLastTimestamp){
                        mLastTimestamp= mCurTimestamp
                        true
                    }else{
                        false
                    }
                }

            }
            if (render) {
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
                mDrawers.forEach { it.draw() }
                mEGLSurface?.setTimeStamp(mCurTimestamp)
                mEGLSurface?.swapBuffers()
            }
        }

        private fun configWordSize() {
            mDrawers.forEach{it.setWorldSize(mWidth,mHeight)}
        }


        private fun initEGL() {
            mEGLSurface= EGLSurfaceHolder()
            mEGLSurface?.init(null,EGL_RECORDABLE_ANDROID)
        }
    }


   enum class RenderState{
    NO_SURFACE,//没有有效的surface
    FRESH_SURFACE,//持有一个未初始化的新surface
    SURFACE_CHANGE,//surface的大小尺寸发生变化
    RENDERING,//正在渲染中
    SURFACE_DESTROY,//可用的surface 被销毁
    STOP   //停止绘制
   }
    enum class RenderMode {
        RENDER_CONTINUOUSLY,
        RENDER_WHEN_DIRTY
    }
}