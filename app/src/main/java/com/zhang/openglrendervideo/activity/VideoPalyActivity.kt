package com.zhang.openglrendervideo.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import com.zhang.openglrendervideo.AudioDecoder
import com.zhang.openglrendervideo.MP4Repack
import com.zhang.openglrendervideo.R
import com.zhang.openglrendervideo.VideoDecoder
import kotlinx.android.synthetic.main.activity_video_paly.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class VideoPalyActivity : AppCompatActivity() {
    lateinit var threaPool: ExecutorService
    lateinit var videoDecoder: VideoDecoder
    lateinit var audioDecoder: AudioDecoder
    val path = Environment.getExternalStorageDirectory().absolutePath + "/MV.mp4"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_paly)
        initPlayer()
        button.setOnClickListener {
            val mP4Repack=MP4Repack(path)
            mP4Repack.start()
        }
    }
    private fun initPlayer() {

        //线程池
        threaPool= Executors.newFixedThreadPool(10)
        //视频解码器

        videoDecoder= VideoDecoder(path, sfv, null)
        threaPool.execute(videoDecoder)
        //音频解码器
        audioDecoder= AudioDecoder(path)
        threaPool.execute(audioDecoder)
        videoDecoder.goOn()
        audioDecoder.goOn()
    }

    override fun onDestroy() {
        videoDecoder.stop()
        audioDecoder.stop()
        super.onDestroy()
    }
}
