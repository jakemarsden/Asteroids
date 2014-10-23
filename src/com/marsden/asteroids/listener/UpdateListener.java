package com.marsden.asteroids.listener;

/**
 * Created by OEM on 27/03/14.
 * <p/>
 * If a class wishes to contribute to updating the game, it must implement this interface and be registered with a
 * GameLoop.
 */
public interface UpdateListener {

    public void onGameUpdate();
}
