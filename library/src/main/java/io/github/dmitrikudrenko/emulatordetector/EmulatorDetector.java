package io.github.dmitrikudrenko.emulatordetector;

import android.content.Context;

public class EmulatorDetector implements IDetector {
    private IDetector[] detectors;
    private int receivedDetections;
    private boolean finished;

    public EmulatorDetector(IDetector... detectors) {
        this.detectors = detectors;
    }

    @Override
    public void detect(Context context, final Callback callback) {
        Callback detectionCallback = new Callback() {
            @Override
            public void onDetect(boolean isEmulator) {
                receivedDetections++;
                if (isEmulator && !finished) {
                    callback.onDetect(true);
                    finished = true;
                    return;
                }
                if (receivedDetections == detectors.length && !finished) {
                    callback.onDetect(isEmulator);
                    finished = true;
                }
            }

            @Override
            public void onError(Exception exception) {
                //ignore
            }
        };

        for (IDetector detector : detectors) {
            detector.detect(context, detectionCallback);
        }
    }
}
