package com.fasten.component.transport

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.fasten.component.spi.loader.ServiceLoader
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tv_click.setOnClickListener {
            startActivity(Intent(this,SecondActivity::class.java))
        }
    }
}
