package com.jakemarsden.asteroids.model;

import android.graphics.PointF;
import android.graphics.RectF;
import com.jakemarsden.asteroids.InputEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents an object controlled by the user which they can use to destroy and evade asteroids. It defines
 * only the object's state and behaviour and says nothing about its appearance to the user.
 *
 * @author jakemarsden
 */
public class Player {

    /*
     * Cache this value so we don't have to recalculate it all the time
     */
    private static final double TWO_PI = 2d * Math.PI;
    /*
     * The coordinates of the vertices of the shape of the ship. The coordinates are clockwise and each line represents
     * one [x,y] coordinate.
     */
    private static final float[] SHAPE = {
            1, -138, // Right-hand side of the 3 pixels making up the tip of the ship
            50, -25,
            66, -25,
            66, -5,
            59, -5,
            117, 129,
            60.5f, 129,
            60.5f, 137,
            -60.5f, 137,
            -60.5f, 129,
            -117, 129,
            -59, -5,
            -66, -5,
            -66, -25,
            -50, -25,
            -1, -138 // Left-hand side of the 3 pixels making up the tip of ths ship
    };

    /*
     * How much to scale SHAPE by when initializing the Player. A value of 1 will make the Player 235x350 pixels large.
     */
    private static final float SHAPE_SCALE = 0.4f;


    /*
     * The shape and location of the Player on the screen.
     */
    public final Polygon position;
    /*
     * The [x,y] velocity of the Player, which will be added to position after each update.
     */
    public final PointF velocity = new PointF();
    /*
     * The acceleration of the Player, which will be added to velocity after each update, based on angle.
     */
    public float acceleration;
    /*
     * The direction the Player is currently pointing, in radians. The Player, and any fired Projectiles, will travel
     * in this direction.
     */
    public float angle;
    /*
     * The angular velocity, in radians per update, in which the Player is currently turning. Will be added to angle
     * after each update.
     */
    public float angularVelocity;
    /*
     * If set to false, this object will soon be removed from the game.
     */
    public boolean isAlive = true;


    public Player() {
        final List<PointF> points = new ArrayList<PointF>();
        for (int i = 1; i < SHAPE.length; i += 2) {
            final PointF point = new PointF(SHAPE[i - 1] * SHAPE_SCALE, SHAPE[i] * SHAPE_SCALE);
            points.add(point);
        }
        position = new Polygon(0, 0, points);
    }


    /*
     * Formats this Player's angle to be in the half-open range [0, 2*PI). This is not strictly necessary, although
	 * it keeps everything neat and tidy.
     */
    public final void formatAngle() {
        while (angle < 0) {
            angle += TWO_PI;
        }
        while (angle >= TWO_PI) {
            angle -= TWO_PI;
        }
    }


    protected void handleUserInput(GameWorld world, InputEvent event) {
        if (event == InputEvent.START_PLAYER_ROTATION_LEFT) {
            angularVelocity = -(float) Math.toRadians(6);

        } else if (event == InputEvent.START_PLAYER_ROTATION_RIGHT) {
            angularVelocity = (float) Math.toRadians(6);

        } else if (event == InputEvent.STOP_PLAYER_ROTATION) {
            angularVelocity = 0;

        } else if (event == InputEvent.START_PLAYER_ACCELERATION) {
            acceleration = 0.2f;

        } else if (event == InputEvent.STOP_PLAYER_ACCELERATION) {
            acceleration = 0;

        } else if (event == InputEvent.FIRE_PROJECTILE) {
            fireProjectile(world);

        }
    }

    /*
     * Updates the Player's position, based on its velocity and angle. Also makes sure this object is still on
	 * screen and has not collided with any Asteroids.
     */
    public void update(GameWorld world) {
        // rotate
        angle += angularVelocity;
        formatAngle();

        // accelerate
        velocity.offset(
                (float) (acceleration * Math.cos(angle)),
                (float) (acceleration * Math.sin(angle))
        );

        // cap the velocity
        final double vMagnitude = Math.hypot(velocity.x, velocity.y);
        if (vMagnitude > 15) {
            final double vAngle = Math.atan2(velocity.y, velocity.x);
            velocity.x = (float) (15 * Math.cos(vAngle));
            velocity.y = (float) (15 * Math.sin(vAngle));
        }

        // move
        position.offset(velocity.x, velocity.y);

        // if we're off-screen, shove us to the other side
        final RectF bounds = position.getBounds();
        float correction;
        if ((correction = world.screenBounds.left - bounds.right) > 0) {
            position.offsetTo(world.screenBounds.right - correction, position.getCentreY());
        } else if ((correction = world.screenBounds.right - bounds.left) < 0) {
            position.offsetTo(world.screenBounds.left - correction, position.getCentreY());
        }
        if ((correction = world.screenBounds.top - bounds.bottom) > 0) {
            position.offsetTo(position.getCentreX(), world.screenBounds.bottom - correction);
        } else if ((correction = world.screenBounds.bottom - bounds.top) < 0) {
            position.offsetTo(position.getCentreX(), world.screenBounds.top - correction);
        }

        // check if we've hit an asteroid
        for (Asteroid asteroid : world.asteroids) {
            if (asteroid.position.overlaps(position, angle, true)) {
                // Have hit an Asteroid
                isAlive = false;
            }
        }
    }

    private void fireProjectile(GameWorld world) {
        final Projectile projectile = new Projectile(8);
        final RectF bounds = position.getBounds();
        final double mag = bounds.height() / 2f;
        projectile.position.set(
                bounds.centerX() + (float) (mag * Math.cos(angle)),
                bounds.centerY() + (float) (mag * Math.sin(angle))
        );
        projectile.velocity.set(
                // The velocity of the player plus a bit more
                velocity.x + (float) (12 * Math.cos(angle)),
                velocity.y + (float) (12 * Math.sin(angle))
        );
        world.onSpawnProjectile(projectile);
    }


    /*
     * @return A human-readable string representing this class. Useful for debugging.
     */
    @Override
    public String toString() {
        final RectF bounds = position.getBounds();
        return getClass().getSimpleName() + "{"
                + "position=[" + bounds.left + "," + bounds.top + "," + bounds.right + "," + bounds.bottom + "], "
                + "velocity=[" + velocity.x + "," + velocity.y + "], "
                + "acceleration=" + acceleration + ", "
                + "rotation=" + angle + ", "
                + "angular-velocity=" + angularVelocity
                + "}";
    }
}
