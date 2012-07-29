package com.jaro;

import android.hardware.Camera;

public class Size {

    public int width;
    public int height;

    public Size(String string) {
        try {
            String[] split = string.split("x");
            width = Integer.parseInt(split[0]);
            height = Integer.parseInt(split[1]);
        } catch (Exception e) {
            throw new RuntimeException("invalid config string [" + string + "]");
        }
    }

    public Size(Camera.Size size) {
        this.width = size.width;
        this.height = size.height;

    }

    @Override
    public String toString() {
        return width + "x" + height;
    }

}
