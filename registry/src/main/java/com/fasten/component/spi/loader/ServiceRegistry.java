package com.fasten.component.spi.loader;

import java.util.Set;

/**
 * 作者:created by storm on 2019-12-07
 */

public abstract class ServiceRegistry {

    private ServiceRegistry(){}

    public static synchronized <S,P extends S> void register(Class<S> serviceClass,Class<P> providerClass){
        throw new RuntimeException("Stub!");
    }

    public static synchronized <S> Set<Class<? extends S>> get(Class<S> serviceClass){
        throw new RuntimeException("Stub!");
    }

    public static synchronized <S> S newProvider(Class<? extends S> providerClass){
        throw new RuntimeException("Stub!");
    }

}
