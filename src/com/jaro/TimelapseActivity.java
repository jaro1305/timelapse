package com.jaro;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.LinearLayout;
import com.jaro.impl.CameraManagerImpl;

import java.io.File;

public class TimelapseActivity extends Activity {

    private SurfaceView photoPreview;

    private LinearLayout layout;
    Config config;

    CameraManager cameraManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        config = new Config(PreferenceManager.getDefaultSharedPreferences(this));


        Log.i("main", "camera setup done");

        Object o = findViewById(R.layout.main);
        photoPreview = new Preview(getApplicationContext());
        photoPreview.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        cameraManager = new CameraManagerImpl();
        cameraManager.init(photoPreview.getHolder(), config);

        Camera.Size cameraPreviewSize = cameraManager.getPreviewSize();
        android.view.ViewGroup.LayoutParams layoutParams = new android.view.ViewGroup.LayoutParams(cameraPreviewSize.width, cameraPreviewSize.height);
        photoPreview.setLayoutParams(layoutParams);
        layout = (LinearLayout)findViewById(R.id.mainLayout);
        layout.addView(photoPreview);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }




    public void togglePreview(View view) {
        Button button = (Button) findViewById(R.id.toggle_preview_button);
        button.setEnabled(false);
        if (cameraManager.isPreviewRunning()) {
            cameraManager.cancelPreview();
            button.setText("start preview");
        } else {
            cameraManager.beginPreview();
            button.setText("stop preview");
        }
        button.setEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_settings:
                showSettings();
                return true;
            case R.id.clear_all:
                clearAllPhotos();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showSettings() {
        Intent settingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void clearAllPhotos() {
        clearDirectory(config.getSaveRoot());
    }

    private void clearDirectory(File directory) {
        Log.i("clearDirectory", "clearing " + directory.getAbsolutePath());
        if (directory != null && directory.exists() && directory.canWrite() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        clearDirectory(file);
                    }
                    file.delete();
                }
            }
        } else {
            Log.e("clearDirectory", "can't clean directory [" + directory + "]");
        }
    }

    private int countPhotos() {
        File saveRoot = config.getSaveRoot();
        if (saveRoot.exists() && saveRoot.canRead()) {
            return saveRoot.list().length;
        } else {
            return 0;
        }
    }

    public void takePhoto(View view) {
        cameraManager.capturePhoto();
    }

    public synchronized void toggleCapture(View view) {
        Button button = (Button)findViewById(R.id.toggle_capture_button);
        if (cameraManager.isCaptureRunning()) {
            cameraManager.stopCapture();
            button.setText("start capture");
        } else {
            cameraManager.startCapture();
            button.setText("stop capture");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }




}
