package io.github.dmitrikudrenko.emulatordetector;

import android.content.Context;

public interface IDetector {
    void detect(Context context, final Callback callback);
}
