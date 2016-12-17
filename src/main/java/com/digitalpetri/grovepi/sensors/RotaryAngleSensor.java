package com.digitalpetri.grovepi.sensors;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import com.digitalpetri.grovepi.GroveAnalogPin;
import com.digitalpetri.grovepi.GrovePi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RotaryAngleSensor {

    /**
     * ADC reference voltage.
     */
    private static final double ADC_REFERENCE = 5;

    /**
     * Grove VCC voltage.
     */
    private static final double GROVE_VCC = 5;

    /**
     * Full rotary angle is 300 (0-300).
     */
    private static final double FULL_ANGLE = 300;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final GrovePi grovePi;
    private final GroveAnalogPin pin;

    public RotaryAngleSensor(GrovePi grovePi, GroveAnalogPin pin) {
        this.grovePi = grovePi;
        this.pin = pin;
    }

    public CompletableFuture<RotaryAngle> getRotaryAngle() {
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

                double sensorValue = ((data[1] & 0xFF) * 256) + (data[2] & 0xFF);
                double voltage = (sensorValue * ADC_REFERENCE / 1023);
                double degrees = voltage * FULL_ANGLE / GROVE_VCC;

                logger.debug(
                    "sensorValue={}, voltage={}, degrees={}",
                    sensorValue, voltage, degrees);

                promise.complete(new RotaryAngle(sensorValue, voltage, degrees));
            } catch (IOException e) {
                promise.failure(e);
            }
        });
    }

    public static class RotaryAngle {

        private final double sensorValue;
        private final double voltage;
        private final double degrees;

        public RotaryAngle(double sensorValue, double voltage, double degrees) {
            this.sensorValue = sensorValue;
            this.voltage = voltage;
            this.degrees = degrees;
        }

        public double getSensorValue() {
            return sensorValue;
        }

        public double getVoltage() {
            return voltage;
        }

        public double getDegrees() {
            return degrees;
        }

        @Override
        public String toString() {
            return "RotaryAngle{" +
                "sensorValue=" + sensorValue +
                ", voltage=" + voltage +
                ", degrees=" + degrees +
                '}';
        }

    }
}
