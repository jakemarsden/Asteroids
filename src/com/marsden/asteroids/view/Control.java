package com.marsden.asteroids.view;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.view.MotionEvent;

/**
 * Created by OEM on 15/04/14.
 * <p/>
 * Represents a control the user can use to control the game
 */
public abstract class Control {

    public final Bitmap bitmap;

    public final RectF position = new RectF();

    private int currentPointerId = MotionEvent.INVALID_POINTER_ID;


    /*
     * Loads the specified drawable resource into memory and uses it as the bitmap.
     * @param resources
     * @param bitmapResourceId
     * @param position
     */
    public Control(Resources resources, int bitmapResourceId, RectF position) {
        this(BitmapFactory.decodeResource(resources, bitmapResourceId), position);
    }

    public Control(Bitmap bitmap, RectF position) {
        this.bitmap = bitmap;
        this.position.set(position);
    }


    /*
     * Checks to see if the event is relevant to this control and if it is, one of the two below abstract methods will
     * be called.
     * @param event
     * @return True if the event was used.
     */
    public boolean handleEvent(MotionEvent event) {
        final int pointerIndex = event.getActionIndex();
        final int pointerID = event.getPointerId(pointerIndex);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                if (currentPointerId == MotionEvent.INVALID_POINTER_ID && position.contains(event.getX(pointerIndex), event.getY(pointerIndex))) {
                    currentPointerId = pointerID;
                    onPointerDown();
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (currentPointerId == pointerID) {
                    currentPointerId = MotionEvent.INVALID_POINTER_ID;
                    onPointerUp();
                    return true;
                }
                break;
        }
        return false;
    }


    /*
     * Called when a user's finger goes down on this control
     */
    protected abstract void onPointerDown();
    /*
     * Called when a user's finger (which previously went down) is lifted from the screen
     */
    protected abstract void onPointerUp();
}
