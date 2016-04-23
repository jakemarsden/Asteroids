package com.jakemarsden.asteroids.listener;

/**
 * If a class wishes to contribute to displaying the game, it must implement this interface and be registered with a
 * GameLoop.
 *
 * @author jakemarsden
 */
public interface DrawListener {

    public boolean onGameRedraw();
}
