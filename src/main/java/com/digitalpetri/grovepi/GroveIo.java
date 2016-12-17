package com.digitalpetri.grovepi;

import java.io.IOException;

public interface GroveIo {
    int read() throws IOException;

    int read(byte[] buffer, int offset, int size) throws IOException;

    void write(byte b) throws IOException;

    void write(byte[] buffer, int offset, int size) throws IOException;

    default void write(byte[] buffer) throws IOException {
        write(buffer, 0, buffer.length);
    }

    default void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Throwable ignored) {
        }
    }
}
