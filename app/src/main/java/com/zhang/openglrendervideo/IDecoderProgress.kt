package com.zhang.openglrendervideo

/**
 * @Description: TODO
 * @Author: chao8
 * @Date: 2020-01-02 16:26:01
 * @Version: appVersionName, 2020-01-02
 * @LastEditors:
 * @LastEditTime: 2020-01-02 16:26:01
 * @Deprecated: false
 */

interface IDecoderProgress{
    //视频宽高回调
 fun videoSizeChange(width:Int,height:Int,rotationAngle:Int)
   //视频播放进度回调
 fun videoProgressChange(pos:Long)
}