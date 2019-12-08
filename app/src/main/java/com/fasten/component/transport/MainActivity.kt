package com.fasten.component.transport

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.fasten.component.spi.annotations.ServiceProvider

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
