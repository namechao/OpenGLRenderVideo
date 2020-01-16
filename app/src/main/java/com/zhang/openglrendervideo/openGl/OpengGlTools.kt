package com.zhang.openglrendervideo.openGl

import android.opengl.GLES20

/**
 * @Description: TODO
 * @Author: chao8
 * @Date: 2020-01-08 17:15:49
 * @Version: appVersionName, 2020-01-08
 * @LastEditors:
 * @LastEditTime: 2020-01-08 17:15:49
 * @Deprecated: false
 */
object OpengGlTools {
   fun creatTextureId(count:Int):IntArray{
   var texture=IntArray(count)
    GLES20.glGenTextures(count,texture,0)
       return texture
   }

}