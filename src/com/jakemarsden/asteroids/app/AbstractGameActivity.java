package com.jakemarsden.asteroids.app;

import android.app.Activity;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import com.jakemarsden.asteroids.GameLoop;
import com.jakemarsden.asteroids.R;
import com.jakemarsden.asteroids.model.GameWorld;
import com.jakemarsden.asteroids.view.GameView;

/**
 * Represents an instance of a game of Asteroids. Subclasses must customize the game by overriding the methods
 * createGameWorld() and createGameLoop(). For a GameView to be recognised by this class, it must have been
 * given the view ID 'R.id.gameView' and added be a part of the view hierarchy set with setContentView(). That way,
 * this class can properly set it up.
 * <p/>
 * Fundamentally, a game can be split into 3 components: A GameWorld to define the game's state and behaviour; a
 * GameView to define how the user can interact with the game (how the game is displayed on screen and how input is
 * retrieved); and a GameLoop to tell the GameWorld when to update and the GameView when to redraw.
 *
 * @author jakemarsden
 */
public abstract class AbstractGameActivity extends Activity {

    public AbstractGameActivity() {
    }


    @Override
    public void setContentView(int resId) {
        super.setContentView(resId);
        onViewChanged();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        onViewChanged();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        onViewChanged();
    }


    /*
     * Called every time the Activity's view is change with setContentView(). This method attempts to find a GameView
     * with the view ID R.id.gameView. If one is present, the game is set up and started as soon as the GameView is
     * ready.
     */
    private void onViewChanged() {
        final View view = findViewById(R.id.gameView);
        if (view != null && view instanceof GameView) {
            // The view contains a GameView we are able to make use of.
            final GameView gameView = (GameView) view;
            final GameWorld gameWorld = createGameWorld();
            final GameLoop gameLoop = createGameLoop();

            // The GameWorld needs to receive callback for InputEvents so it can properly manage the game's behaviour
            gameView.addInputListener(gameWorld);
            gameView.setGameWorld(gameWorld);
            gameView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    // FIXME logging Logger.INSTANCE.i(((Object) this).getClass(), "surfaceCreated(holder=" + holder + ")");
                    // The view has been fully set up and is visible to the user. Now we can start the game.
                    gameWorld.onViewCreated(gameView.getLeft(), gameView.getTop(), gameView.getRight(), gameView.getBottom());
                    gameLoop.setLoopState(GameLoop.LoopState.RUNNING);
                    new Thread(gameLoop).start();
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    // FIXME logging Logger.INSTANCE.i(((Object) this).getClass(), "surfaceDestroyed(holder=" + holder + ")");
                    // The view is no longer visible to the user. Stop the game.
                    gameLoop.setLoopState(GameLoop.LoopState.STOPPED);
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    // FIXME logging Logger.INSTANCE.i(((Object) this).getClass(), "surfaceChanged(holder=" + holder + ", format=" + format + ", width=" + width + ", height=" + height + ")");
                }
            });

            // The GameWorld needs to periodically update the game's state.
            gameLoop.addUpdateListener(gameWorld);
            // The GameView needs to periodically redraw the game's state.
            gameLoop.addDrawListener(gameView);
        }
    }


    /*
     * Used by subclasses to set up a GameWorld instance. For example, an OnGameEnd listener could be set. This
     * method will be called every time you reset the view with setContentView(), providing a GameView has been exposed.
     * @return A GameWorld object to be used in the game.
     */
    protected abstract GameWorld createGameWorld();

    /*
     * Used by subclasses to set up a GameLoop instance. For example, the UPS and the FPS could be set here. This
     * method will be called every time you reset the view with setContentView(), providing a GameView has been exposed.
     * @return A GameLoop object to be used in the game.
     */
    protected abstract GameLoop createGameLoop();
}
