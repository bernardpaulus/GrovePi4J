package com.digitalpetri.grovepi.sensors;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import com.digitalpetri.grovepi.GrovePi;
import com.pi4j.io.i2c.I2CDevice;

public class RgbBacklightLcd {

    private static final int I2C_ADDRESS_RGB = 0x62;
    private static final int I2C_ADDRESS_TEXT = 0x3E;

    private volatile String lastText;

    private final I2CDevice rgbDevice;
    private final I2CDevice textDevice;

    private final GrovePi grovePi;

    public RgbBacklightLcd(GrovePi grovePi) throws IOException {
        this.grovePi = grovePi;

        rgbDevice = grovePi.getI2CBus().getDevice(I2C_ADDRESS_RGB);
        textDevice = grovePi.getI2CBus().getDevice(I2C_ADDRESS_TEXT);

        initRgbDevice();
        initTextDevice();
    }

    private void initRgbDevice() {
        grovePi.execute(rgbDevice, (io, promise) -> {
            try {
                io.write(new byte[]{0, 0});
                io.write(new byte[]{1, 0});
                io.write(new byte[]{0x08, (byte) 0xAA});
                io.sleep(50);

                promise.complete(null);
            } catch (IOException e) {
                promise.failure(e);
            }
        });
    }

    private void initTextDevice() {
        grovePi.execute(textDevice, (io, promise) -> {
            try {
                // display on, no cursor
                io.write(new byte[]{
                    (byte) GrovePi.CMD_LCD,
                    (byte) (0x08 | 0x04)
                });
                io.sleep(50);

                // two lines
                io.write(new byte[]{
                    (byte) GrovePi.CMD_LCD,
                    (byte) 0x28
                });
                io.sleep(50);

                promise.complete(null);
            } catch (IOException e) {
                promise.failure(e);
            }
        });
    }

    public CompletableFuture<Void> setRgb(int r, int g, int b) {
        return grovePi.execute(rgbDevice, (io, promise) -> {
            try {
                io.write(new byte[]{4, (byte) r});
                io.write(new byte[]{3, (byte) g});
                io.write(new byte[]{2, (byte) b});
                io.sleep(50);

                promise.complete(null);
            } catch (IOException e) {
                promise.failure(e);
            }
        });
    }

    public CompletableFuture<Void> setText(String text) {
        return grovePi.execute(textDevice, (io, promise) -> {
            try {
                if (Objects.equals(text, lastText)) {
                    promise.complete(null);
                    return;
                }

                // clear the display
                io.write(new byte[]{
                    (byte) GrovePi.CMD_LCD,
                    (byte) GrovePi.CMD_LCD_CLEAR
                });
                io.sleep(50);

                int count = 0;
                int row = 0;
                for (char c : text.toCharArray()) {
                    if (c == '\n' || count == 16) {
                        count = 0;

                        if (++row == 2) break;

                        io.write(new byte[]{
                            (byte) GrovePi.CMD_LCD,
                            (byte) GrovePi.CMD_LCD_NEWLINE
                        });

                        if (c == '\n') continue;
                    }

                    count++;
                    io.write(new byte[]{
                        (byte) GrovePi.CMD_LCD_WRITE,
                        (byte) c
                    });
                }
                io.sleep(100);

                lastText = text;

                promise.complete(null);
            } catch (IOException e) {
                promise.failure(e);
            }
        });
    }

}
