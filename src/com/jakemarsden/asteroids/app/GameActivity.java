package com.jakemarsden.asteroids.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import com.jakemarsden.asteroids.GameLoop;
import com.jakemarsden.asteroids.R;
import com.jakemarsden.asteroids.model.GameWorld;

/**
 * Created by OEM on 27/03/14.
 * <p/>
 * The actual game for the user to play.
 */
public class GameActivity extends AbstractGameActivity {

    /*
     * When this Activity finishes, information will be passed back to the caller as key/value pairs. This is the key
     * representing the user's score at the end of the game.
     */
    public static final String EXTRA_FINAL_SCORE = "com.marsden.asteroids.extras.FINAL_SCORE";


    public GameActivity() {
        // FIXME logging Logger.INSTANCE.i(getClass(), Logger.INIT + "()");
    }


    /*
     * Called by Android when the Activity is first created. We can use this to set the view and set up UI elements etc.
     * @param state The last state of the Activity, as saved in Activity.onSaveInstanceState().
     */
    @Override
    protected void onCreate(Bundle state) {
        // FIXME logging Logger.INSTANCE.v(getClass(), "onCreate(state=" + state + ")");
        super.onCreate(state);

        // Hide the title and the status bar.
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Inflates the layout as defined by the file res/layout/activity_game.xml and sets it as the current view.
        setContentView(R.layout.activity_game);
    }

    /*
     * Called by Android when the user presses the back button. We use this to display an "Are you sure?"-type
     * dialog.
     */
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setMessage(R.string.exit_prompt)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        GameActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .create()
                .show();
    }


    @Override
    protected GameWorld createGameWorld() {
        // Use the current time as the seed to ensure different results each game
        final GameWorld world = new GameWorld(this, System.currentTimeMillis(), false, true);
        world.setOnGameEndListener(new GameWorld.OnGameEndListener() {
            @Override
            public void onGameEnd(int finalScore) {
                // Closes the Activity, passing back the user's final score as the result
                final Intent result = new Intent();
                result.putExtra(EXTRA_FINAL_SCORE, finalScore);
                setResult(RESULT_OK, result);
                finish();
            }
        });
        return world;
    }

    @Override
    protected GameLoop createGameLoop() {
        return new GameLoop();
    }
}
