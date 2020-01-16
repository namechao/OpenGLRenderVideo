package com.zhang.openglrendervideo

import android.media.*
import java.lang.Exception
import java.nio.ByteBuffer
import java.nio.ShortBuffer

/**
 * @Description: TODO
 * @Author: chao8
 * @Date: 2020-01-06 17:37:31
 * @Version: appVersionName, 2020-01-06
 * @LastEditors:
 * @LastEditTime: 2020-01-06 17:37:31
 * @Deprecated: false
 */
class AudioDecoder (path:String):BaseDecoder(path){
    //采样率
    private var mSampleRate=-1
    //声音通道数量
    private var mChannels=1
    //PCM采样位数
    private var mPCMEncodeBit= AudioFormat.ENCODING_PCM_16BIT
    //音乐播放器
    private var mAudioTrack:AudioTrack?=null
    //音频数据缓存
    private var mAudioOutTempBuf:ShortArray?=null

    override fun check(): Boolean {
        return true
    }

    override fun initExtractor(path: String): IExtractor {
       return  AudioExtractor(path)
    }

    override fun initSpecParams(format: MediaFormat) {
       try {
           mChannels=format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
           mSampleRate=format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
           mPCMEncodeBit=if (format.containsKey(MediaFormat.KEY_PCM_ENCODING)){
               format.getInteger(MediaFormat.KEY_PCM_ENCODING)
           }else {
               AudioFormat.ENCODING_PCM_16BIT
           }
       }catch (e:Exception){

       }
    }

    override fun initRender(): Boolean {
 val channel=if (mChannels==1){
     //单声道
     AudioFormat.CHANNEL_OUT_MONO
 }else{
     AudioFormat.CHANNEL_OUT_STEREO
 }
        val minBufferSize=AudioTrack.getMinBufferSize(mSampleRate,channel,mPCMEncodeBit)
        mAudioOutTempBuf= ShortArray(minBufferSize/2)
        mAudioTrack= AudioTrack(AudioManager.STREAM_MUSIC,mSampleRate,channel,mPCMEncodeBit,minBufferSize,AudioTrack.MODE_STREAM)
        mAudioTrack?.play()
return true
    }

    override fun render(outputBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
      if (mAudioOutTempBuf!!.size<bufferInfo.size/2){
          mAudioOutTempBuf= ShortArray(bufferInfo.size/2)
      }
        outputBuffer.position(0)
        outputBuffer.asShortBuffer().get(mAudioOutTempBuf,0,bufferInfo.size/2)
        mAudioTrack?.write(mAudioOutTempBuf!!,0,bufferInfo.size/2)

    }

    override fun configCodec(mediaCodec: MediaCodec, mediaFormat: MediaFormat): Boolean {
        mediaCodec.configure(mediaFormat,null,null,0)
        return true
    }

    override fun doneDecode() {
        mAudioTrack?.stop()
        mAudioTrack?.release()
    }

    override fun stop() {
      mAudioTrack?.stop()
    }

}