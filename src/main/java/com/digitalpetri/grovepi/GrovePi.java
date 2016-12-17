package com.digitalpetri.grovepi;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrovePi implements Closeable {

    public static final int CMD_UNUSED = 0;
    public static final int CMD_DIGITAL_READ = 1;
    public static final int CMD_DIGITAL_WRITE = 2;
    public static final int CMD_ANALOG_READ = 3;
    public static final int CMD_ANALOG_WRITE = 4;
    public static final int CMD_PIN_MODE = 5;
    public static final int CMD_ULTRASONIC_READ = 7;
    public static final int CMD_FIRWARE_VERSION = 8;
    public static final int CMD_DHT_TEMP = 40;

    public static final int CMD_LCD = 0x80;
    public static final int CMD_LCD_CLEAR = 0x01;
    public static final int CMD_LCD_WRITE = 0x40;
    public static final int CMD_LCD_NEWLINE = 0xC0;

    public static final int PIN_MODE_IN = 0;
    public static final int PIN_MODE_OUT = 1;

    private static final int GROVE_PI_I2C_ADDRESS = 4;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final I2CBus i2CBus;
    private final I2CDevice i2CDevice;

    public GrovePi() throws IOException, I2CFactory.UnsupportedBusNumberException {
        i2CBus = I2CFactory.getInstance(I2CBus.BUS_1);
        i2CDevice = i2CBus.getDevice(GROVE_PI_I2C_ADDRESS);
    }

    public <T> CompletableFuture<T> execute(GroveIoTransaction<T> transaction) {
        return execute(i2CDevice, transaction);
    }

    public <T> CompletableFuture<T> execute(I2CDevice device, GroveIoTransaction<T> transaction) {
        GroveIoPromise<T> promise = new GroveIoPromise<>();

        executor.execute(() -> {
            GroveIo io = new GroveIoImpl(device);

            logger.debug("Executing transaction: {}", transaction);

            transaction.accept(io, promise);
        });

        promise.future.whenComplete((v, t) -> {
            boolean success = t == null;

            logger.debug("Transaction {}",
                success ? "succeeded." : "failed.");
        });

        return promise.future;
    }

    public I2CBus getI2CBus() {
        return i2CBus;
    }

    public I2CDevice getI2CDevice() {
        return i2CDevice;
    }

    public void shutdown() {
        try {
            close();
        } catch (IOException e) {
            logger.error("Error closing I2CBus: {}", e.getMessage(), e);
        }
    }

    @Override
    public void close() throws IOException {
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.warn("Interrupted waiting for executor termination.", e.getMessage(), e);
        }
        i2CBus.close();
    }

    private static class GroveIoImpl implements GroveIo {

        private final I2CDevice device;

        public GroveIoImpl(I2CDevice device) {
            this.device = device;
        }

        @Override
        public int read() throws IOException {
            return device.read();
        }

        @Override
        public int read(byte[] buffer, int offset, int size) throws IOException {
            return device.read(buffer, offset, size);
        }

        @Override
        public void write(byte b) throws IOException {
            executeIoAction(() -> device.write(b), true);
        }

        @Override
        public void write(byte[] buffer, int offset, int size) throws IOException {
            executeIoAction(() -> device.write(buffer, offset, size), true);
        }

        /**
         * Execute an {@link IoAction}, retrying once if an {@link IOException} occurs.
         *
         * @param action           the {@link IoAction} to execute.
         * @param retryOnException {@code true} if the action should be executed if an {@link IOException} occurs.
         * @throws IOException
         */
        private void executeIoAction(IoAction action, boolean retryOnException) throws IOException {
            try {
                action.execute();
            } catch (IOException e) {
                if (retryOnException) {
                    executeIoAction(action, false);
                } else {
                    throw e;
                }
            }
        }

        private interface IoAction {
            void execute() throws IOException;
        }

    }

}
