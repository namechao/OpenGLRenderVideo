package com.zhang.openglrendervideo.activity

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import com.zhang.openglrendervideo.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button.setOnClickListener {
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ,1)
        }
        button2.setOnClickListener {
            startActivity(Intent(this, OpenGlRenderActivity::class.java))
        }
        button3.setOnClickListener {
            startActivity(Intent(this, BitmapRenderActivity::class.java))
        }
        button4.setOnClickListener {
            startActivity(Intent(this, VideoRenderActivity::class.java))
        }
         button5.setOnClickListener {
           startActivity(Intent(this, MultiOpenGLPlayerActivity::class.java))
          }
        button6.setOnClickListener {
            startActivity(Intent(this, EGLPlayerActivity::class.java))
        }

    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode==1){
            for (i in 0 until permissions.size) {
                if (grantResults[i] === PERMISSION_GRANTED) { // 申请成功
                    startActivity(Intent(this, VideoPalyActivity::class.java))
                } else { // 申请失败
                }
            }
        }

    }

}
