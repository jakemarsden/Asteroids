package com.jakemarsden.asteroids.listener;

/**
 * Created by OEM on 27/03/14.
 * <p/>
 * If a class wishes to contribute to displaying the game, it must implement this interface and be registered with a
 * GameLoop.
 */
public interface DrawListener {

    public boolean onGameRedraw();
}
