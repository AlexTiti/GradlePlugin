package com.alex.kotlin.gradleplugin

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.alex.kotlin.sdkasppectj.OnClickAnnotation
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnButton.setOnClickListener { action(it) }
    }

    @OnClickAnnotation
    public fun action(view : View) {
        startActivity(Intent(this, Main2Activity::class.java))
    }

}
