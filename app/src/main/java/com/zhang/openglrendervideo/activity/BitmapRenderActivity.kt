package com.zhang.openglrendervideo.activity

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.graphics.drawable.toBitmap
import com.zhang.openglrendervideo.R
import com.zhang.openglrendervideo.openGl.BitmapDrawer
import com.zhang.openglrendervideo.openGl.IDrawer
import com.zhang.openglrendervideo.openGl.SimpleRender
import com.zhang.openglrendervideo.openGl.TriangleDrawer
import kotlinx.android.synthetic.main.activity_open_gl_render.*

class BitmapRenderActivity : AppCompatActivity() {
    private lateinit var drawer: IDrawer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bitmap_render)
        drawer= BitmapDrawer(BitmapFactory.decodeResource(resources,R.drawable.abc))
        initRender()
    }

    private fun initRender() {
        glsv.setEGLContextClientVersion(2)
        val render=SimpleRender()
        render.addDrawer(drawer)
        glsv.setRenderer(render)
    }
}
