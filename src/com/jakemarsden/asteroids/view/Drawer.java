package com.jakemarsden.asteroids.view;

import android.graphics.Canvas;

/*
 * Used to draw a game object of type T on the canvas
 *
 * @author jakemarsden
 */
public interface Drawer<T> {

    /*
     * Draw the game object to the canvas
     * @param canvas The canvas to draw the object on
     * @param object The object to draw on the canvas
     */
    public void draw(Canvas canvas, T object);
}
