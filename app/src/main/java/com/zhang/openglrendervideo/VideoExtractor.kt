package com.zhang.openglrendervideo

import android.media.MediaFormat
import java.nio.ByteBuffer

/**
 * @Description: TODO
 * @Author: chao8
 * @Date: 2020-01-06 16:30:37
 * @Version: appVersionName, 2020-01-06
 * @LastEditors:
 * @LastEditTime: 2020-01-06 16:30:37
 * @Deprecated: false
 */
class VideoExtractor(path:String) :IExtractor{
    private val mMediaExtractor=MMExtractor(path)
    override fun getMediaFormat(): MediaFormat? {
       return  mMediaExtractor.getVideoFormat()
    }

    override fun readBuffer(byteBuffer: ByteBuffer): Int {
      return mMediaExtractor.readBuffer(byteBuffer)
    }

    override fun getCurrentTimestamp(): Long {
      return mMediaExtractor.getCurrentTimestamp()
    }

    override fun seek(pos: Long): Long {
        return mMediaExtractor.seek(pos)
    }

    override fun setStartPos(pos: Long) {
       return mMediaExtractor.setStartPos(pos)
    }

    override fun stop() {
      mMediaExtractor.stop()
    }

    override fun getSampleFlag(): Int {
       return mMediaExtractor.getSampleFlag()
    }


}