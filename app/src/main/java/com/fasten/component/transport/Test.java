package com.fasten.component.transport;
import android.util.Log;

import com.fasten.component.spi.annotations.ServiceProvider;

/**
 * 作者:created by storm on 2019-12-08
 */
@ServiceProvider(ITest.class)
public class Test implements ITest{
   public static String hello="hello";

    @Override
    public void printMessage(String msg) {
        Log.e("stormzsl",msg);
    }
}
