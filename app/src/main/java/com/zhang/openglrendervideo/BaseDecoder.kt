package com.zhang.openglrendervideo

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.lang.Exception
import java.nio.ByteBuffer
import java.util.*

/**
 * @Description: TODO
 * @Author: chao8
 * @Date: 2020-01-02 17:00:30
 * @Version: appVersionName, 2020-01-02
 * @LastEditors:
 * @LastEditTime: 2020-01-02 17:00:30
 * @Deprecated: false
 */
abstract class BaseDecoder(private  val  mFilePath:String):IDecoder{
    private val TAG = "BaseDecoder"
    //-----线程相关---
    //解码器是否在运行
    private var  mIsRunning=true
    //线程等待锁
    private val mLock=Object()
    //线程是否可以解码
    private var mReadyForDecode=false
    //------解码相关变量-----
    //音视频解码器
    protected var mCodec:MediaCodec?=null
    //音视频数据读取器
    protected var mExtractor:IExtractor?=null
    //解码输入缓存区
    protected var mInputBuffers:Array<ByteBuffer>?=null
    //解码输出缓存区
    protected var mOutputBuffers:Array<ByteBuffer>?=null
    //解码数据信息
    private var mBufferInfo=MediaCodec.BufferInfo()
    private var mState =DecodeState.STOP
    private var mStateListener:IDecoderStateListener?=null
    //流数据是否结束
    private var mIsEOS=false
    protected var mVideoWidth=0
    protected var mVideoHeight=0
    private var mDuration:Long=0
    private var mEndPos:Long=0
    /**
     * 开始解码时间，用于音视频同步
     */
    private var mStartTimeForSync = -1L
    // 是否需要音视频渲染同步
    private var mSyncRender = true
    //-----解码流程-----

    override fun run() {
    mState=DecodeState.START
    mStateListener?.decoderPrepare(this)
   //初始化并且启动解码器
        if (!init()) return

    }

    private fun init(): Boolean {
     if (mFilePath.isEmpty()||!File(mFilePath).exists()){
         Log.w(TAG,"文件路径为空")
         mStateListener?.decoderError(this,"文件路径为空")
         return false
     }
        if (!check()) return false
        mExtractor=initExtractor(mFilePath)
        if (mExtractor==null||mExtractor!!.getMediaFormat()==null){
        Log.w(TAG,"无法解析文件")
            return  false
        }
        if (!initParams()) return false
        if (!initRender()) return false
        if (!initCodec()) return false
        Log.i(TAG, "开始解码")
        try {
            while (mIsRunning){
               if (mState!=DecodeState.START&&mState!=DecodeState.DECODING&&mState!=DecodeState.SEEKING){
                   Log.i(TAG,"进入等待:$mState")
                   waitDecode()
                   //同步时间矫正去除流逝时间
                   mStartTimeForSync=System.currentTimeMillis()-getCurTimeStamp()
               }
               if (!mIsRunning||mState==DecodeState.STOP){
                   mIsRunning=false
                   break
               }
                if (mStartTimeForSync==-1L){
                    mStartTimeForSync=System.currentTimeMillis()
                }
                //如果没有解码完毕要将数据压入解码器
                if (!mIsEOS){
                    //【将数据压入解码器】
                    mIsEOS=pushBufferToDecoder()
                }
                  //【将解好的数据从缓存区拉出来】
                 val index=pullBufferFromDecoder()
                if (index>=0){
                    //音视频同步
                    if (mSyncRender&&mState==DecodeState.DECODING){
                        sleepRender()
                    }
                    //渲染视频,如果只是用于编码合成新视频，无需渲染
                    if (mSyncRender){
                        render(mOutputBuffers!![index],mBufferInfo)
                    }
                    //将数据传出去
                    var frame=Frame()
                    frame.buffer=mOutputBuffers!![index]
                    frame.setBuffer(mBufferInfo)
                    mStateListener?.decoderOneFrame(this,frame)
                    //释放输出缓存
                    mCodec!!.releaseOutputBuffer(index,true)
                    if (mState==DecodeState.START){
                        mState=DecodeState.PAUSE
                    }
                }
                    //最后判断是否解码完成然后关闭解码
                if (mBufferInfo.flags==MediaCodec.BUFFER_FLAG_END_OF_STREAM){
                    Log.i(TAG,"解码完成")
                    mState==DecodeState.FINISH
                    mStateListener?.decoderFinish(this)
                }

            }

        }catch (e:Exception){
            e.printStackTrace()
        }finally {
           doneDecode()
           release()
        }

        return true
    }

    private fun release() {
        try {
            Log.i(TAG,"解码器释放")
            mState=DecodeState.STOP
            mIsEOS=false
            mExtractor?.stop()
            mCodec?.stop()
            mCodec?.release()
            mStateListener?.decoderDestroy(this)
        }catch (e:Exception){}
    }

