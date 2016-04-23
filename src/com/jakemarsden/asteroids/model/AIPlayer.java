package com.jakemarsden.asteroids.model;

import android.graphics.RectF;
import com.jakemarsden.asteroids.InputEvent;

/**
 * @author jakemarsden
 */
public class AIPlayer extends Player {

    private static final long MIN_PROJECTILE_PERIOD = 300;
    /*
     * How long, in milliseconds, we're allowed to stick with the same target before checking it's still the best choice.
     */
    private static final long TARGET_PERIOD = 1000;

    /*
     * If the player is somewhere between pointing directly at its target and pointing FOLLOW_MARGIN radians ahead of
     * the target, it will be told to stop rotating (i.e. it is said to be pointing in the right direction).
     */
    private static final double FOLLOW_MARGIN = Math.toRadians(10);

    /*
     * The time, in milliseconds, when we will next be allowed to fire a projectile.
     */
    private long nextProjectileTime = System.currentTimeMillis();
    /*
     * The time, in milliseconds, at which we will next have to confirm our current target is the optimal choice.
     */
    private long nextTargetTime = System.currentTimeMillis();
    /*
     * The Asteroid we're currently trying to hunt down and destroy, or null if we don't currently have a target.
     */
    public Asteroid currentTarget = null;


    public AIPlayer() {
    }


    @Override
    public void update(GameWorld world) {
        super.update(world);
        final long time = System.currentTimeMillis();

        if (currentTarget != null && !currentTarget.isAlive) {
            // The current target is now invalid
            currentTarget = null;
        }
        if (currentTarget == null || time >= nextTargetTime) {
            // Find a new target
            currentTarget = findTarget(world);
            nextTargetTime = time + TARGET_PERIOD;
        }
        if (currentTarget != null) {
            // We have a target, now lets try to hit it
            double ang = Math.atan2(
                    currentTarget.position.getCentreY() - position.getCentreY(),
                    currentTarget.position.getCentreX() - position.getCentreX()
            );
            while (ang < 0) {
                ang += 2d * Math.PI; // format the angle so we can actually use it
            }

            double angDifference = angle - ang;
            if (angDifference >= -FOLLOW_MARGIN / 2d && angDifference <= FOLLOW_MARGIN / 2d) {
                // We're pointing roughly towards the target. FIRE!
                world.onUserInput(InputEvent.STOP_PLAYER_ROTATION);
                if (time >= nextProjectileTime) {
                    nextProjectileTime = time + MIN_PROJECTILE_PERIOD;
                    world.onUserInput(InputEvent.FIRE_PROJECTILE);
                }
            } else {
                // Need to rotate so we're pointing at the target.
                if (angDifference < -FOLLOW_MARGIN / 2d) {
                    world.onUserInput(InputEvent.START_PLAYER_ROTATION_RIGHT);
                } else {
                    world.onUserInput(InputEvent.START_PLAYER_ROTATION_LEFT);
                }
            }
        }
    }


    /*
     * Finds the best possible Asteroid for us to try to hit.
     * @param world
     * @return The best Asteroid for us to try to hit, or null if no targets are available.
     */
    private Asteroid findTarget(GameWorld world) {
        Asteroid closestTarget = null;
        double closestDistance = Double.MAX_VALUE;
        for (Asteroid asteroid : world.asteroids) {
            final RectF asteroidBounds = asteroid.position.getBounds();
            if (asteroidBounds.right > world.screenBounds.left
                    && asteroidBounds.left < world.screenBounds.right
                    && asteroidBounds.bottom > world.screenBounds.top
                    && asteroidBounds.top < world.screenBounds.bottom) {
                final double distance = Math.hypot(asteroid.position.getCentreX() - position.getCentreX(), asteroid.position.getCentreY() - position.getCentreY());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestTarget = asteroid;
                }
            }
        }
        return closestTarget;
    }
}
