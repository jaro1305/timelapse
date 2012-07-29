package com.jaro;

import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.View;

import java.util.List;

public interface CameraManager {
    void init(SurfaceHolder holder);

    Camera.Size getPreviewSize();

    boolean isPreviewRunning();

    void capturePhoto(boolean autoFocus, Size pictureSize);

    void startCapture(int delaySeconds, boolean autoFocus,  Size pictureSize);

    void stopCapture();

    boolean isCaptureRunning();

    void beginPreview(boolean autoFocus, Size pictureSize);

    void cancelPreview();

    List<Size> getSupportedPictureSizes();
}
