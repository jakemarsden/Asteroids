package com.jakemarsden.asteroids.view;

import android.graphics.Canvas;
import android.graphics.Paint;
import com.jakemarsden.asteroids.model.Projectile;

/**
 * @author jakemarsden
 */
public class ProjectileDrawer implements Drawer<Projectile> {

    private final Paint fillPaint;

    private final Paint borderPaint;


    public ProjectileDrawer() {
        fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(0xff000000);

        borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2f);
        borderPaint.setColor(0xffffffff);
    }


    @Override
    public void draw(Canvas canvas, Projectile object) {
        canvas.drawCircle(object.position.x, object.position.y, object.radius, fillPaint);
        canvas.drawCircle(object.position.x, object.position.y, object.radius, borderPaint);
    }
}
