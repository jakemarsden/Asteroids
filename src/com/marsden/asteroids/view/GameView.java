package com.marsden.asteroids.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.marsden.asteroids.InputEvent;
import com.marsden.asteroids.R;
import com.marsden.asteroids.listener.DrawListener;
import com.marsden.asteroids.listener.InputListener;
import com.marsden.asteroids.model.Asteroid;
import com.marsden.asteroids.model.GameWorld;
import com.marsden.asteroids.model.Projectile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by OEM on 27/03/14.
 * <p/>
 * This class acts as an interface between the user and the application. It defines how the game's state should be
 * displayed to the user and how the game should receive input from the user. When registered with a GameLoop, this
 * class will periodically redraw game objects to the screen and any input from the user will be delegated to any attached
 * InputListeners.
 */
public class GameView extends SurfaceView implements DrawListener {

    /*
     * Any listeners who wish to receive callback about user input. Currently, the GameWorld will be the only listener
     * subscribed. We use this list instead of directly accessing the gameWorld variable so that, in the future,
     * additional InputListeners can be added easily.
     */
    private final List<InputListener> inputListeners = new ArrayList<InputListener>();


    private Control leftRotationControl;

    private Control rightRotationControl;

    private Control accelerateControl;

    private Control fireControl;

    private Control muteControl;

    /*
     * Implementations of the Drawer interface. Each is used to draw a specific type of game object to the screen.
     */
    private PlayerDrawer playerDrawer;

    private ProjectileDrawer projectileDrawer;

    private AsteroidDrawer asteroidDrawer;

    private ScoreDrawer scoreDrawer;

    private ControlDrawer controlDrawer;


    /*
     * The state of the game, which will be periodically drawn to the screen.
     */
    private GameWorld gameWorld;

    private ControlState controlState;


    /*
     * This is how we can create instances of this class when we're not using XML layouts to define our view hierarchy.
     * We currently do not use this constructor.
     */
     public GameView(Context context) {
        this(context, null);
    }

    /*
     * Android can use this constructor to create instances of this class when we're using XML layouts to define view
     * hierarchies (which we are).
     */
    public GameView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /*
     * Android can use this constructor to create instances of this class when we're using XML layouts to define view
     * hierarchies (which we are).
     */
    public GameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                final Resources res = getResources();
                final Rect screen = new Rect(getLeft(), getTop(), getRight(), getBottom());

                // Set up the controls
                leftRotationControl = new Control(null, new RectF(screen.left, screen.top, screen.centerX(), screen.bottom)) {
                    @Override
                    protected void onPointerDown() {
                        dispatchInputEvent(InputEvent.START_PLAYER_ROTATION_LEFT);
                    }

                    @Override
                    protected void onPointerUp() {
                        dispatchInputEvent(InputEvent.STOP_PLAYER_ROTATION);
                    }
                };

                rightRotationControl = new Control(null, new RectF(screen.centerX(), screen.top, screen.right, screen.bottom)) {
                    @Override
                    protected void onPointerDown() {
                        dispatchInputEvent(InputEvent.START_PLAYER_ROTATION_RIGHT);
                    }

                    @Override
                    protected void onPointerUp() {
                        dispatchInputEvent(InputEvent.STOP_PLAYER_ROTATION);
                    }
                };

                accelerateControl = new Control(res, R.drawable.control_accelerate, new RectF(screen.left + 25, screen.bottom - 145, screen.left + 100, screen.bottom - 25)) {
                    @Override
                    protected void onPointerDown() {
                        dispatchInputEvent(InputEvent.START_PLAYER_ACCELERATION);
                    }

                    @Override
                    protected void onPointerUp() {
                        dispatchInputEvent(InputEvent.STOP_PLAYER_ACCELERATION);
                    }
                };

                final float fireButtonLeft = screen.left + (screen.width() - 192) / 2f;
                fireControl = new Control(res, R.drawable.control_fire, new RectF(fireButtonLeft, screen.bottom - 145, fireButtonLeft + 192, screen.bottom - 25)) {
                    @Override
                    protected void onPointerDown() {
                    }

                    @Override
                    protected void onPointerUp() {
                        dispatchInputEvent(InputEvent.FIRE_PROJECTILE);
                    }
                };

                muteControl = new Control(res, android.R.drawable.ic_lock_silent_mode, new RectF(screen.left + 25, screen.top + 25, screen.left + 100, screen.top + 100)) {
                    @Override
                    protected void onPointerDown() {
                    }

                    @Override
                    protected void onPointerUp() {
                        dispatchInputEvent(InputEvent.TOGGLE_AUDIO_MUTE);
                    }
                };

