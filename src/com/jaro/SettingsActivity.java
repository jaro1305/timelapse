package com.jaro;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

import java.util.List;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    private String[] toStringArray(List<Size> supportedPictureSizes) {
        String[] list = new String[supportedPictureSizes.size()];
        for (int i = 0; i < supportedPictureSizes.size(); i++) {
            list[i] = supportedPictureSizes.get(i).toString();
        }
        return list;
    }

}
