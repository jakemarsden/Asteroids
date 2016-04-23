package com.jakemarsden.asteroids.model;

import android.content.Context;
import android.graphics.RectF;
import com.jakemarsden.asteroids.InputEvent;
import com.jakemarsden.asteroids.listener.InputListener;
import com.jakemarsden.asteroids.listener.UpdateListener;
import jake.utils.Random;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by OEM on 27/03/14.
 * <p/>
 * Defines the state and behaviour of the game. The game contains 3 types
 * of game objects: the Player, Asteroids and Projectiles. The state of these
 * objects is updated every time the GameLoop calls the UpdateListener.onGameUpdate()
 * method defined by this class.
 */
public class GameWorld implements InputListener, UpdateListener {

    /*
     * Any input given to us will be held in this queue until the next update. This isolates the friction between the
     * GameLoop thread and Android's main thread (which passes us the input) to just this object. Also note that
     * ArrayBlockingQueues are thread-safe so we don't need to implement any thread synchronization ourselves.
     */
    private final ArrayBlockingQueue<InputEvent> inputQueue = new ArrayBlockingQueue<InputEvent>(20);
    /*
     * The boundaries of the screen. This must be set with onViewCreated() before the first call to onGameUpdate().
     */
    public final RectF screenBounds = new RectF();
    /*
     * The boundaries of the game, which should be slightly larger than the screen. Objects leaving these boundaries
     * will be removed from the game. This is necessary as using the screen bounds would cause objects to disappear
     * when only half off the screen. This must be set with onViewCreated() before the first call to onGameUpdate().
     */
    public final RectF worldBounds = new RectF();
    /*
     * The random generator to use for any random events in the game. This generator must be used to generate all of the
     * game's random events to ensure consistent results. When the same random seed is used and when the same input is
     * supplied, game results must be the same. This could be helpful in the future for 1) a multi-player option (handing
     * both instances the same seed and same input == same results for each player) or 2) a replay mechanism (recording
     * only the random seed and user input would result in identical playback).
     */
    public final Random randomGenerator;
    /*
     * True if the computer is controlling the player, false if the user is controlling the player.
     */
    private final boolean usingAIPlayer;

    /*
     * The listener to notify when the game ends. If set to null, no listener will be notified.
     */
    private OnGameEndListener onGameEndListener = null;
    /*
     * It may take us one or two updates to completely bring the game to a halt (first, the Activity must close. Then,
     *  the view's surfaceDestroyed() method must be called and then the GameLoop's thread must be stopped). This
     *  prevents bad things from happening, such as calling the onGameEndListener twice when we didn't mean to.
     */
    private boolean gameAlreadyFinished = false;

    /*
     * Any game objects currently a part of the game.
     */
    public Player player;
    public final List<Asteroid> asteroids = new ArrayList<Asteroid>();

    public final List<Projectile> projectiles = new ArrayList<Projectile>();

    public final AudioController audioController;

    public int score = 0;

    /*
     * How probable it is that an Asteroid will spawn each update. The larger this number, the lower the probability of
     * an Asteroid spawning.
     */
    public float asteroidSpawnProbability = 100;


    /*
     * @param context The context to use to access resources and system services etc.
     * @param seed The seed to use for the random generator. Two games with identical seeds and identical user input
     *          will always produce identical results.
     * @param aiControlled Pass true if you wish the player to be controlled by the computer, false if you wish the
     *          player to be controlled by the user.
     * @param allowSound Whether or not sound is to be enabled at the start of the game. Note that user input can alter
     *          this after the game has started.
     */
    public GameWorld(Context context, long seed, boolean aiControlled, boolean allowSound) {
        randomGenerator = new Random(seed);
        usingAIPlayer = aiControlled;
        audioController = new AudioController(context, 5, !allowSound);
    }


    /*
     * @param The listener to be notified when the game ends, or null to remove the current listener.
     */
    public void setOnGameEndListener(OnGameEndListener listener) {
        onGameEndListener = listener;
    }


    /*
     * Must be called once after the view is ready to go but before the first call to onGameUpdate(). Here we set
     * up the game's boundaries and object locations.
     * @param left The position of the left edge of the screen.
     * @param top The position of the top edge of the screen.
     * @param right The position of the right edge of the screen.
     * @param bottom The position of the bottom edge of the screen.
     */
    public void onViewCreated(float left, float top, float right, float bottom) {
        screenBounds.set(left, top, right, bottom);
        worldBounds.set(left - 100f, top - 100f, right + 100f, bottom + 100f);
        onSpawnPlayer();

        for (int i = 0; i < 3; i++) {
            onSpawnAsteroid();
        }
    }


    /*
     * Called by a GameView as the user interacts with the game, or by any game objects with AI. Adds each event to a
     * queue to be handled during the next game update. This prevents Android's main thread (which gives us the input)
     * and the game's update thread from trying to access game objects simultaneously, which will cause nasty
     * ConcurrentModificationExceptions.
     */
    @Override
    public void onUserInput(InputEvent event) {
        //Logger.INSTANCE.v(getClass(), "onUserInput(event=" + event + ")");
        try {
            inputQueue.put(event);
        } catch (InterruptedException err) {
            // Should never really be thrown as we aren't using InterruptedExceptions to stop threads and the
            // ArrayBlockingQueue is initialized with plenty of room for normal use.
            // FIXME logging Logger.INSTANCE.d(getClass(), "Interrupted while queueing input: " + event);
        }
    }


