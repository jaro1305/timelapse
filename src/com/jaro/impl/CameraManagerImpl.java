package com.jaro.impl;

import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import com.jaro.CameraManager;
import com.jaro.Config;
import com.jaro.Size;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class CameraManagerImpl implements CameraManager {

    private Camera camera;
    private Camera.Size cameraPreviewSize;
    private ScheduledExecutorService cameraExecutor;
    private ScheduledFuture<Boolean> captureFuture;
    private List<Camera.Size> supportedImageSizes;
    private boolean previewRunning = false;
    private SurfaceHolder surfaceHolder;
    private Config config;

    // TODO: make configurable
    private int cnt = 0;

    private final SavePictureCallback savePictureCallback = new SavePictureCallback();

    @Override
    public Camera.Size getPreviewSize() {
        return cameraPreviewSize;
    }

    @Override
    public boolean isPreviewRunning() {
        return previewRunning;
    }

    @Override
    public void init(SurfaceHolder surfaceHolder, Config config) {
        this.config = config;
        this.surfaceHolder = surfaceHolder;
        cameraExecutor = Executors.newScheduledThreadPool(1);

        Future<Boolean> future = cameraExecutor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread thread, Throwable throwable) {
                        Log.e("camera thread", "unhandled exception", throwable);
                    }
                });
                setupCamera();
                return Boolean.TRUE;
            }
        });
        try {
            future.get();
        } catch (Exception e) {
        }
    }

    @Override
    public void capturePhoto() {
        cameraExecutor.submit(new Runnable() {
            @Override
            public void run() {
                takePhoto(config.getAutoFocus(), config.getPictureSize());
            }
        });
    }

    @Override
    public boolean isCaptureRunning() {
        return captureFuture != null;
    }


    private void setupCamera() {
        try {
            ensureCameraOpen();
            supportedImageSizes = detectSupportedImageSizes(camera);
            cameraPreviewSize = detectSmallestPreviewSize(camera);

        } catch (Exception e) {
            Log.e("cameraSetup", "camera setup failed", e);
        } finally {
            if (camera != null) {
                camera.release();
            }
            camera = null;
        }
    }

    private void fixCameraOptions(boolean autoFocus, Size pictureSize) {
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPictureSize(pictureSize.width, pictureSize.height);
        parameters.setPreviewSize(cameraPreviewSize.width, cameraPreviewSize.height);
        parameters.setRotation(90);
        parameters.set("orientation", "portrait");
        camera.setDisplayOrientation(90);
        camera.setParameters(parameters);
        if (!autoFocus) {
            camera.cancelAutoFocus();
        }
    }

    private void ensureCameraOpen() {
        if (camera == null) {
            camera = Camera.open();
        }
    }

    @Override
    public void beginPreview() {
        cameraExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (isPreviewRunning()) {
                    return;
                } else {
                    startPreview(config.getAutoFocus(), config.getPictureSize());
                }
            }
        });
    }

    @Override
    public void cancelPreview() {
        cameraExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (isPreviewRunning()) {
                    stopPreview();
                } else {
                    return;
                }
            }
        });
    }


    private void stopPreview() {
        if (camera != null) {
            camera.stopPreview();
        } else {
            Log.e("stopPreview", "camera should not be null at this point");
        }
        previewRunning = false;
    }

    private void startPreview(boolean autoFocus, Size pictureSize) {
        if (previewRunning) {
            Log.i("startPreview", "preview already running");
            return;
        } else {
            try {
                ensureCameraOpen();
                fixCameraOptions(autoFocus, pictureSize);

                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();
                previewRunning = true;
            } catch (Exception e) {
                throw new RuntimeException("failed to start preview", e);
            }
        }
    }


    private class SavePictureCallback implements Camera.PictureCallback {
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {
            File file = new File(config.getSaveRoot(), "picture" + (cnt++) + ".jpeg");
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file, false);
                fos.write(bytes);
                Log.i("camera", "captured image [file=" + file.getAbsolutePath() + "]");
            } catch (Exception e) {
                Log.e("camera", "failed to save the captured image");
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                    }
                }
                camera.stopPreview();
                Log.i("camera", "stopping preview");
            }
            Log.i("capture", "exists: " + file.exists() + " size: " + file.length());
        }
    }

    @Override
    public void startCapture() {

        if (isCaptureRunning()) {
            Log.i("toggle capture", "capture already running");
            return;
        } else {
            captureFuture = cameraExecutor.schedule(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    takePhoto(config.getAutoFocus(), config.getPictureSize());
                    if (captureFuture != null) {
                        cameraExecutor.schedule(this, config.getDelaySeconds(), TimeUnit.SECONDS);
                    }
                    return Boolean.TRUE;
                }
            }, config.getDelaySeconds(), TimeUnit.SECONDS);
        }

    }

    @Override
    public void stopCapture() {
        if (isCaptureRunning()) {
            captureFuture.cancel(false);
            captureFuture = null;
        } else {
            Log.i("toggle capture", "capture already stopped");
            return;
        }
    }

    private void takePhoto(boolean autoFocus, Size pictureSize) {
        try {
            startPreview(autoFocus, pictureSize);
            try {

                Thread.sleep(1500);
                camera.takePicture(null, null, savePictureCallback);
            } catch (Exception e) {
                Log.e("main", "failed to take picture");
            }
            cancelPreview();
        } catch (Exception e) {
            Log.e("main", "failed big " + e.getClass().getSimpleName() + " , " + e.getMessage());
        }
    }

    private List<Camera.Size> detectSupportedImageSizes(Camera camera) {
        List<Camera.Size> list = new ArrayList<Camera.Size>();
        try {
            Camera.Parameters parameters = camera.getParameters();

            for (Camera.Size size : parameters.getSupportedPictureSizes()) {
                parameters.setPictureSize(size.width, size.height);
                try {
                    camera.setParameters(parameters);
                    Log.i("camera setup", "trying picture size " + size.width + "/" + size.height + " - OK");
                    list.add(size);
                } catch (Exception e) {
                    Log.i("camera setup", "trying picture size " + size.width + "/" + size.height + " - FAIL");
                }
            }
        } catch (Exception e) {
            Log.e("detectSupportedImageSizes", "failed to detect supported image sizes", e);
        }
        if (list.isEmpty()) {
            throw new RuntimeException("failed to detect supported picture sizes");
        } else {
            return list;
        }
    }


    private Camera.Size detectSmallestPreviewSize(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        for (int i = sizes.size() - 1; i >= 0; i--) {
            Camera.Size size = sizes.get(i);
            parameters.setPreviewSize(size.width, size.height);
            try {
                camera.setParameters(parameters);
                Log.i("camera setup", "trying preview size " + size.width + "/" + size.height + " - OK");
                return size;
            } catch (Exception e) {
                Log.i("camera setup", "trying preview size " + size.width + "/" + size.height + " - FAIL");
            }

        }
        throw new RuntimeException("failed to detect the smallest preview size");
    }


}
