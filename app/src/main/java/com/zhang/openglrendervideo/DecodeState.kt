package com.zhang.openglrendervideo

/**
 * @Description: TODO
 * @Author: chao8
 * @Date: 2020-01-03 10:09:04
 * @Version: appVersionName, 2020-01-03
 * @LastEditors:
 * @LastEditTime: 2020-01-03 10:09:04
 * @Deprecated: false
 */
enum  class DecodeState {
    //开始状态
    START,
    //解码中
    DECODING,
    //暂停解码
    PAUSE,
    //正在快进
    SEEKING,
    //解码完成
    FINISH,
    //解码器释放
    STOP,
}