    /*
     * Drain the queue of InputEvents and deal with each accordingly. Then, update the game's state by moving objects
     * and detecting collisions etc. This should be called periodically by a GameLoop.
     */
    // Used for debugging to make sure updates aren't taking too long. If they are either the timing of the GameLoop
    // should be changed or this method should be made less intensive.
    private long fastestUpdate = Long.MAX_VALUE;
    private long slowestUpdate = Long.MIN_VALUE;

    @Override
    public void onGameUpdate() {
        final long startTime = System.currentTimeMillis();

        // FIXME logging Logger.INSTANCE.v(getClass(), "onGameUpdate() Note: inputQueue size: " + inputQueue.size() + ", asteroids: " + asteroids.size());

        // Drain the inputQueue and delegate each InputEvent appropriately.
        InputEvent event;
        while ((event = inputQueue.poll()) != null) {
            if (event != InputEvent.TOGGLE_AUDIO_MUTE) {
                player.handleUserInput(this, event);
            }
            audioController.handleUserInput(this, event);
        }

        // Update the player
        player.update(this);
        if (!player.isAlive) {
            onFinishGame();
        }

        // Update the projectiles
        for (int i = 0; i < projectiles.size(); i++) {
            final Projectile projectile = projectiles.get(i);
            projectile.update(this);
            if (!projectile.isAlive) {
                // FIXME logging Logger.INSTANCE.v(getClass(), "Projectile left the game: " + projectile);
                projectiles.remove(i);
                i--;
            }
        }

        // Update the asteroids
        for (int i = 0; i < asteroids.size(); i++) {
            final Asteroid asteroid = asteroids.get(i);
            asteroid.update(this);
            if (!asteroid.isAlive) {
                // FIXME logging Logger.INSTANCE.v(getClass(), "Asteroid left the game: " + asteroid);
                asteroids.remove(i);
                i--;
            }
        }

        // Should we spawn a new Asteroid?
        if (randomGenerator.nextInt(0, (int) asteroidSpawnProbability) == 0) {
            onSpawnAsteroid();
        }

        // Increase the difficulty
        // The probability after a certain number of updates (providing it isn't 10) will be: P = 100*[1-(1/5000)]^updates
        asteroidSpawnProbability -= asteroidSpawnProbability / 5000;

        // The hardest the game can get is 1 asteroid every 10 updates (4 every second)
        if (asteroidSpawnProbability < 10) {
            asteroidSpawnProbability = 10;
        }


        // Used for debugging to make sure updates aren't taking too long.
        final long time = System.currentTimeMillis() - startTime;
        if (time > slowestUpdate) {
            slowestUpdate = time;
            // FIXME logging Logger.INSTANCE.i(getClass(), "new slowest update: " + time);
        }
        if (time < fastestUpdate) {
            fastestUpdate = time;
            // FIXME logging Logger.INSTANCE.i(getClass(), "new fastest update: " + time);
        }
    }


    public void onSpawnPlayer() {
        player = usingAIPlayer ? new AIPlayer() : new Player();
        player.position.offset(
                screenBounds.centerX(),
                screenBounds.centerY()
        );
        player.angle = -(float) (Math.PI / 2d); // Start pointing towards the top of the screen
    }

    public void onSpawnProjectile(Projectile projectile) {
        // FIXME logging Logger.INSTANCE.v(getClass(), "Projectile entering the game: " + projectile);
        projectiles.add(projectile);
    }

    /*
     * Spawns an asteroid at the left, top, right or bottom of the screen.
     */
    public void onSpawnAsteroid() {
        float positionX, positionY;
        double direction;
        do {
            // Generate a new random position and direction for the asteroid. The asteroid will be just to the left,
            // top, right or bottom of the screen.
            switch (randomGenerator.nextInt(0, 4)) {
                case 0: // left
                    positionX = worldBounds.left;
                    positionY = randomGenerator.nextFloat(worldBounds.top, worldBounds.bottom);
                    direction = Math.toRadians(randomGenerator.nextFloat(-67.5f, 67.5f));
                    break;

                case 1: // top
                    positionX = randomGenerator.nextFloat(worldBounds.left, worldBounds.right);
                    positionY = worldBounds.top;
                    direction = Math.toRadians(randomGenerator.nextFloat(67.5f, 157.5f));
                    break;

                case 2: // right
                    positionX = worldBounds.right;
                    positionY = randomGenerator.nextFloat(worldBounds.top, worldBounds.bottom);
                    direction = Math.toRadians(randomGenerator.nextFloat(67.5f, 247.5f));
                    break;

                case 3: // bottom
                    positionX = randomGenerator.nextFloat(worldBounds.left, worldBounds.right);
                    positionY = worldBounds.bottom;
                    direction = Math.toRadians(randomGenerator.nextFloat(157.5f, 337.5f));
                    break;

                default: // should be impossible
                    throw new RuntimeException("Random int was not in the range [0-3]");
            }

            // The centre of the new asteroid must be at least 130 pixels from the centre of the player
        } while (Math.hypot(positionX - player.position.getCentreX(), positionY - player.position.getCentreX()) <= 130);

        final Asteroid.Size size = randomAsteroidSize();
        onSpawnAsteroid(size, positionX, positionY, (float) (size.speed * Math.cos(direction)), (float) (size.speed * Math.sin(direction)));
    }

