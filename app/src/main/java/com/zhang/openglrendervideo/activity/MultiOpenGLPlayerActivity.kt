package com.zhang.openglrendervideo.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.view.Surface
import android.view.SurfaceView
import com.zhang.openglrendervideo.AudioDecoder
import com.zhang.openglrendervideo.R
import com.zhang.openglrendervideo.VideoDecoder
import com.zhang.openglrendervideo.openGl.IDrawer
import com.zhang.openglrendervideo.openGl.SimpleRender
import com.zhang.openglrendervideo.openGl.VideoDrawer
import kotlinx.android.synthetic.main.activity_multi_open_glplayer.*
import kotlinx.android.synthetic.main.activity_multi_open_glplayer.glsv
import kotlinx.android.synthetic.main.activity_open_gl_render.*
import kotlinx.android.synthetic.main.activity_video_paly.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class MultiOpenGLPlayerActivity : AppCompatActivity() {
    private lateinit var drawer: IDrawer
    val path = Environment.getExternalStorageDirectory().absolutePath + "/MV.mp4"
    val path1 = Environment.getExternalStorageDirectory().absolutePath + "/mvtest_2.mp4"
    var threadPool = Executors.newFixedThreadPool(10)
    private var render=SimpleRender()
    val threadPools= Executors.newFixedThreadPool(10)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_open_glplayer)
        drawer= VideoDrawer()
        drawer.setVideoSize(1920, 1080)
        drawer.getSurfaceTexture {
            initPlayer(path,Surface(it),true)
        }
        var drawer2=VideoDrawer()
        drawer2.setVideoSize(1920,1080)
        drawer2.getSurfaceTexture {
            initPlayer(path1,Surface(it),true)
        }
        render.addDrawer(drawer)
        render.addDrawer(drawer2)
        glsv.addDrawer(drawer2)
         Handler().postDelayed({
             drawer2.scale(0.5f, 0.5f)
         },1000)
            initRender()
    }

    private fun initRender() {

        glsv.setEGLContextClientVersion(2)

        glsv.setRenderer(render)
    }

    override fun onDestroy() {
        super.onDestroy()
        threadPool.shutdownNow()
        threadPool=null
        drawer.release()
    }
    private fun initPlayer(path:String,sf: Surface,isAudio:Boolean) {
        val videoDecoder = VideoDecoder(path, null, sf)
        threadPool.execute(videoDecoder)
        val audioDecoder = AudioDecoder(path)
        threadPool.execute(audioDecoder)
        audioDecoder.goOn()
        videoDecoder.goOn()
    }

}
