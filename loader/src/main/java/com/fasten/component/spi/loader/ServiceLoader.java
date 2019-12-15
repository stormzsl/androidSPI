package com.fasten.component.spi.loader;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.ServiceConfigurationError;
import java.util.Set;

/**
 * 作者:created by storm
 */

public class ServiceLoader<S> implements Iterable<S> {

    private Class<S> mServiceClass;

    private Set<S> mProvides=new LinkedHashSet<>();

    public static final <S> ServiceLoader<S> load(Class<S> serviceClass) {

        return new ServiceLoader<>(serviceClass);
    }

    private ServiceLoader(Class<S> serviceClass) {
        this.mServiceClass=serviceClass;
        load();
    }

    public S get(){
        Iterator<S> iterator=mProvides.iterator();
        if(iterator.hasNext()){
            return iterator.next();
        }

        return null;
    }

    @Override
    public Iterator<S> iterator() {
        return Collections.unmodifiableSet(mProvides).iterator();
    }

    private void load(){

        for (Class<? extends S> provideClass: ServiceRegistry.get(mServiceClass)){
            try {
                S providerInstance=ServiceRegistry.newProvider(provideClass);
                mProvides.add(providerInstance);
            }catch (Exception e){
                throw new ServiceConfigurationError("provider :"+provideClass.getName()+" initialized error");
            }

        }
    }
}
