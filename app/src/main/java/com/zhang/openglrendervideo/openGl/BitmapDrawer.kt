package com.zhang.openglrendervideo.openGl

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
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
class BitmapDrawer ( private val mBitmap: Bitmap):IDrawer{
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
    private lateinit var mVertexBuffer:FloatBuffer
    private lateinit var mTextureBuffer:FloatBuffer

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

    override fun setAlpha(alpha: Float) {

    }

    override fun setVideoSize(videoW: Int, videoH: Int) {

    }

    override fun setWorldSize(worldW: Int, worldH: Int) {

    }

    override fun draw() {
        if (mTextureId!=-1) {
            //第二步创建编译启动OpenGL着色器程序
            createGlProgram()
            activeTexture()
            bindBitmapToTexture()
            //渲染
            doDraw()
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
               mVertexPosHandler=GLES20.glGetAttribLocation(mProgram,"aPosition")
               mTexturePosHandler=GLES20.glGetAttribLocation(mProgram,"aCoordinate")
               mTextureHandler=GLES20.glGetAttribLocation(mProgram,"uTexture")
           }
       GLES20.glUseProgram(mProgram)
    }
       fun activeTexture(){
         //激活纹理单元
           GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
           //绑定纹理
           GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,mTextureId)
          //将激活纹理传到着色器上
           GLES20.glUniform1i(mTextureHandler,0)
           //配置边缘过渡参数
           GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR.toFloat())
           GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
           GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
           GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
       }
     fun  bindBitmapToTexture(){
         if (!mBitmap.isRecycled){
             GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,mBitmap,0)
         }
     }
    private fun doDraw() {
        //启动顶点句柄
        GLES20.glEnableVertexAttribArray(mVertexPosHandler)
        GLES20.glEnableVertexAttribArray(mTexturePosHandler)
        //设置参数 第二各参数代表每个顶点包含的参数数量
         GLES20.glVertexAttribPointer(mVertexPosHandler,2,GLES20.GL_FLOAT,false,0,mVertexBuffer)
         GLES20.glVertexAttribPointer(mTexturePosHandler,2,GLES20.GL_FLOAT,false,0,mTextureBuffer)
        //开始绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4)

    }
    override fun setTextureId(id: Int) {
        mTextureId=id
    }

    override fun release() {
          GLES20.glDisableVertexAttribArray(mVertexPosHandler)
        GLES20.glDisableVertexAttribArray(mTexturePosHandler)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0)
        GLES20.glDeleteTextures(1, intArrayOf(mTextureId),0)
        GLES20.glDeleteProgram(mProgram)
    }

    private fun getVertexShader(): String {
        return "attribute vec4 aPosition;" +
                "attribute vec2 aCoordinate;" +
                "varying vec2 vCoordinate;" +
                "void main() {" +
                "  gl_Position = aPosition;" +
                "  vCoordinate = aCoordinate;" +
                "}"
    }

    private fun getFragmentShader(): String {
        return "precision mediump float;" +
                "uniform sampler2D uTexture;" +
                "varying vec2 vCoordinate;" +
                "void main() {" +
                "  vec4 color = texture2D(uTexture, vCoordinate);" +
                "  gl_FragColor = color;" +
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

}