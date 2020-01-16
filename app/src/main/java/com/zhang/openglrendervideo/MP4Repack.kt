package com.zhang.openglrendervideo

import android.media.MediaCodec
import android.util.Log
import java.nio.ByteBuffer

/**
 * @Description: TODO
 * @Author: chao8
 * @Date: 2020-01-07 14:58:37
 * @Version: appVersionName, 2020-01-07
 * @LastEditors:
 * @LastEditTime: 2020-01-07 14:58:37
 * @Deprecated: false
 */
class MP4Repack(path :String) {
    private val TAG = "MP4Repack"
    private val mAExtractor=AudioExtractor(path)
    private val mVExtractor=VideoExtractor(path)
    private val mMuxer=MMuxer()
  fun start(){
      val audioFormat=mAExtractor.getMediaFormat()
      val videoFormat=mVExtractor.getMediaFormat()
      if (audioFormat!=null){
        mMuxer.addAudioTrack(audioFormat)
         }else{
          mMuxer.setNoAudio()
      }
      if (videoFormat!=null){
          mMuxer.addVideoTrack(videoFormat)
      }else{
          mMuxer.setNoVideo()
      }
     Thread{
      val  buffer=ByteBuffer.allocate(500*1024)
      val bufferInfo=MediaCodec.BufferInfo()
       if (audioFormat !=null){
         var  size=mAExtractor.readBuffer(buffer)
         while (size>0){
             bufferInfo.set(0,size,mAExtractor.getCurrentTimestamp(),mAExtractor.getSampleFlag())
             mMuxer.witerAudioData(buffer,bufferInfo)
             size=mAExtractor.readBuffer(buffer)
         }
         }
         if (videoFormat !=null){
             var  size=mVExtractor.readBuffer(buffer)
             while (size>0){
                 bufferInfo.set(0,size,mVExtractor.getCurrentTimestamp(),mVExtractor.getSampleFlag())
                 mMuxer.witerVideoData(buffer,bufferInfo)
                 size=mVExtractor.readBuffer(buffer)
             }
         }
         mVExtractor.stop()
         mAExtractor.stop()
         mMuxer.releaseAudioTrack()
         mMuxer.releaseVideoTrack()
         Log.i(TAG,"已生成mp4包")
     }.start()
  }



}