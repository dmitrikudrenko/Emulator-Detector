package io.github.dmitrikudrenko.emulatordetector.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import io.github.dmitrikudrenko.emulatordetector.Callback;
import io.github.dmitrikudrenko.emulatordetector.EmulatorDetector;
import io.github.dmitrikudrenko.emulatordetector.accelerometer.AccelerometerDetector;
import io.github.dmitrikudrenko.emulatordetector.gyroscope.GyroscopeDetector;

public class SampleActivity extends AppCompatActivity {
    private TextView mStatusView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_sample);
        mStatusView = (TextView) findViewById(R.id.status);

        AccelerometerDetector accelerometerDetector = AccelerometerDetector.builder()
                .setDelay(500)
                .setEventCount(5)
                //check continues 500*5 = 2500ms
                .build();

        GyroscopeDetector gyroscopeDetector = GyroscopeDetector.builder()
                .setDelay(500)
                .setEventCount(5)
                .build();
        EmulatorDetector emulatorDetector = new EmulatorDetector(accelerometerDetector, gyroscopeDetector);
        emulatorDetector.detect(this, new Callback() {
            @Override
            public void onDetect(boolean isEmulator) {
                mStatusView.setText(isEmulator ? R.string.device_is_emulator : R.string.device_is_not_emulator);
            }

            @Override
            public void onError(Exception exception) {
                mStatusView.setText(exception.getMessage());
                Log.e("onError", exception.getMessage(), exception);
            }
        });
    }
}
