package com.zhang.openglrendervideo.openGl

import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLUtils
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * @Description: TODO
 * @Author: chao8
 * @Date: 2020-01-08 15:36:12
 * @Version: appVersionName, 2020-01-08
 * @LastEditors:
 * @LastEditTime: 2020-01-08 15:36:12
 * @Deprecated: false
 */
class VideoDrawer ():IDrawer{
    //顶点坐标
    private val mVertexCoors= floatArrayOf(
        -1f,-1f,
        1f,-1f,
        -1f,1f,
         1f,1f)
   //纹理坐标
    private val mTexterCoors= floatArrayOf(
       0f,1f,
       1f,1f,
       0f,0f,
       1f,0f)
    //纹理id
    private var mTextureId:Int=-1
    //程序id
    private var mProgram:Int=-1
    //顶点坐标句柄
    private var mVertexPosHandler:Int=-1
    //纹理坐标句柄
    private var mTexturePosHandler:Int=-1
    //纹理句柄
    private var mTextureHandler=-1
    //矩阵接受者
    private var  mVertexMatrixHandler=-1
    private lateinit var mVertexBuffer:FloatBuffer
    private lateinit var mTextureBuffer:FloatBuffer
    private var mSurfaceTexture:SurfaceTexture?=null
    private var mSftCb:((SurfaceTexture)->Unit)?=null
    private var mWorldWidth: Int = -1
    private var mWorldHeight: Int = -1
    private var mVideoWidth: Int = -1
    private var mVideoHeight: Int = -1
    private var mMatrix:FloatArray?=null
    private var mAlpha:Float=1.0f
    private var mAlphaHandler:Int=-1
    init {
      //初始化顶点坐标
        initPos()
    }

    private fun initPos() {
      //首先先将坐标数据转化为可供opengl使用的形式
       val bb=ByteBuffer.allocateDirect(mVertexCoors.size*6)
        bb.order(ByteOrder.nativeOrder())
        mVertexBuffer=bb.asFloatBuffer()
        mVertexBuffer.put(mVertexCoors)
        mVertexBuffer.position(0)
        //同理纹理坐标
        val cc =ByteBuffer.allocateDirect(mTexterCoors.size*6)
        cc.order(ByteOrder.nativeOrder())
        mTextureBuffer=cc.asFloatBuffer()
        mTextureBuffer.put(mTexterCoors)
        mTextureBuffer.position(0)

    }
    fun translate(dx: Float, dy: Float) {
        Matrix.translateM(mMatrix, 0, dx*mWidthRatio*2, -dy*mHeightRatio*2, 0f)
    }
    override fun setAlpha(alpha: Float) {
        mAlpha=alpha
    }

    override fun setVideoSize(videoW: Int, videoH: Int) {
        mVideoWidth=videoW
        mVideoHeight=videoH
    }

    override fun setWorldSize(worldW: Int, worldH: Int) {
         mWorldHeight=worldH
         mWorldWidth=worldW
    }

    override fun draw() {
        if (mTextureId!=-1) {
            initMatrix()
            //第二步创建编译启动OpenGL着色器程序
            createGlProgram()
            activeTexture()
            updateTexture()
            //渲染
            doDraw()
        }
    }
    private var mWidthRatio = 1f
    private var mHeightRatio = 1f
   //初始化矩阵
    private fun initMatrix() {
       if (mMatrix != null) return
       if (mVideoWidth != -1 && mVideoHeight != -1 &&
           mWorldWidth != -1 && mWorldHeight != -1) {
           mMatrix = FloatArray(16)
           var prjMatrix = FloatArray(16)
           val originRatio = mVideoWidth / mVideoHeight.toFloat()
           val worldRatio = mWorldWidth / mWorldHeight.toFloat()
           if (mWorldWidth > mWorldHeight) {
               if (originRatio > worldRatio) {
                   mHeightRatio = originRatio / worldRatio
                   Matrix.orthoM(
                       prjMatrix, 0,
                       -mWidthRatio, mWidthRatio,
                       -mHeightRatio, mHeightRatio,
                       3f, 5f
                   )
               } else {// 原始比例小于窗口比例，缩放高度度会导致高度超出，因此，高度以窗口为准，缩放宽度
                   mWidthRatio = worldRatio / originRatio
                   Matrix.orthoM(
                       prjMatrix, 0,
                       -mWidthRatio, mWidthRatio,
                       -mHeightRatio, mHeightRatio,
                       3f, 5f
                   )
               }
           } else {
               if (originRatio > worldRatio) {
                   mHeightRatio = originRatio / worldRatio
                   Matrix.orthoM(
                       prjMatrix, 0,
                       -mWidthRatio, mWidthRatio,
                       -mHeightRatio, mHeightRatio,
                       3f, 5f
                   )
               } else {// 原始比例小于窗口比例，缩放高度会导致高度超出，因此，高度以窗口为准，缩放宽度
                   mWidthRatio = worldRatio / originRatio
                   Matrix.orthoM(
                       prjMatrix, 0,
                       -mWidthRatio, mWidthRatio,
                       -mHeightRatio, mHeightRatio,
                       3f, 5f
                   )
               }
           }

       //设置相机位置
       val viewMatrix = FloatArray(16)
       Matrix.setLookAtM(
           viewMatrix, 0,
           0f, 0f, 5.0f,
           0f, 0f, 0f,
           0f, 1.0f, 0f
       )
//       Matrix.rotateM(prjMatrix,0,270f,0f,0f,1f)
       //计算变换矩阵
       Matrix.multiplyMM(mMatrix, 0, prjMatrix, 0, viewMatrix, 0)
     }
    }

