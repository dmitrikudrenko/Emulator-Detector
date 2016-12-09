package io.github.dmitrikudrenko.emulatordetector.gyroscope;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Handler;

import io.github.dmitrikudrenko.emulatordetector.BaseDetector;
import io.github.dmitrikudrenko.emulatordetector.Callback;
import io.github.dmitrikudrenko.emulatordetector.DefaultSensorDataProcessor;
import io.github.dmitrikudrenko.emulatordetector.ISensorDataProcessor;

public class GyroscopeDetector extends BaseDetector {
    private final int delay;
    private final float[][] sensorData;
    private int eventCount;
    private float[] lastSensorValues;
    private Handler mHandler = new Handler();
    private boolean isSleeping;
    private ISensorDataProcessor sensorDataProcessor;

    private GyroscopeDetector(int delay, int eventCount, ISensorDataProcessor sensorDataProcessor) {
        this.delay = delay;
        this.sensorData = new float[eventCount][];
        this.sensorDataProcessor = sensorDataProcessor;
    }

    @Override
    protected int getSensorType() {
        return Sensor.TYPE_GYROSCOPE;
    }

    @Override
    protected void onSensorEventReceived(SensorEvent sensorEvent, final Callback callback) {
        lastSensorValues = sensorEvent.values;
        if (!isSleeping) {
            isSleeping = true;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (eventCount < sensorData.length) {
                        sensorData[eventCount] = copy(lastSensorValues);
                        eventCount++;
                        if (eventCount != sensorData.length) {
                            mHandler.postDelayed(this, delay);
                        } else {
                            onDetectionComplete();
                            callback.onDetect(sensorDataProcessor.isEmulator(sensorData));
                        }
                        isSleeping = false;
                    }
                }
            }, delay);
        }
    }

    private float[] copy(float[] array) {
        float[] copy = new float[array.length];
        System.arraycopy(array, 0, copy, 0, array.length);
        return copy;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int delay = 1000;
        private int eventCount = 10;
        private ISensorDataProcessor sensorDataProcessor;

        public Builder() {
            this.sensorDataProcessor = new DefaultSensorDataProcessor();
        }

        public GyroscopeDetector.Builder setEventCount(int eventCount) {
            this.eventCount = eventCount;
            return this;
        }

        public GyroscopeDetector.Builder setDelay(int delay) {
            this.delay = delay;
            return this;
        }

        public GyroscopeDetector.Builder setSensorDataProcessor(ISensorDataProcessor sensorDataProcessor) {
            this.sensorDataProcessor = sensorDataProcessor;
            return this;
        }

        public GyroscopeDetector build() {
            return new GyroscopeDetector(delay, eventCount, sensorDataProcessor);
        }
    }
}
