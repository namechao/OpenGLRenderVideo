package com.zhang.openglrendervideo.openGl

import android.opengl.GLES20
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
class TriangleDrawer ():IDrawer{
    //顶点坐标
    private val mVertexCoors= floatArrayOf(
        -1f,-1f,
        1f,-1f,
        0f,1f)
   //纹理坐标
    private val mTexterCoors= floatArrayOf(
       0f,1f,
       1f,1f,
       0.5f,0f)
    //纹理id
    private var mTextureId:Int=-1
    //程序id
    private var mProgram:Int=-1
    //顶点坐标句柄
    private var mVertexPosHandler:Int=-1
    //纹理坐标句柄
    private var mTexturePosHandler:Int=-1

    private lateinit var mVertexBuffer:FloatBuffer
    private lateinit var mTextureBuffer:FloatBuffer

    init {
      //初始化顶点坐标
        initPos()
    }

    private fun initPos() {
      //首先先将坐标数据转化为可供opengl使用的形式
       val bb=ByteBuffer.allocateDirect(mVertexCoors.size*4)
        bb.order(ByteOrder.nativeOrder())
        mVertexBuffer=bb.asFloatBuffer()
        mVertexBuffer.put(mVertexCoors)
        mVertexBuffer.position(0)
        //同理纹理坐标
        val cc =ByteBuffer.allocateDirect(mTexterCoors.size*4)
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
           }
       GLES20.glUseProgram(mProgram)
    }

    private fun doDraw() {
        //启动顶点句柄
        GLES20.glEnableVertexAttribArray(mVertexPosHandler)
        GLES20.glEnableVertexAttribArray(mTexturePosHandler)
        //设置参数 第二各参数代表每个顶点包含的参数数量
         GLES20.glVertexAttribPointer(mVertexPosHandler,2,GLES20.GL_FLOAT,false,0,mVertexBuffer)
         GLES20.glVertexAttribPointer(mTexturePosHandler,2,GLES20.GL_FLOAT,false,0,mTextureBuffer)
        //开始绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,3)

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
                "void main() {" +
                "  gl_Position = aPosition;" +
                "}"
    }

    private fun getFragmentShader(): String {
        return "precision mediump float;" +
                "void main() {" +
                "  gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);" +
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