package com.marsden.asteroids.view;

import android.graphics.Canvas;
import android.graphics.Paint;
import com.marsden.asteroids.model.Projectile;

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
