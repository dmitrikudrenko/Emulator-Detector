package io.github.dmitrikudrenko.emulatordetector;

public interface Callback {
    void onDetect(boolean isEmulator);

    void onError(Exception exception);
}
