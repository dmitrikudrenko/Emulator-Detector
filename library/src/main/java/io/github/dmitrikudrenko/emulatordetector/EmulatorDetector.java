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
    private static final int DELAY = 1000;
    private float[][] sensorData = new float[10][];
    private int eventCount = 0;
    private float[] lastSensorValues;
    private Handler mHandler = new Handler();
    private SensorEventListener mSensorEventListener;
    private boolean isSleeping;

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
                                    mHandler.postDelayed(this, DELAY);
                                } else {
                                    sensorManager.unregisterListener(mSensorEventListener);
                                    processSensorData(callback);
                                }
                                isSleeping = false;
                            }
                        }
                    }, DELAY);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
        sensorManager.registerListener(mSensorEventListener, accelerometerSensor, 1000*1000*1000);
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
        callback.onDetect(sameEventCount >= 5);
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
}
