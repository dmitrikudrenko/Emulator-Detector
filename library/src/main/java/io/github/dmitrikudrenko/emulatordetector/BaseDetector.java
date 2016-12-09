package io.github.dmitrikudrenko.emulatordetector;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public abstract class BaseDetector implements IDetector {
    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorEventListener sensorEventListener;

    @Override
    public void detect(Context context, final Callback callback) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensor = getSensor(sensorManager);
        if (sensor != null) {
            sensorEventListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent sensorEvent) {
                    onSensorEventReceived(sensorEvent, callback);
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int i) {
                    //nothing
                }
            };
            sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_UI);
        } else callback.onError(new RuntimeException("No sensor"));
    }

    private Sensor getSensor(SensorManager sensorManager) {
        return sensorManager.getDefaultSensor(getSensorType());
    }

    protected void onDetectionComplete() {
        sensorManager.unregisterListener(sensorEventListener);
    }

    protected abstract int getSensorType();

    protected abstract void onSensorEventReceived(SensorEvent sensorEvent, Callback callback);
}
