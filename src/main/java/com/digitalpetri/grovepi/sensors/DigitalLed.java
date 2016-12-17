package com.digitalpetri.grovepi.sensors;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import com.digitalpetri.grovepi.GroveDigitalPin;
import com.digitalpetri.grovepi.GrovePi;

public class DigitalLed {

    private final GrovePi grovePi;
    private final GroveDigitalPin pin;

    public DigitalLed(GrovePi grovePi, GroveDigitalPin pin) {
        this.grovePi = grovePi;
        this.pin = pin;

        grovePi.execute((io, promise) -> {
            try {
                io.write(new byte[]{
                    GrovePi.CMD_PIN_MODE,
                    (byte) pin.number(),
                    GrovePi.PIN_MODE_OUT,
                    GrovePi.CMD_UNUSED
                });

                promise.complete(null);
            } catch (IOException e) {
                promise.failure(e);
            }
        });

        setValue(false);
    }

    public CompletableFuture<Void> setValue(boolean value) {
        return grovePi.execute((io, promise) -> {
            try {
                io.write(new byte[]{
                    GrovePi.CMD_DIGITAL_WRITE,
                    (byte) pin.number(),
                    (byte) (value ? 1 : 0),
                    GrovePi.CMD_UNUSED
                });

                promise.complete(null);
            } catch (IOException e) {
                promise.failure(e);
            }
        });
    }

}
