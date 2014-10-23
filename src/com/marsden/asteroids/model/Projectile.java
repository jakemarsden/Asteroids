package com.marsden.asteroids.model;

import android.graphics.PointF;

/**
 * Created by OEM on 27/03/14.
 * <p/>
 * This class represents an object the user is able to fire in order to destroy asteroids. It defines only the object's
 * state and behaviour and says nothing about its appearance to the user.
 */
public class Projectile {

    /*
     * The radius of the projectile.
     */
    public final float radius;
    /*
     * The [x,y] location of this Projectile on the screen.
     */
    public PointF position = new PointF();
    /*
     * The [x,y] velocity of this Projectile, which will be added to position after each update.
     */
    public PointF velocity = new PointF();
    /*
     * If set to false, this projectile will soon be removed from the game.
     */
    public boolean isAlive = true;


    public Projectile(float radius) {
        this.radius = radius;
    }


    /*
     * Updates the Projectile's position, based on its velocity.
     */
    public void update(GameWorld world) {
        // Move
        position.offset(velocity.x, velocity.y);

        // Check if still in game.
        if (!world.worldBounds.contains(position.x, position.y)) {
            isAlive = false;
        } else {
            // Check if has hit an Asteroid
            for (int i = 0; i < world.asteroids.size(); i++) {
                final Asteroid asteroid = world.asteroids.get(i);
                if (asteroid.position.overlaps(position.x, position.y, radius, true)) {
                    // We just hit an asteroid, tell the GameWorld about it and mark our self as dead.
                    world.onAsteroidDestroyed(asteroid);
                    isAlive = false;
                    break;
                }
            }
        }
    }


    /*
     * @return A human-readable string representing this class. Useful for debugging
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{"
                + "radius=" + radius + ", "
                + "position=[" + position.x + "," + position.y + "], "
                + "velocity=[" + velocity.x + "," + velocity.y + "]"
                + "}";
    }
}
