package com.zhang.openglrendervideo

import android.media.MediaFormat
import java.nio.ByteBuffer

/**
 * @Description: TODO
 * @Author: chao8
 * @Date: 2020-01-03 10:08:29
 * @Version: appVersionName, 2020-01-03
 * @LastEditors:
 * @LastEditTime: 2020-01-03 10:08:29
 * @Deprecated: false
 */
interface IExtractor{
    //获取音视频格式
    fun  getMediaFormat():MediaFormat?
    //读取音视频数据
    fun readBuffer(byteBuffer: ByteBuffer):Int
    //获取当前帧时间
    fun getCurrentTimestamp():Long
    // 跳到指定位置并返回指定时间
    fun  seek(pos:Long):Long
    //设置开始位置
    fun setStartPos(pos: Long)
    //停止读取数据
    fun stop()
    fun getSampleFlag(): Int
}