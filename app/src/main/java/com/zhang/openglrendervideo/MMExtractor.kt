package com.zhang.openglrendervideo

import android.media.MediaExtractor
import android.media.MediaFormat
import java.lang.Exception
import java.nio.ByteBuffer

/**
 * @Description: TODO
 * @Author: chao8
 * @Date: 2020-01-06 14:28:40
 * @Version: appVersionName, 2020-01-06
 * @LastEditors:
 * @LastEditTime: 2020-01-06 14:28:40
 * @Deprecated: false
 */

class MMExtractor(path:String){
  //音视频分离器
    private var mExtractor:MediaExtractor?=null
  //音频通道索引
    private var mAudioTrack=-1
    //视频通道索引
    private  var mVideoTrack=-1
    //当前时间戳
    private  var mCurSampleTime:Long=0
    /**当前帧标志*/
    private var mCurSampleFlag: Int = 0
    //开始解码时间点
    private var mStartPos:Long =0
    init {
        try {
            mExtractor= MediaExtractor()
            mExtractor?.setDataSource(path)
        }catch (e:Exception){
            e.printStackTrace()
        }

    }
//获取视频格式参数
    fun getVideoFormat():MediaFormat?{
    //获取视频多媒体格式
      for (i in 0 until mExtractor!!.trackCount){
        val  mediaFormat=mExtractor!!.getTrackFormat(i)
          val mime=mediaFormat.getString(MediaFormat.KEY_MIME)
          if (mime.startsWith("video/")){
              mVideoTrack=i
              break
          }
      }
      return if (mVideoTrack>=0) mExtractor!!.getTrackFormat(mVideoTrack) else null
     }
    //获取视频格式参数
    fun getAudioFormat():MediaFormat?{
        //获取音频多媒体格式
        for (i in 0 until mExtractor!!.trackCount){
            val  mediaFormat=mExtractor!!.getTrackFormat(i)
            val mime=mediaFormat.getString(MediaFormat.KEY_MIME)
            if (mime.startsWith("audio/")){
                mAudioTrack=i
                break
            }
        }
        return if (mAudioTrack>=0) mExtractor!!.getTrackFormat(mAudioTrack) else null
    }
    //读取视频数据
    fun readBuffer(byteBuffer: ByteBuffer):Int{
       byteBuffer.clear()
        selectSourceTrack()
        var readSamplerCount=mExtractor!!.readSampleData(byteBuffer,0)
        if (readSamplerCount<0){
            return  -1
        }
          mCurSampleTime=mExtractor!!.sampleTime
        mCurSampleFlag = mExtractor!!.sampleFlags
        mExtractor!!.advance()
        return  readSamplerCount
    }

    //选择通道
    fun selectSourceTrack(){
        if (mVideoTrack>=0){
            mExtractor!!.selectTrack(mVideoTrack)
        }else if(mAudioTrack>=0){
            mExtractor!!.selectTrack(mAudioTrack)
        }
    }
    //跳到指定位置并且返回实际帧时间
    fun seek(pos:Long):Long{
        mExtractor!!.seekTo(pos,MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
        return  mExtractor!!.sampleTime
    }
    //停止读取数据
    fun stop(){
        mExtractor!!.release()
        mExtractor=null
    }
    fun getVideoTrack():Int{
        return mVideoTrack
    }
    fun getAudioTrack():Int{
        return mAudioTrack
    }
    //设置开始位置
    fun setStartPos(pos:Long){
        mStartPos=pos
    }
    //获取当前帧时间错
    fun getCurrentTimestamp():Long{
        return mCurSampleTime
    }
    fun getSampleFlag(): Int {
        return mCurSampleFlag
    }
}