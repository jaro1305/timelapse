package com.jaro;

import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: jaro
 * Date: 7/29/12
 * Time: 8:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class Config {

    private SharedPreferences sharedPreferences;

    public Config(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public File getSaveRoot() {
        String relativeFolder = sharedPreferences.getString("pref_save_folder", "");
        File appRoot = new File(Environment.getExternalStorageDirectory(), "timelapse");
        File saveRoot = new File(appRoot, relativeFolder);
        if (!saveRoot.exists()) {
            boolean success = saveRoot.mkdirs();
            if (!success) {
                Log.e("getSaveRoot", "failed to create save directory");
            }
        }
        Log.i("getSaveRoot", "using save directory [" + saveRoot.getAbsolutePath() + "]");
        return saveRoot;
    }

    public int getDelaySeconds() {
        return Integer.parseInt(sharedPreferences.getString("pref_photo_delay", "5"));
    }


    public boolean getAutoFocus() {
        return sharedPreferences.getBoolean("pref_auto_focus", false);
    }

    public Size getPictureSize() {
        String str = sharedPreferences.getString("pref_resolution", null);
        return new Size(str);
    }
}
