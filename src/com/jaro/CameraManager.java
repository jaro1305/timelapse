package com.jaro;

import android.hardware.Camera;
import android.view.SurfaceHolder;

public interface CameraManager {

    void init(SurfaceHolder holder, Config config);
    void capturePhoto();
    void startCapture();
    void stopCapture();
    void beginPreview();
    void cancelPreview();

    // TODO: these have to go
    boolean isCaptureRunning();
    Camera.Size getPreviewSize();
    boolean isPreviewRunning();

}
