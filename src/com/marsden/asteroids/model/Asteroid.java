package com.marsden.asteroids.model;

import android.graphics.PointF;
import android.graphics.RectF;
import jake.utils.Random;

/**
 * Created by OEM on 27/03/14.
 * <p/>
 * This class represents an object which will spawn randomly for the user to try to avoid and destroy. It defines only
 * the object's state and behaviour and says nothing about its appearance to the user.
 */
public class Asteroid {

    /*
     * The [x,y] location of this Asteroid on the screen.
     */
    public final Polygon position;
    /*
     * The [x,y] velocity of this Asteroid, which will be added to position after each update.
     */
    public PointF velocity = new PointF();
    /*
     * The size of the Asteroid. This cannot be changed once set.
     */
    public final Size size;
    /*
     * If set to false, this asteroid will soon be removed from the game.
     */
    public boolean isAlive = true;


    public Asteroid(Random random, float centreX, float centreY, Size size) {
        this.position = RandomPolygonGenerator.createPolygon(
                random,
                centreX, centreY,
                size.minInternalRadius, size.maxExternalRadius
        );
        this.size = size;
    }


    /*
     * Moves this asteroid based on its velocity.
     */
    public void update(GameWorld world) {
        // Move
        position.offset(velocity.x, velocity.y);

        // Check if still in game
        if (!world.worldBounds.contains(position.getCentreX(), position.getCentreY())) {
            isAlive = false;
        }
    }


    /*
     * @return A human-readable string representing this class. Useful for debugging
     */
    @Override
    public String toString() {
        final RectF bounds = position.getBounds();
        return getClass().getSimpleName() + "{"
                + "position=[" + bounds.left + "," + bounds.top + "," + bounds.right + "," + bounds.bottom + "], "
                + "velocity=[" + velocity.x + "," + velocity.y + "], "
                + "size=" + size
                + "}";
    }


    /*
     * Represents the size of an asteroid. Smaller asteroids are lighter and faster than larger asteroids.
     */
    public enum Size {

        SMALL(20, 40, 52),
        MEDIUM(30, 70, 134),
        LARGE(60, 90, 234);


        private final float minInternalRadius;

        private final float maxExternalRadius;

        public final float mass;

        public final float speed;


        private Size(float minRadius, float maxRadius, float mass) {
            this.minInternalRadius = minRadius;
            this.maxExternalRadius = maxRadius;
            this.mass = mass;
            this.speed = 200f / mass; // 120
        }
    }
}
