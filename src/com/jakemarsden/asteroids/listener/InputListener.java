package com.jakemarsden.asteroids.listener;

import com.jakemarsden.asteroids.InputEvent;

/**
 * Created by OEM on 27/03/14.
 * <p/>
 * If a class wishes to contribute to handling the game's user input, it must implement this interface and be registered
 * with the class dispatching the input (currently the GameView).
 */
public interface InputListener {

    public void onUserInput(InputEvent event);
}
