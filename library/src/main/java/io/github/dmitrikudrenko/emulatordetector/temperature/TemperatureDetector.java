package io.github.dmitrikudrenko.emulatordetector.temperature;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.util.Log;

import io.github.dmitrikudrenko.emulatordetector.BaseDetector;
import io.github.dmitrikudrenko.emulatordetector.Callback;

public class TemperatureDetector extends BaseDetector {

    @Override
    protected int getSensorType() {
        return Sensor.TYPE_AMBIENT_TEMPERATURE;
    }

    @Override
    protected void onSensorEventReceived(SensorEvent sensorEvent, Callback callback) {
        Log.i(TemperatureDetector.class.getSimpleName(), sensorEvent.toString());
        onDetectionComplete();
    }
}
