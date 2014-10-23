package com.marsden.asteroids.view;

import android.graphics.Canvas;
import android.graphics.Paint;
import com.marsden.asteroids.model.Asteroid;

public class AsteroidDrawer implements Drawer<Asteroid> {

    private final Paint paint;


    public AsteroidDrawer() {
        paint = new Paint();
        paint.setColor(0xffffffff);
    }


    @Override
    public void draw(Canvas canvas, Asteroid object) {
        // Play dot-to-dot with the vertices
        for (int i = 1; i < object.position.getVertexCount(); i++) {
            canvas.drawLine(object.position.getX(i - 1), object.position.getY(i - 1), object.position.getX(i), object.position.getY(i), paint);
        }
        // Join the last point to the first point to complete the polygon
        canvas.drawLine(object.position.getX(object.position.getVertexCount() - 1), object.position.getY(object.position.getVertexCount() - 1), object.position.getX(0), object.position.getY(0), paint);
    }
}
