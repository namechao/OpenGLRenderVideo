package com.zhang.openglrendervideo.activity

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.Surface
import com.zhang.openglrendervideo.AudioDecoder
import com.zhang.openglrendervideo.R
import com.zhang.openglrendervideo.VideoDecoder
import com.zhang.openglrendervideo.openGl.IDrawer
import com.zhang.openglrendervideo.openGl.SimpleRender
import com.zhang.openglrendervideo.openGl.TriangleDrawer
import com.zhang.openglrendervideo.openGl.VideoDrawer
import kotlinx.android.synthetic.main.activity_open_gl_render.*
import java.util.concurrent.Executors

class VideoRenderActivity : AppCompatActivity() {
    private lateinit var drawer: IDrawer
    val path = Environment.getExternalStorageDirectory().absolutePath + "/MV.mp4"
    var threadPool = Executors.newFixedThreadPool(10)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_render)
        drawer= VideoDrawer()
        drawer.setVideoSize(1920, 1080)
        drawer.getSurfaceTexture {
            initPlayer(Surface(it))
        }
        initRender()
    }

    private fun initRender() {

        glsv.setEGLContextClientVersion(2)
        val render= SimpleRender()
        render.addDrawer(drawer)
        glsv.setRenderer(render)
    }

    override fun onDestroy() {
        super.onDestroy()
        threadPool.shutdownNow()
        threadPool=null
        drawer.release()
    }
    private fun initPlayer(sf: Surface) {


        val videoDecoder = VideoDecoder(path, null, sf)
        threadPool.execute(videoDecoder)

        val audioDecoder = AudioDecoder(path)
        threadPool.execute(audioDecoder)

        videoDecoder.goOn()
        audioDecoder.goOn()
    }

}