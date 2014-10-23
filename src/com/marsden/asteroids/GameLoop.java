package com.marsden.asteroids;

import com.marsden.asteroids.listener.DrawListener;
import com.marsden.asteroids.listener.UpdateListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by OEM on 27/03/14.
 * <p/>
 * This class is used to periodically update and redraw the game through the use of UpdateListeners and DrawListeners.
 * Typically, this class is started from its own Thread using:
 * <p/>
 * GameLoop gameLoop;
 * new Thread(gameLoop).start();
 * <p/>
 * However, it can also be started from the calling thread with:
 * <p/>
 * GameLoop gameLoop;
 * gameLoop.run();
 */
public class GameLoop implements Runnable {

    /*
     * The default update period to be used when none is specified.
     */
    private static final long DEF_UPDATE_PERIOD = 1000 / 40; // 40 UPS
    /*
     * The default draw period to be used when none is specified.
     */
    private static final long DEF_DRAW_PERIOD = 1000 / 50; // 50 FPS
    /*
     * The default max updates per frame to be used when none is specified.
     */
    private static final int DEF_MAX_UPDATES_PER_FRAME = 3;
    /*
     * The default of whether or not the thread is allowed to sleep to be used when none is specified.
     */
    private static final boolean DEF_SHOULD_ALLOW_SLEEPING = true;

    /*
     * How long the loop will sleep for between checks for a changed state, in milliseconds.
     */
    private static final long SLEEP_TIME_WHILE_PAUSED = 100;

    /*
     * Any listeners who wish to receive callback about periodic game updates. Currently, the GameWorld will be the
     * only listener subscribed.
     */
    private final List<UpdateListener> updateListeners = new ArrayList<UpdateListener>();
    /*
     * Any listeners who wish to receive callback about periodic game redraws. Currently, the GameView will be the
     * only listener subscribed.
     */
    private final List<DrawListener> drawListeners = new ArrayList<DrawListener>();
    /*
     * How far apart two consecutive updates should be, in milliseconds. The actual delay should never really be longer
     * than this, unless an extremely small value is set or updating is extremely CPU intensive.
     */
    private final long updatePeriod;
    /*
     * How far apart two consecutive draws should be, in milliseconds. The actual delay may be longer than this in
     * practise if the processor cannot update/draw fast enough.
     * As we're not currently using interpolation, this should really be just longer than updatePeriod as drawing more
     * than once between each update is pretty pointless.
     */
    private final long framePeriod;
    /*
     * If the game is running slow, i.e. the processor is unable to keep up with the specified updatePeriod and
     * drawPeriod, this value determines how many times the game is allowed to be updated before the results are drawn
     * to the screen. A low value may make the game slightly more jerky on slower devices, but at least the user will be
     * able to see what's happening. Set this value to 0 for no limit.
     * <p/>
     * Note that this functionality is not currently implemented as this game isn't very CPU intensive. Too much work
     * for too little gain.
     */
    private final int maximumUpdatesPerFrame;
    /*
     * If the game is running ahead of time, i.e. the processor is able to keep up with the specified updatePeriod and
     * drawPeriod, this value determines if we should sleep or not. Setting true will save CPU and battery life at the
     * cost of an almost negligible accuracy increase (if the thread doesn't wake back up in time, we could be slightly
     * late for an update or a draw, although this is unlikely). It is recommended to use true.
     */
    private final boolean shouldSleepWhileRunning;
    /*
     * The current state of the game loop. See the getLoopState() method and the LoopState enum for more details.
     */
    private LoopState loopState = LoopState.STOPPED;


    /*
     * Constructs the class using the default values above
     */
    public GameLoop() {
        this(DEF_UPDATE_PERIOD, DEF_DRAW_PERIOD, DEF_MAX_UPDATES_PER_FRAME, DEF_SHOULD_ALLOW_SLEEPING);
    }

    public GameLoop(long updatePeriod, long framePeriod, int maximumUpdatesPerFrame, boolean shouldSleepWhileRunning) {
        this.updatePeriod = updatePeriod;
        this.framePeriod = framePeriod;
        this.maximumUpdatesPerFrame = maximumUpdatesPerFrame;
        this.shouldSleepWhileRunning = shouldSleepWhileRunning;
    }


    /*
     * @return The current state of the loop (or the state which the loop will change to after the current update or
     * draw has finished if setLoopState() was called very recently).
     */
    public LoopState getLoopState() {
        return loopState;
    }

