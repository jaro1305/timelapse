package com.jaro;

import android.content.SharedPreferences;
import android.os.Environment;

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
        String folder = sharedPreferences.getString("pref_save_folder", "");
        File appRoot = new File(Environment.getExternalStorageDirectory(), "timelapse");
        return new File(appRoot, folder);
    }

    public int getDelaySeconds() {
        return sharedPreferences.getInt("pref_photo_delay", 5);
    }


    public boolean getAutoFocus() {
        return sharedPreferences.getBoolean("pref_auto_focus", false);
    }

    public Size getPictureSize() {
        String str = sharedPreferences.getString("pref_resolution", null);
        return new Size(str);
    }
}
