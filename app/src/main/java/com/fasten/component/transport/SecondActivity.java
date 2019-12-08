package com.fasten.component.transport;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.fasten.component.spi.annotations.ServiceProvider;

/**
 * 作者:created by storm on 2019-12-08
 */
@ServiceProvider(SecondActivity.class)
public class SecondActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
