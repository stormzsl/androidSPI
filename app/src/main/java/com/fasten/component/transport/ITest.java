package com.fasten.component.transport;

import com.fasten.component.spi.annotations.ServiceProviderInterface;

/**
 * 作者:created by storm on 2019-12-08
 */
@ServiceProviderInterface
public interface ITest {


    void printMessage(String msg);
}
