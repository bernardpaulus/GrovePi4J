package com.digitalpetri.grovepi.sensors;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.CompletableFuture;

import com.digitalpetri.grovepi.GroveDigitalPin;
import com.digitalpetri.grovepi.GrovePi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemperatureAndHumiditySensor {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final GrovePi grovePi;
    private final GroveDigitalPin pin;
    private final int sensorType;

    public TemperatureAndHumiditySensor(GrovePi grovePi, GroveDigitalPin pin, int sensorType) {
        this.grovePi = grovePi;
        this.pin = pin;
        this.sensorType = sensorType;
    }

    public CompletableFuture<TemperatureAndHumidity> getTemperatureAndHumidity() {
        return grovePi.execute((io, promise) -> {
            try {
                byte[] cmd = {
                    GrovePi.CMD_DHT_TEMP,
                    (byte) pin.number(),
                    (byte) sensorType,
                    GrovePi.CMD_UNUSED
                };

                io.write(cmd);
                io.sleep(750);

                byte[] data = new byte[9];
                io.read(data, 0, data.length);

                ByteBuffer bb = ByteBuffer.wrap(data)
                    .order(ByteOrder.LITTLE_ENDIAN);

                bb.get(); // skip first byte
                float temperature = bb.getFloat();
                float humidity = bb.getFloat();

                logger.debug("temperature={}C, humidity={}%");

                promise.complete(new TemperatureAndHumidity(temperature, humidity));
            } catch (BufferUnderflowException | IOException e) {
                promise.failure(e);
            }
        });
    }

    public static class TemperatureAndHumidity {

        private final float temperature;
        private final float humidity;

        public TemperatureAndHumidity(float temperature, float humidity) {
            this.temperature = temperature;
            this.humidity = humidity;
        }

        /**
         * @return the temperature, measured in Celsius.
         */
        public float getTemperature() {
            return temperature;
        }

        /**
         * @return the humidity, as a percentage.
         */
        public float getHumidity() {
            return humidity;
        }

        @Override
        public String toString() {
            return "TemperatureAndHumidity{" +
                "temperature=" + temperature +
                ", humidity=" + humidity +
                '}';
        }

    }

}

