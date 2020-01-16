package com.zhang.openglrendervideo

import android.media.MediaFormat

/**
 * @Description: TODO
 * @Author: chao8
 * @Date: 2020-01-02 16:06:17
 * @Version: appVersionName, 2020-01-02
 * @LastEditors:
 * @LastEditTime: 2020-01-02 16:06:17
 * @Deprecated: false
 */
 interface  IDecoder  : Runnable {
    //暂停解码
    fun pause()
    // 继续解码
    fun goOn()
    //调转到哪个位置
    fun seekTo(pos:Long):Long
   //跳转某个位置并且播放
    fun seekAndPlay(pos: Long):Long
    //停止解码
    fun stop()
    //是否正在解码
    fun  isDecoding():Boolean
    //是否正在快进
    fun   isSeeking():Boolean
    //是否停止解码
    fun  isStop():Boolean
    //设置尺寸监听器
   fun setSizeListener(l:IDecoderProgress)
   //设置解码监听
    fun setStateListener(l:IDecoderStateListener)
    //获取视频宽度
    fun  getWidth() :Int
    //获取视频高度
    fun getHeight() :Int
    //获取视频长度
    fun getDuration():Long
    //获取当前时间戳
    fun getCurTimeStamp():Long
    // 获取视频旋转角度
    fun getRotationAngle():Int
    //获取音视频对应的宽度
    fun getMediaFormat():MediaFormat?
    //获取音视频对应的媒体轨道
    fun getTrack():Int
    //获取解码文件的路径
    fun getFilePath():String
    //无需音视频同步
    fun withoutSync():IDecoder

}