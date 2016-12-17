package com.digitalpetri.grovepi.sensors;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import com.digitalpetri.grovepi.GroveDigitalPin;
import com.digitalpetri.grovepi.GrovePi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UltrasonicRangeSensor {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final GrovePi grovePi;
    private final GroveDigitalPin pin;

    public UltrasonicRangeSensor(GrovePi grovePi, GroveDigitalPin pin) {
        this.grovePi = grovePi;
        this.pin = pin;
    }

    public CompletableFuture<Double> getRange() {
        return grovePi.execute((io, promise) -> {
            try {
                byte[] cmd = {
                    GrovePi.CMD_ULTRASONIC_READ,
                    (byte) pin.number(),
                    GrovePi.CMD_UNUSED,
                    GrovePi.CMD_UNUSED
                };

                io.write(cmd);
                io.sleep(200);

                byte[] data = new byte[4];
                io.read(data, 0, data.length);

                double range = ((data[1] & 0xFF) * 256.0) + (data[2] & 0xFF);

                logger.debug("range={}", range);

                promise.complete(range);
            } catch (IOException e) {
                promise.failure(e);
            }
        });
    }

}
