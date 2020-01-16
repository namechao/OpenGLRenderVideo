package com.zhang.openglrendervideo

import android.media.MediaCodec
import java.nio.ByteBuffer

/**
 * @Description: TODO
 * @Author: chao8
 * @Date: 2020-01-03 13:38:17
 * @Version: appVersionName, 2020-01-03
 * @LastEditors:
 * @LastEditTime: 2020-01-03 13:38:17
 * @Deprecated: false
 */

class Frame{
   var buffer:ByteBuffer?=null
    var bufferInfo=MediaCodec.BufferInfo()
    private  set
    fun setBuffer(info:MediaCodec.BufferInfo){
        bufferInfo.set(info.offset,info.size,info.presentationTimeUs,info.flags)
    }

}