                // Set up the drawers
                playerDrawer = new PlayerDrawer(getResources());
                projectileDrawer = new ProjectileDrawer();
                asteroidDrawer = new AsteroidDrawer();
                scoreDrawer = new ScoreDrawer(getTop(), getRight());
                controlDrawer = new ControlDrawer();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }
        });

        // Show controls by default, but if a different state is defined by the XML layout used to define the layout,
        // find it and set it here.
        ControlState controlState = ControlState.ON;
        if (attrs != null) {
            final TypedArray typedAttrs = context.obtainStyledAttributes(attrs, R.styleable.GameView);
            if (typedAttrs != null) {
                try {
                    final int stateIndex = typedAttrs.getInt(R.styleable.GameView_controlState, -1);
                    if (stateIndex != -1) {
                        controlState = ControlState.values()[stateIndex];
                    }
                } finally {
                    typedAttrs.recycle();
                }
            }
        }
        setControlState(controlState);
    }


    /*
     * Helper method to dispatch an input event to any attached listeners.
     * @param event The event to dispatch.
     */
    private void dispatchInputEvent(InputEvent event) {
        for (InputListener listener : inputListeners) {
            listener.onUserInput(event);
        }
    }



    /*
     * Sets the world for the view to draw to the screen. This must be called once before the first call to
     * onGameRedraw() to avoid NullPointerExceptions.
     * @param world The world we will be drawing to the screen in onGameRedraw().
     */
    public void setGameWorld(GameWorld world) {
        gameWorld = world;
    }


    public ControlState getControlState() {
        return controlState;
    }

    public void setControlState(ControlState state) {
        controlState = state;
    }


    /*
     * Expresses a listener's interest in receiving calls about user input.
     * @listener The InputListener to register
     */
    public void addInputListener(InputListener listener) {
        inputListeners.add(listener);
    }

    /*
     * The specified listener will no longer receive calls about user input. If the listener was never registered,
     * nothing will happen.
     * @param listener The InputListener to unregister.
     * @return Whether or not the specified listener was registered in the first place.
     */
    public boolean removeInputListener(InputListener listener) {
        if (inputListeners.contains(listener)) {
            inputListeners.remove(listener);
            return true;
        } else {
            return false;
        }
    }


    /*
     * Redraws the entire screen from scratch. This is called periodically by a GameLoop.
     */
    @Override
    public boolean onGameRedraw() {
        // FIXME logging Logger.INSTANCE.v(((Object) this).getClass(), "onGameRedraw()");
        final SurfaceHolder holder = getHolder();
        // Lock the Canvas so we can draw on it. No one can draw on a SurfaceHolder's Canvas without the lock.
        final Canvas canvas = holder.lockCanvas();
        if (canvas == null) {
            // FIXME logging Logger.INSTANCE.d(((Object) this).getClass(), "onGameRedraw: Aborted as the canvas is null");
            return false;
        } else {
            canvas.drawColor(0xff000000);

            for (Projectile projectile : gameWorld.projectiles) {
                projectileDrawer.draw(canvas, projectile);
            }

            for (Asteroid asteroid : gameWorld.asteroids) {
                asteroidDrawer.draw(canvas, asteroid);
            }

            playerDrawer.draw(canvas, gameWorld.player);

            if (controlState == ControlState.ON) {
                controlDrawer.draw(canvas, leftRotationControl);
                controlDrawer.draw(canvas, rightRotationControl);
                controlDrawer.draw(canvas, accelerateControl);
                controlDrawer.draw(canvas, fireControl);
                controlDrawer.draw(canvas, muteControl);
            }

            scoreDrawer.draw(canvas, gameWorld.score);


            // Release the Canvas.
            holder.unlockCanvasAndPost(canvas);
            return true;
        }
    }


    /*
     * Called by Android whenever one of the user's fingers touches or releases (ACTION_UP) the screen. We use this to
     * delegate input events to any attached InputListeners.
     * @param event Describes all the pointers currently on the screen.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // FIXME logging Logger.INSTANCE.v(((Object) this).getClass(), "onTouchEvent(event=" + event + ")");
        if (controlState == ControlState.ON) {
            // Only the first control which uses the event will be called. This is important as the rotation controls
            // overlap with other controls, so we must check these only if the controls on top haven't used the event.
            if (fireControl.handleEvent(event)) {
            } else if (accelerateControl.handleEvent(event)) {
            } else if (muteControl.handleEvent(event)) {
            } else if (leftRotationControl.handleEvent(event)) {
            } else if (rightRotationControl.handleEvent(event)) {
            }
        }
        return true;
    }


    public enum ControlState {

        OFF(),
        ON();
    }
}
