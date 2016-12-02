package io.github.dmitrikudrenko.emulatordetector;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;

import java.util.Arrays;

public class EmulatorDetector {
    private final int delay;
    private final float[][] sensorData;
    private int eventCount;
    private float[] lastSensorValues;
    private Handler mHandler = new Handler();
    private SensorEventListener mSensorEventListener;
    private boolean isSleeping;
    private ISensorDataProcessor sensorDataProcessor;

    private EmulatorDetector(int delay, int eventCount, ISensorDataProcessor sensorDataProcessor) {
        this.delay = delay;
        this.sensorData = new float[eventCount][];
        this.sensorDataProcessor = sensorDataProcessor;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void detect(Context context, final Callback callback) {
        final SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometerSensor == null) {
            callback.onError(new RuntimeException("No sensor"));
            return;
        }

        mSensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                Log.i("Sensor event", sensorEvent.toString());
                lastSensorValues = sensorEvent.values;
                if (!isSleeping) {
                    isSleeping = true;
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (eventCount < sensorData.length) {
                                sensorData[eventCount] = copy(lastSensorValues);
                                Log.i("Sensor data", Arrays.toString(lastSensorValues));
                                eventCount++;
                                if (eventCount != sensorData.length) {
                                    mHandler.postDelayed(this, delay);
                                } else {
                                    sensorManager.unregisterListener(mSensorEventListener);
                                    callback.onDetect(sensorDataProcessor.isEmulator(sensorData));
                                }
                                isSleeping = false;
                            }
                        }
                    }, delay);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
        sensorManager.registerListener(mSensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    private float[] copy(float[] array) {
        float[] copy = new float[array.length];
        System.arraycopy(array, 0, copy, 0, array.length);
        return copy;
    }

    public float[][] getSensorData() {
        return sensorData;
    }

    public interface Callback {
        void onDetect(boolean isEmulator);

        void onError(Exception exception);
    }

    public static class Builder {
        private int delay = 1000;
        private int eventCount = 10;
        private ISensorDataProcessor sensorDataProcessor;

        public Builder setEventCount(int eventCount) {
            this.eventCount = eventCount;
            return this;
        }

        public Builder setDelay(int delay) {
            this.delay = delay;
            return this;
        }

        public Builder setSensorDataProcessor(ISensorDataProcessor sensorDataProcessor) {
            this.sensorDataProcessor = sensorDataProcessor;
            return this;
        }

        public EmulatorDetector build() {
            return new EmulatorDetector(delay, eventCount,
                    sensorDataProcessor != null ? sensorDataProcessor : new DefaultSensorDataProcessor());
        }
    }
}
