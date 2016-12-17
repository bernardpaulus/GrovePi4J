package com.digitalpetri.grovepi.sensors;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import com.digitalpetri.grovepi.GroveAnalogPin;
import com.digitalpetri.grovepi.GrovePi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroveLightSensor {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final GrovePi grovePi;
    private final GroveAnalogPin pin;

    public GroveLightSensor(GrovePi grovePi, GroveAnalogPin pin) {
        this.grovePi = grovePi;
        this.pin = pin;
    }

    public CompletableFuture<Double> getValue() {
        return grovePi.execute((io, promise) -> {
            try {
                byte[] cmd = {
                    GrovePi.CMD_ANALOG_READ,
                    (byte) pin.number(),
                    GrovePi.CMD_UNUSED,
                    GrovePi.CMD_UNUSED
                };

                io.write(cmd);
                io.sleep(100);

                byte[] data = new byte[4];
                io.read(data, 0, data.length);

                double value = ((data[1] & 0xFF) * 256.0) + (data[2] & 0xFF);

                logger.debug("value={}K", value);

                promise.complete(value);
            } catch (IOException e) {
                promise.failure(e);
            }
        });
    }

}
