package com.digitalpetri.grovepi;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import com.digitalpetri.grovepi.sensors.AnalogLed;
import com.digitalpetri.grovepi.sensors.DigitalLed;
import com.digitalpetri.grovepi.sensors.GroveLightSensor;
import com.digitalpetri.grovepi.sensors.RgbBacklightLcd;
import com.digitalpetri.grovepi.sensors.RotaryAngleSensor;
import com.digitalpetri.grovepi.sensors.TemperatureAndHumiditySensor;
import com.digitalpetri.grovepi.sensors.UltrasonicRangeSensor;
import com.pi4j.io.i2c.I2CFactory;

public class GrovePiMain {

    public static void main(String[] args) throws IOException, I2CFactory.UnsupportedBusNumberException {
        GrovePi grovePi = new GrovePi();

        TemperatureAndHumiditySensor temperatureAndHumiditySensor =
            new TemperatureAndHumiditySensor(grovePi, GroveDigitalPin.D4, 0);

        RotaryAngleSensor rotaryAngleSensor = new RotaryAngleSensor(grovePi, GroveAnalogPin.A0);

        UltrasonicRangeSensor ultrasonicRangeSensor = new UltrasonicRangeSensor(grovePi, GroveDigitalPin.D3);

        GroveLightSensor lightSensor = new GroveLightSensor(grovePi, GroveAnalogPin.A2);

        RgbBacklightLcd lcd = new RgbBacklightLcd(grovePi);

        DigitalLed digitalLed = new DigitalLed(grovePi, GroveDigitalPin.D2);
        AnalogLed analogLed = new AnalogLed(grovePi, GroveAnalogPin.A1);

        Random random = new Random();
        int r = random.nextInt(256);
        int g = random.nextInt(256);
        int b = random.nextInt(256);

        boolean ledOn = true;
        int ledValue = 0;

        while (true) {
            try {
                TemperatureAndHumiditySensor.TemperatureAndHumidity tempAndHumidity =
                    temperatureAndHumiditySensor.getTemperatureAndHumidity().get();
//
//                System.out.printf(
//                        "temperature=%.1fC, humidity=%.1f%%\n",
//                        tempAndHumidity.getTemperature(),
//                        tempAndHumidity.getHumidity());

//                RotaryAngleSensor.RotaryAngle rotaryAngle =
//                        rotaryAngleSensor.getRotaryAngle().get();
//
//                System.out.println(rotaryAngle);

//                Double range = ultrasonicRangeSensor.getRange().get();
//
//                System.out.printf("range=%.2fcm\n", range);

//                Double colorTemperature = lightSensor.getValue().get();
//                System.out.printf("colorTemperature=%.2fK\n", colorTemperature);

                String text = String.format(
                    "temp=%.1fC\nhumidity=%.1f%%",
                    tempAndHumidity.getTemperature(),
                    tempAndHumidity.getHumidity());

                lcd.setRgb(r, g, b);
                lcd.setText(text);

                digitalLed.setValue(ledOn);
                ledOn = !ledOn;

                analogLed.setValue(ledValue);
                ledValue += 10;

                Thread.sleep(100);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

}
