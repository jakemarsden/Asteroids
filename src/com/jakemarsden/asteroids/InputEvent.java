package com.jakemarsden.asteroids;

/**
 * Represents a user interaction with the game.
 *
 * @author jakemarsden
 */
public enum InputEvent {

    START_PLAYER_ROTATION_LEFT(),
    START_PLAYER_ROTATION_RIGHT(),
    STOP_PLAYER_ROTATION(),
    START_PLAYER_ACCELERATION(),
    STOP_PLAYER_ACCELERATION(),
    FIRE_PROJECTILE(),
    TOGGLE_AUDIO_MUTE();
}
