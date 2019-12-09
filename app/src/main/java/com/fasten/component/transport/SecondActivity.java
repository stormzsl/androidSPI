package com.fasten.component.transport;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.fasten.component.spi.annotations.ServiceProvider;
import com.fasten.component.spi.loader.ServiceLoader;

/**
 * 作者:created by storm on 2019-12-08
 */
@ServiceProvider(value = View.OnClickListener.class, priority = 3)
public class SecondActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        for (ITest test : ServiceLoader.load(ITest.class)) {
            test.printMessage("测试");
        }

        for (final View.OnClickListener listener : ServiceLoader.load(View.OnClickListener.class)) {
            Log.i("MainActivity", listener.toString());
        }
    }

    @Override
    public void onClick(View v) {

    }
}
