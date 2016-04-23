package com.jakemarsden.asteroids.view;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class ScoreDrawer implements Drawer<Integer> {

    private static final float PADDING_TOP = 15;

    private static final float PADDING_RIGHT = 15;

    private static final float PADDING_TEXT = 15;


    private final int screenTop;

    private final int screenRight;


    private final Paint fillPaint;

    private final Paint borderPaint;

    private final Paint textPaint;

    private final Rect scoreTextBounds = new Rect();


    public ScoreDrawer(int screenTop, int screenRight) {
        this.screenTop = screenTop;
        this.screenRight = screenRight;

        fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(0x88555555);

        borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3);
        borderPaint.setColor(0xffffffff);

        textPaint = new Paint();
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setTextSize(35);
        textPaint.setColor(0xffffffff);
    }


    @Override
    public void draw(Canvas canvas, Integer object) {
        final String scoreText = "Score: " + object;

        // measure the size of the text
        textPaint.getTextBounds(scoreText, 0, scoreText.length(), scoreTextBounds);

        // where to draw the box
        final float right = screenRight - PADDING_RIGHT,
                top = screenTop + PADDING_TOP,
                left = right - (scoreTextBounds.width() + 2f * PADDING_TEXT),
                bottom = top + (scoreTextBounds.height() + 2f * PADDING_TEXT);

        canvas.drawRect(left, top, right, bottom, fillPaint);
        canvas.drawRect(left, top, right, bottom, borderPaint);
        canvas.drawText(
                scoreText,
                left + ((right - left) - scoreTextBounds.width()) / 2f,
                bottom - ((bottom - top) - scoreTextBounds.height()) / 2f,
                textPaint
        );
    }
}
