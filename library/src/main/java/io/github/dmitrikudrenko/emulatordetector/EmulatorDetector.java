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

    private EmulatorDetector(int delay, int eventCount) {
        this.delay = delay;
        this.sensorData = new float[eventCount][];
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
                                    processSensorData(callback);
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
        sensorManager.registerListener(mSensorEventListener, accelerometerSensor, 1000 * 1000 * 1000);
    }

    private void processSensorData(Callback callback) {
        float dx, dy, dz;
        float lastX = 0, lastY = 0, lastZ = 0;
        int sameEventCount = 0;
        Log.i("Sensor data", Arrays.deepToString(sensorData));
        for (int i = 0; i < sensorData.length; i++) {
            if (i == 0) {
                lastX = sensorData[i][0];
                lastY = sensorData[i][1];
                lastZ = sensorData[i][2];
                continue;
            }
            dx = sensorData[i][0] - lastX;
            dy = sensorData[i][1] - lastY;
            dz = sensorData[i][2] - lastZ;
            int sameD = 0;
            if (dx == 0) sameD++;
            if (dy == 0) sameD++;
            if (dz == 0) sameD++;
            if (sameD >= 2) sameEventCount++;
        }
        callback.onDetect( (double) sameEventCount /  (double) sensorData.length >= 0.5D);
    }

    private float[] copy(float[] array) {
        float[] copy = new float[array.length];
        System.arraycopy(array, 0, copy, 0, array.length);
        return copy;
    }

    public interface Callback {
        void onDetect(boolean isEmulator);

        void onError(Exception exception);
    }

    public static class Builder {
        private int delay = 1000;
        private int eventCount = 10;

        public Builder setEventCount(int eventCount) {
            this.eventCount = eventCount;
            return this;
        }

        public Builder setDelay(int delay) {
            this.delay = delay;
            return this;
        }

        public EmulatorDetector build() {
            return new EmulatorDetector(delay, eventCount);
        }
    }
}