    /*
     * Helper method used by onSpawnAsteroid() and onAsteroidDestroyed() to create an asteroid with the specified
     * size, position and velocity.
     * @param size
     * @param centerX
     * @param centerY
     * @param velocityX
     * @param velocityY
     */
    public void onSpawnAsteroid(Asteroid.Size size, float centerX, float centerY, float velocityX, float velocityY) {
        final Asteroid asteroid = new Asteroid(randomGenerator, centerX, centerY, size);
        asteroid.velocity.set(velocityX, velocityY);

        // FIXME logging Logger.INSTANCE.v(getClass(), "Asteroid entering the game: " + asteroid);
        asteroids.add(asteroid);
    }

    /*
     * Removes the asteroid from the game, increases the score and, depending on the asteroid's size, spawns some more
     * asteroids to take its place (i.e. larger asteroids will "split" into smaller ones).
     * @param asteroid The asteroid which was destroyed.
     */
    public void onAsteroidDestroyed(Asteroid asteroid) {
        asteroid.isAlive = false;
        score++;

        if (asteroid.size != Asteroid.Size.SMALL) {
            // Spawn some new asteroids to make the destroyed asteroid "split" into smaller ones
            Asteroid.Size newSize = asteroid.size == Asteroid.Size.LARGE ? Asteroid.Size.MEDIUM : Asteroid.Size.SMALL;

            // Chance of having 2 asteroids spawn is 4/5. Chance of having 3 asteroids spawn is 1/5.
            final int newAsteroidCount = randomGenerator.nextInt(0, 6) == 0 ? 3 : 2;
            final double oldAngle = Math.atan2(asteroid.velocity.y, asteroid.velocity.x);

            // The second new asteroid will travel with the same speed and direction as the destroyed one. The others
            // will travel with different speeds and directions.
            boolean sameVelocity = true;
            for (int i = 0; i < newAsteroidCount; i++) {

                float velocityX, velocityY;
                if (sameVelocity) {
                    velocityX = asteroid.velocity.x;
                    velocityY = asteroid.velocity.y;
                } else {
                    final double newDirection = oldAngle + randomGenerator.nextDouble(-Math.PI / 4f, Math.PI / 4f);
                    velocityX = (float) (newSize.speed * Math.cos(newDirection));
                    velocityY = (float) (newSize.speed * Math.sin(newDirection));
                }
                onSpawnAsteroid(newSize, asteroid.position.getCentreX(), asteroid.position.getCentreY(), velocityX, velocityY);
                sameVelocity = !sameVelocity;
            }
        }

        audioController.onAsteroidDestroyed(); // Play a sound
    }


    /*
     * Used when the game should end. If we're using an AI-controlled player, just continue the game as if nothing
      * happened. If we're using a user-controlled player, the player just lost the game.
     */
    private void onFinishGame() {
        if (usingAIPlayer) {
            // Continue as if nothing ever happened
            player.isAlive = true;
        } else if (!gameAlreadyFinished) {
            gameAlreadyFinished = true;

            // As it can take the game a second to actually stop updating, freeze all of our game objects so it doesn't
            // look so silly when game objects keep moving after the player's hit an asteroid.
            player.velocity.set(0, 0);
            player.angularVelocity = 0;
            for (Projectile projectile : projectiles) {
                projectile.velocity.set(0, 0);
            }
            for (Asteroid asteroid : asteroids) {
                asteroid.velocity.set(0, 0);
            }

            if (onGameEndListener != null) {
                onGameEndListener.onGameEnd(score); // Notify the listener
            }
            audioController.release(); // release any cached audio from memory
        }
    }


    /*
     * Helper method to generate a random asteroid size.
     * @return A randomly generated size.
     */
    private Asteroid.Size randomAsteroidSize() {
        Asteroid.Size size;

        final int randInt = randomGenerator.nextInt(0, 4);
        switch (randInt) {
            case 0: // Has a 1/2 chance
            case 1:
                size = Asteroid.Size.SMALL;
                break;

            case 2: // Has a 1/4 chance
                size = Asteroid.Size.MEDIUM;
                break;

            case 3: // has a 1/4 chance
                size = Asteroid.Size.LARGE;
                break;

            default: // should be impossible
                throw new IllegalArgumentException("Random int was not in range [0-3]");
        }
        return size;
    }


    /*
     * Used to tell someone when the game ends.
     */
    public interface OnGameEndListener {

        public void onGameEnd(int finalScore);
    }
}