    /*
     * Sets the state that the loop will change to after the current update or draw has finished.
     * @param state The state to change to.
     */
    public void setLoopState(LoopState state) {
        // FIXME logging Logger.INSTANCE.i(((Object) this).getClass(), "setLoopState(state=" + state + ")");
        loopState = state;
    }


    /*
     * Expresses a listener's interest in receiving periodic calls to signal when the game should be updated.
     * @listener The UpdateListener to register
     */
    public void addUpdateListener(UpdateListener listener) {
        updateListeners.add(listener);
    }

    /*
     * The specified listener will no longer receive updates about game updates. If the listener was never registered,
     * nothing will happen.
     * @param listener The UpdateListener to unregister.
     * @return Whether or not the specified listener was registered in the first place.
     */
    public boolean removeUpdateListener(UpdateListener listener) {
        if (updateListeners.contains(listener)) {
            updateListeners.remove(listener);
            return true;
        } else {
            return false;
        }
    }

    /*
     * Expresses a listener's interest in receiving periodic calls to signal when the game should be drawn.
     * @listener The DrawListener to register
     */
    public void addDrawListener(DrawListener listener) {
        drawListeners.add(listener);
    }

    /*
     * The specified listener will no longer receive updates about game draws. If the listener was never registered,
     * nothing will happen.
     * @param listener The DrawListener to unregister.
     * @return Whether or not the specified listener was registered in the first place.
     */
    public boolean removeDrawListener(DrawListener listener) {
        if (drawListeners.contains(listener)) {
            drawListeners.remove(listener);
            return true;
        } else {
            return false;
        }
    }


    private void doUpdate() {
        for (UpdateListener listener : updateListeners) {
            listener.onGameUpdate();
        }
    }

    private void doDraw() {
        for (DrawListener listener : drawListeners) {
            listener.onGameRedraw();
        }
    }


    @Override
    public void run() {
        long nextUpdate, nextDraw;
        nextUpdate = nextDraw = System.currentTimeMillis();

        while (true) {
            if (loopState == LoopState.RUNNING) {

                final long time = System.currentTimeMillis();
                final long timeTilNextUpdate = nextUpdate - time;
                if (timeTilNextUpdate <= 0) {
                    nextUpdate += updatePeriod; // If we fail to update on time, the time should be made up next time.
                    doUpdate();
                } else {
                    final long timeTilNextDraw = nextDraw - time;
                    if (timeTilNextDraw <= 0) {
                        nextDraw = time + framePeriod; // If we fail to draw on time, we don't really care too much.
                        doDraw();
                    } else if (shouldSleepWhileRunning) {
                        // Save some battery and some CPU by sleeping until just before the next update or draw is scheduled.
                        final long sleepTime = (long) ((double) Math.min(timeTilNextUpdate, timeTilNextDraw) * 0.9d);
                        if (sleepTime > 0) {
                            try {
                                Thread.sleep(sleepTime);
                            } catch (InterruptedException err) {
                                // FIXME logging Logger.INSTANCE.d(((Object) this).getClass(), "Sleep interrupted while running", err);
                            }
                        }

                    }
                }

            } else if (loopState == LoopState.PAUSED) {
                try {
                    Thread.sleep(SLEEP_TIME_WHILE_PAUSED);
                } catch (InterruptedException err) {
                    // FIXME logging Logger.INSTANCE.d(((Object) this).getClass(), "Sleep interrupted while paused", err);
                }
            } else {
                break;
            }
        }
    }


    /*
     * An enumeration of the possible states this loop could be in at any one time. Note that setting the state to
     * anything from RUNNING will result in a small delay while the current update/draw completes before anything
     * about the loop's behaviour changes.
     */
    public enum LoopState {

        /*
         * The thread is currently running, i.e. the game will be updated and drawn periodically. To actually start a
         * GameLoop, GameLoop.run() must also be called either directly, or by a Thread holding an instance of a
         * GameLoop (with Thread.start()).
         */
        RUNNING(),
        /*
         * The loop is currently paused and will remain idle until the state is set back to either RUNNING or STOPPED.
         * It is unwise to leave the loop in this state for an extended period of time as it will waste battery. A
         * better idea would be to set the state to STOPPED and create a new Thread to "resume" the loop in.
         */
        PAUSED(),
        /*
         * The loop is sitting idle, waiting to be started. If setting a loop's state to this value, it will soon stop
         * issuing updates and draws and its host Thread will soon be killed. Note that trying to start a Thread after
         * it's been stopped will throw an IllegalThreadStateException. Therefore, a new Thread must be created if you
         * wish to restart a game loop after it's been stopped.
         */
        STOPPED();
    }
}