    private fun sleepRender() {
        val passTime=System.currentTimeMillis()-mStartTimeForSync//正常流逝的时间
        val curTime=getCurTimeStamp();//当前播放时间
        if (curTime>passTime)
        {
            Thread.sleep(curTime-passTime)//同步当前时间音视频时间
        }
    }

    private fun pullBufferFromDecoder(): Int {
         //查询看输入缓存区有没有数据index>0为有效 index为索引
          var index=mCodec!!.dequeueOutputBuffer(mBufferInfo,1000)
           when(index){
               MediaCodec.INFO_OUTPUT_FORMAT_CHANGED ->{ }
               MediaCodec.INFO_TRY_AGAIN_LATER->{}
               MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED ->{
                   mOutputBuffers=mCodec!!.outputBuffers
               }
               else ->{
                   return  index
               }
           }
        return -1
    }

    private fun pushBufferToDecoder(): Boolean {
       var inputBufferIndex=mCodec!!.dequeueInputBuffer(1000)
        var isEndStream=false
        if (inputBufferIndex>=0){
           val inputBuffer=mInputBuffers!![inputBufferIndex]
            val simpleSize=mExtractor!!.readBuffer(inputBuffer)
            if (simpleSize<0){
                 //如果已经取完数据 需要压入结束标识
                mCodec!!.queueInputBuffer(inputBufferIndex,0,0,0,MediaCodec.BUFFER_FLAG_END_OF_STREAM)
              isEndStream=true
            }else{
                mCodec!!.queueInputBuffer(inputBufferIndex,0,simpleSize,mExtractor!!.getCurrentTimestamp(),0)
            }
        }

        return isEndStream
    }

    private fun initCodec(): Boolean {
        try {
            val  type=mExtractor!!.getMediaFormat()!!.getString(MediaFormat.KEY_MIME)
            mCodec= MediaCodec.createDecoderByType(type)
        if (!configCodec(mCodec!!,mExtractor!!.getMediaFormat()!!)) {
            waitDecode()
        }
            mCodec!!.start()
            mInputBuffers=mCodec?.inputBuffers
            mOutputBuffers=mCodec?.outputBuffers
        }catch (e:Exception){
            return false
        }
            return true
    }
   //解码等待线程等待
    private fun waitDecode() {
       try {
           if (mState == DecodeState.PAUSE) {
               mStateListener?.decoderPause(this)
           }
           synchronized(mLock) {
               mLock.wait()
           }
       }catch (e:Exception){
           e.printStackTrace()
       }
    }


    private fun initParams(): Boolean {
    try {
        val format=mExtractor!!.getMediaFormat()!!
          mDuration=format.getLong(MediaFormat.KEY_DURATION)/1000
          if (mEndPos==0L)mEndPos=mDuration
        initSpecParams(format)
         }catch ( e:Exception){
        return false
      }
        return true
    }

    //通知解码线程刷新
  protected  fun  notifyDecode(){
        synchronized(mLock){
         mLock.notifyAll()
        }
        if (mState==DecodeState.DECODING){
            mStateListener?.decoderRunning(this)
        }
    }

    override fun pause() {
        mState=DecodeState.PAUSE
    }

    override fun goOn() {
        mState=DecodeState.DECODING
    }

    override fun seekTo(pos: Long): Long {
        return  0
    }

    override fun seekAndPlay(pos: Long): Long {
        return 0
    }

    override fun isDecoding(): Boolean {
        return mState==DecodeState.DECODING
    }

    override fun isSeeking(): Boolean {
        return mState==DecodeState.SEEKING
    }

    override fun isStop() :Boolean {
        return mState == DecodeState.STOP
    }

    override fun setSizeListener(l: IDecoderProgress) {

    }

    override fun setStateListener(l: IDecoderStateListener) {
        mStateListener=l
    }

    override fun getWidth(): Int {
        return mVideoWidth
    }

    override fun getHeight(): Int {
        return mVideoHeight
    }

    override fun getDuration(): Long {
        return  mDuration
    }
    override fun getCurTimeStamp(): Long {
        return  mBufferInfo.presentationTimeUs/1000
    }

    override fun getRotationAngle(): Int {
        return 0
    }

    override fun getMediaFormat(): MediaFormat?{
        return mExtractor?.getMediaFormat()
    }

    override fun getTrack(): Int {
        return 0
    }

    override fun getFilePath(): String {
        return  mFilePath
    }

    override fun withoutSync():IDecoder {
        mSyncRender=false
        return  this
    }
    //检查子类参数
    abstract  fun check():Boolean
    //初始化数据提取器
    abstract fun initExtractor(path:String):IExtractor
    //初始化子类特有的参数
    abstract fun initSpecParams( format: MediaFormat)
    //初始化渲染器
    abstract fun initRender():Boolean
    abstract fun render(outputBuffer:ByteBuffer,bufferInfo: MediaCodec.BufferInfo)
    //配置解码器
    abstract fun configCodec(mediaCodec: MediaCodec,mediaFormat: MediaFormat):Boolean
    /**
     * 结束解码
     */
    abstract fun doneDecode()
}