    private fun createGlProgram() {
           if (mProgram==-1){
           val vertexShader=loadShader(GLES20.GL_VERTEX_SHADER,getVertexShader())
           val fragmentShader=loadShader(GLES20.GL_FRAGMENT_SHADER,getFragmentShader())
            //创建程序需要在渲染线程创建
            mProgram=GLES20.glCreateProgram()
               //添加顶点着色器
            GLES20.glAttachShader(mProgram,vertexShader)
            //添加片源着色器
               GLES20.glAttachShader(mProgram,fragmentShader)
            //连接程序
               GLES20.glLinkProgram(mProgram)
               mAlphaHandler=GLES20.glGetAttribLocation(mProgram,"alpha")
               mVertexMatrixHandler = GLES20.glGetUniformLocation(mProgram, "uMatrix")
               mVertexPosHandler=GLES20.glGetAttribLocation(mProgram,"aPosition")
               mTexturePosHandler=GLES20.glGetAttribLocation(mProgram,"aCoordinate")
               mTextureHandler=GLES20.glGetUniformLocation(mProgram,"uTexture")
           }
       GLES20.glUseProgram(mProgram)
    }
       fun activeTexture(){
           //激活指定纹理单元
           GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
           //绑定纹理ID到纹理单元
           GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId)
           //将激活的纹理单元传递到着色器里面
           GLES20.glUniform1i(mTextureHandler, 0)
           //配置边缘过渡参数
           GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR.toFloat())
           GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
           GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
           GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
       }

    private fun doDraw() {
        //启动顶点句柄
        GLES20.glEnableVertexAttribArray(mVertexPosHandler)
        GLES20.glEnableVertexAttribArray(mTexturePosHandler)
        GLES20.glUniformMatrix4fv(mVertexMatrixHandler,1,false,mMatrix,0)
        //设置参数 第二各参数代表每个顶点包含的参数数量
         GLES20.glVertexAttribPointer(mVertexPosHandler,2,GLES20.GL_FLOAT,false,0,mVertexBuffer)
         GLES20.glVertexAttribPointer(mTexturePosHandler,2,GLES20.GL_FLOAT,false,0,mTextureBuffer)
          GLES20.glVertexAttrib1f(mAlphaHandler,mAlpha)
        //开始绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4)

    }
    override fun setTextureId(id: Int) {
        mTextureId=id
        mSurfaceTexture= SurfaceTexture(mTextureId)
        mSftCb?.invoke(mSurfaceTexture!!)
    }

    override fun getSurfaceTexture(cb: (st: SurfaceTexture) -> Unit) {
        mSftCb=cb
    }
    override fun release() {
        GLES20.glDisableVertexAttribArray(mVertexPosHandler)
        GLES20.glDisableVertexAttribArray(mTexturePosHandler)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0)
        GLES20.glDeleteTextures(1, intArrayOf(mTextureId),0)
        GLES20.glDeleteProgram(mProgram)
    }

   fun updateTexture(){
       mSurfaceTexture?.updateTexImage()
   }
    private fun getVertexShader(): String {
        return "attribute vec4 aPosition;" +
                "precision mediump float;" +
                "uniform mat4 uMatrix;" +
                "attribute vec2 aCoordinate;" +
                "attribute float alpha;"+
                "varying float inalpha;"+
                "varying vec2 vCoordinate;" +
                "void main() {" +
                "  gl_Position = uMatrix*aPosition;" +
                "  vCoordinate = aCoordinate;" +
                "  inalpha = alpha;" +
                "}"
    }

    private fun getFragmentShader(): String {
        return "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;" +
                "uniform samplerExternalOES  uTexture;" +
                "varying vec2 vCoordinate;" +
                "varying float inalpha;" +
                "void main() {" +
                "  vec4 color = texture2D(uTexture, vCoordinate);" +
                "  float gray = (color.r+color.g+color.b)/3.0;"+
                "  gl_FragColor = vec4(gray,gray,gray,inalpha);" +
                "}"
    }


    fun loadShader(type:Int, shaderCode:String):Int{
         //根据类型创建片源着色器或者顶点着色器
         val shader=GLES20.glCreateShader(type)
         //载入着色代码
         GLES20.glShaderSource(shader,shaderCode)
         //编译程序
         GLES20.glCompileShader(shader)
        return shader
    }
    fun scale(sx: Float, sy: Float) {
        Matrix.scaleM(mMatrix, 0, sx, sy, 1f)
        mWidthRatio /= sx
        mHeightRatio /= sy
    }
}