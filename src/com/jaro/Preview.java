package com.jaro;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Preview extends SurfaceView {

    SurfaceHolder holder;
    public Preview(Context context) {
        super(context);
        holder = getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

}
