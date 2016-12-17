package com.digitalpetri.grovepi.sensors;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import com.digitalpetri.grovepi.GroveAnalogPin;
import com.digitalpetri.grovepi.GrovePi;

public class AnalogLed {

    private final GrovePi grovePi;
    private final GroveAnalogPin pin;

    public AnalogLed(GrovePi grovePi, GroveAnalogPin pin) {
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

        setValue(0);
    }

    public CompletableFuture<Void> setValue(int value) {
        return grovePi.execute((io, promise) -> {
            try {
                int val = ((value > 255) ? 255 : (value < 0 ? 0 : value));

                io.write(new byte[]{
                    GrovePi.CMD_ANALOG_WRITE,
                    (byte) pin.number(),
                    (byte) val,
                    GrovePi.CMD_UNUSED
                });

                promise.complete(null);
            } catch (IOException e) {
                promise.failure(e);
            }
        });
    }

}
