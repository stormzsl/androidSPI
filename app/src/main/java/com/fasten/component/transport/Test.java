package com.fasten.component.transport;
import com.fasten.component.spi.annotations.ServiceProvider;

/**
 * 作者:created by storm on 2019-12-08
 */
@ServiceProvider(ITest.class)
public class Test implements ITest{

    @Override
    public void printMessage(String msg) {

    }
}
