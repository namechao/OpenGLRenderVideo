package com.zhang.openglrendervideo

import android.media.MediaCodec
import android.media.MediaFormat
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.nio.ByteBuffer

/**
 * @Description: TODO
 * @Author: chao8
 * @Date: 2020-01-06 16:40:33
 * @Version: appVersionName, 2020-01-06
 * @LastEditors:
 * @LastEditTime: 2020-01-06 16:40:33
 * @Deprecated: false
 */
class VideoDecoder (  path:String,  mSurfaceView:SurfaceView?,  mSurface: Surface?):BaseDecoder(path){
    private val mSurfaceView = mSurfaceView
    private var mSurface = mSurface

    override fun check(): Boolean {
        if (mSurfaceView==null&&mSurface==null)
        {
            return false
        }
        return true
    }

    override fun initExtractor(path: String): IExtractor {
       return VideoExtractor(path)
    }

    override fun initSpecParams(format: MediaFormat) {

    }
    override fun configCodec(mediaCodec: MediaCodec, mediaFormat: MediaFormat): Boolean {
         if (mSurface!=null){
             mediaCodec.configure(mediaFormat,mSurface,null,0)
             notifyDecode()
         }else if (mSurfaceView?.holder?.surface!=null){
             mSurface=mSurfaceView?.holder?.surface
             configCodec(mediaCodec,mediaFormat)
         }else{
             mSurfaceView?.holder?.addCallback(object :SurfaceHolder.Callback2{
                 override fun surfaceRedrawNeeded(holder: SurfaceHolder?) {

                 }

                 override fun surfaceChanged(
                     holder: SurfaceHolder?,
                     format: Int,
                     width: Int,
                     height: Int
                 ) {

                 }

                 override fun surfaceDestroyed(holder: SurfaceHolder?) {

                 }

                 override fun surfaceCreated(holder: SurfaceHolder?) {
                     mSurface=holder?.surface
                     configCodec(mediaCodec,mediaFormat)
                 }

             })
             return false
         }
        return true
    }
    override fun initRender(): Boolean {
     return true
    }

    override fun render(outputBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {

    }



    override fun doneDecode() {

    }

    override fun stop() {

    }

}