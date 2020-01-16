package com.zhang.openglrendervideo.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.zhang.openglrendervideo.R
import com.zhang.openglrendervideo.openGl.IDrawer
import com.zhang.openglrendervideo.openGl.SimpleRender
import com.zhang.openglrendervideo.openGl.TriangleDrawer
import kotlinx.android.synthetic.main.activity_open_gl_render.*

class OpenGlRenderActivity : AppCompatActivity() {
    private lateinit var drawer: IDrawer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_gl_render)
        drawer=TriangleDrawer()
        initRender()
    }

    private fun initRender() {
        glsv.setEGLContextClientVersion(2)
        val render=SimpleRender()
        render.addDrawer(drawer)
        glsv.setRenderer(render)
    }
}
