package com.zhang.openglrendervideo

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Environment
import android.util.Log
import java.lang.Exception
import java.nio.ByteBuffer

/**
 * @Description: TODO
 * @Author: chao8
 * @Date: 2020-01-07 14:17:56
 * @Version: appVersionName, 2020-01-07
 * @LastEditors:
 * @LastEditTime: 2020-01-07 14:17:56
 * @Deprecated: false
 */
class MMuxer {
    private val TAG="mmuxer"
    //文件路径
    private var mPath:String=""
    //系统封装器
    private var mMediaMuxer:MediaMuxer?=null

    private var mVideoTrackIndex=-1

    private var mAudioTrackIndex=-1

    private var mIsVideoTrackAdd=false

    private var mIsAudioTrackAdd=false

    private var mIsAudioEnd = false
    private var mIsVideoEnd = false

    private var mIsStart=false
    private var mStateListener: IMuxerStateListener?  = null
    init {
     val fileName="NewMP4.mp4"
     val filePath=Environment.getExternalStorageDirectory().absolutePath.toString()+"/"
        mPath=filePath+fileName
        mMediaMuxer= MediaMuxer(mPath,MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

    }
   //添加视频轨道
    fun  addVideoTrack(mediaFormat: MediaFormat){
        if (mIsVideoTrackAdd) return
        if (mMediaMuxer!=null){
        mVideoTrackIndex=try {
           mMediaMuxer!!.addTrack(mediaFormat)
        }catch (e:Exception){
            e.printStackTrace()
            return
        }
          Log.i(TAG,"添加视频轨道")
            mIsVideoTrackAdd=true
            startMuxer()
        }
    }

    //添加音频轨道
    fun addAudioTrack(mediaFormat: MediaFormat){
     if (mIsAudioTrackAdd)return
     if (mMediaMuxer!=null){
         mAudioTrackIndex=try {
             mMediaMuxer!!.addTrack(mediaFormat)
         }catch (e:Exception){
             e.printStackTrace()
             return
         }
         Log.i(TAG,"添加音频轨道")
         mIsAudioTrackAdd=true
         startMuxer()

     }
 }

    fun setNoVideo(){
        if (mIsVideoTrackAdd) return
        mIsAudioTrackAdd=true
        mIsAudioTrackAdd=true
        startMuxer()
    }
    fun setNoAudio(){
        if (mIsAudioTrackAdd) return
        mIsAudioTrackAdd=true
        mIsAudioTrackAdd=true
        startMuxer()
    }
    //写入视频数据数据
   fun witerVideoData(byteBuffer: ByteBuffer,bufferInfo: MediaCodec.BufferInfo){
       if (mIsStart){
           mMediaMuxer?.writeSampleData(mVideoTrackIndex,byteBuffer,bufferInfo)
       }
   }
    //写入音频数据数据
    fun witerAudioData(byteBuffer: ByteBuffer,bufferInfo: MediaCodec.BufferInfo){
        if (mIsStart){
            mMediaMuxer?.writeSampleData(mAudioTrackIndex,byteBuffer,bufferInfo)
        }
    }

    //启动封装器
    private fun startMuxer() {
    if (mIsAudioTrackAdd&&mIsVideoTrackAdd){
        mMediaMuxer?.start()
        mIsStart=true
        mStateListener?.onMuxerStart()
        Log.i(TAG,"启动封装器")
    }
    }
    //释放视频轨道
    fun releaseVideoTrack(){
       mIsVideoEnd=true
        release()
    }
    //释放音频轨道
    fun releaseAudioTrack(){
        mIsAudioEnd=true
        release()
    }
    //释放封装器
     fun release() {
     if (mIsVideoEnd&&mIsAudioEnd){
         mIsAudioTrackAdd=false
         mIsVideoTrackAdd=false
         try {
             mMediaMuxer?.stop()
             mMediaMuxer?.release()
             mMediaMuxer==null
             mIsStart=false
             Log.i(TAG,"退出封装器")
         }catch (e:Exception){
             e.printStackTrace()
         }finally {
             mStateListener?.onMuxerFinish()
         }

     }
    }

    fun setStateListener(l: IMuxerStateListener) {
        this.mStateListener = l
    }

    interface IMuxerStateListener {
        fun onMuxerStart() {}
        fun onMuxerFinish() {}
    }
}