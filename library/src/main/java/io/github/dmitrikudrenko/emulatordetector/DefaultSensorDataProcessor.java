package io.github.dmitrikudrenko.emulatordetector;

import android.util.Log;

import java.util.Arrays;

public class DefaultSensorDataProcessor implements ISensorDataProcessor {
    private static final double DEFAULT_INAPPROPRIATE_PERCENT = 0.5D;
    private static final int DEFAULT_D_COUNT_IN_PAIR = 2;
    private double inappropriatePercent;
    private int dCountInPair;

    public DefaultSensorDataProcessor() {
        this(DEFAULT_INAPPROPRIATE_PERCENT, DEFAULT_D_COUNT_IN_PAIR);
    }

    public DefaultSensorDataProcessor(double inappropriatePercent, int dCountInPair) {
        this.inappropriatePercent = inappropriatePercent;
        this.dCountInPair = dCountInPair;
    }

    @Override
    public boolean isEmulator(float[][] sensorData) {
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
            if (sameD >= dCountInPair) sameEventCount++;
        }
        return (double) sameEventCount / (double) sensorData.length >= inappropriatePercent;
    }
}
