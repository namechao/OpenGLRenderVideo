package com.zhang.openglrendervideo

/**
 * @Description: TODO
 * @Author: chao8
 * @Date: 2020-01-02 16:34:17
 * @Version: appVersionName, 2020-01-02
 * @LastEditors:
 * @LastEditTime: 2020-01-02 16:34:17
 * @Deprecated: false
 */
interface IDecoderStateListener{
    fun  decoderPrepare(decoderJob: BaseDecoder)
    fun  decoderReady(decoderJob: BaseDecoder)
    fun  decoderRunning(decoderJob: BaseDecoder)
    fun  decoderPause(decoderJob: BaseDecoder)
    fun  decoderOneFrame(decoderJob: BaseDecoder,frame:Frame)
    fun  decoderFinish(decoderJob: BaseDecoder)
    fun  decoderDestroy(decoderJob: BaseDecoder)
    fun  decoderError(decoderJob: BaseDecoder,msg:String)

}