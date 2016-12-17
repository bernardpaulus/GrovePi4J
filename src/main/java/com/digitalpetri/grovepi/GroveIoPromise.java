package com.digitalpetri.grovepi;

import java.util.concurrent.CompletableFuture;

public class GroveIoPromise<T> {

    final CompletableFuture<T> future = new CompletableFuture<>();

    public void complete(T value) {
        future.complete(value);
    }

    public void failure(Throwable ex) {
        future.completeExceptionally(ex);
    }

}
