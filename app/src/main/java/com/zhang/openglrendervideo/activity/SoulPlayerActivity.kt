package com.zhang.openglrendervideo.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.Surface
import com.zhang.openglrendervideo.AudioDecoder
import com.zhang.openglrendervideo.R
import com.zhang.openglrendervideo.VideoDecoder
import com.zhang.openglrendervideo.openGl.IDrawer
import com.zhang.openglrendervideo.openGl.SimpleRender
import com.zhang.openglrendervideo.openGl.SoulVideoDrawer
import kotlinx.android.synthetic.main.activity_soul_player.*
import java.util.concurrent.Executors

class SoulPlayerActivity : AppCompatActivity() {
    val path = Environment.getExternalStorageDirectory().absolutePath + "/mvtest.mp4"
    lateinit var drawer: IDrawer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_soul_player)
        initRender()
    }
    private fun initRender() {
        // 使用“灵魂出窍”渲染器
        drawer = SoulVideoDrawer()
        drawer.setVideoSize(1920, 1080)
        drawer.getSurfaceTexture {
            initPlayer(Surface(it))
        }
        glsv.setEGLContextClientVersion(2)
        val render = SimpleRender()
        render.addDrawer(drawer)
        glsv.setRenderer(render)
    }
    private fun initPlayer(sf: Surface) {
        val threadPool = Executors.newFixedThreadPool(10)

        val videoDecoder = VideoDecoder(path, null, sf)
        threadPool.execute(videoDecoder)

        val audioDecoder = AudioDecoder(path)
        threadPool.execute(audioDecoder)

        videoDecoder.goOn()
        audioDecoder.goOn()
    }

}
