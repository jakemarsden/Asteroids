package com.marsden.asteroids.view;

import android.graphics.Canvas;

public class ControlDrawer implements Drawer<Control> {

    public ControlDrawer() {
    }


    @Override
    public void draw(Canvas canvas, Control object) {
        // Some controls don't have an image, e.g. the left/right rotation controls
        if (object.bitmap != null) {
            canvas.drawBitmap(object.bitmap, null, object.position, null);
        }
    }
}
