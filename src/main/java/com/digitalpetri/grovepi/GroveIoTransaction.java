package com.digitalpetri.grovepi;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface GroveIoTransaction<T> extends BiConsumer<GroveIo, GroveIoPromise<T>> {

}
