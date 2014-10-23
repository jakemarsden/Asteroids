package com.marsden.asteroids.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import com.marsden.asteroids.GameLoop;
import com.marsden.asteroids.R;
import com.marsden.asteroids.model.GameWorld;

/**
 * Created by marsdenj10 on 11/04/14.
 * <p/>
 * The game's menu. This contains buttons for starting a game, shows information about past scores and shows a
 * computer-controlled character playing the game in the background.
 */
public class MenuActivity extends AbstractGameActivity {

    /*
     * An arbitrary number to identify our request to start the GameActivity. This isn't really necessary it's the
     * only Activity we start for a result from this here, but it's good practise to define one anyway for future
     * additions.
     */
    private static final int REQUEST_GAME = 100;
    /*
     * Android preferences store information as key/value pairs. This key represents the score from the user's most
     * recent game.
     */
    private static final String PREF_LATEST_SCORE = "com.marsden.asteroids.preferences.LATEST_SCORE";
    /*
     * Android preferences store information as key/value pairs. This key represents the highest score the user has
     * ever got.
     */
    private static final String PREF_HIGHEST_SCORE = "com.marsden.asteroids.preferences.HIGHEST_SCORE";

    /*
     * The view to display the user's latest score
     */
    private TextView txtLatestScore;
    /*
     * The view to display the user's highest score
     */
    private TextView txtHighestScore;

    private SharedPreferences preferences;


    public MenuActivity() {
        // FIXME logging Logger.INSTANCE.i(getClass(), Logger.INIT);
    }


    /*
     * Called by Android when the Activity is first created. We can use this to set the view and set up UI elements etc.
     * @param state The last state of the Activity, as saved in Activity.onSaveInstanceState().
     */
    @Override
    protected void onCreate(Bundle state) {
        // FIXME logging Logger.INSTANCE.v(getClass(), "onCreate(state=" + state + ")");
        super.onCreate(state);


        // Hide the title but leave the status bar visible
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);


        // Inflates the layout defined by the file res/layouts/activity_menu.xml and sets it as the current view.
        setContentView(R.layout.activity_menu);


        txtLatestScore = (TextView) findViewById(R.id.lblLatestScore);
        txtHighestScore = (TextView) findViewById(R.id.lblHighScore);

        findViewById(R.id.btnStartGame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // FIXME logging Logger.INSTANCE.v(((Object) this).getClass(), "onCLick(view=" + v + ")");
                final Intent intent = new Intent(MenuActivity.this, GameActivity.class);
                startActivityForResult(intent, REQUEST_GAME); // onActivityResult() will be called to give us the result
            }
        });

        findViewById(R.id.btnHelp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // FIXME logging Logger.INSTANCE.v(((Object) this).getClass(), "onCLick(view=" + v + ")");
                final Intent intent = new Intent(MenuActivity.this, HelpActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.btnExit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // FIXME logging Logger.INSTANCE.v(((Object) this).getClass(), "onClick(view=" + v + ")");
                finish(); // Close the Activity.
            }
        });


        preferences = getPreferences(MODE_PRIVATE);
        updateLatestScore(-1);
        updateHighScore(-1);
    }

    /*
     * Called by Android when another Activity which was started from here (through Activity.startActivityForResult())
     * is closed, either because the user pressed the back button or because the Activity called finish() on itself.
     * @param requestCode The same integer as was passed when calling Activity.startActivityForResult(). This is used
     *          to distinguish different Activities from each other, although we only ever start one other Activity from
     *          this class (the GameActivity).
     * @param resultCode How the Activity finished. Generally speaking, Activity.RESULT_OK is returned unless the user
     *          pressed the back button, in which case Activity.RESULT_CANCELLED is passed.
     * @param data Any information the Activity decided to pass back to us.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // FIXME logging Logger.INSTANCE.v(getClass(), "onActivityResult(request=" + requestCode + ", result=" + resultCode + ", data=" + data + ")");
        switch (requestCode) {
            case REQUEST_GAME:
                if (resultCode == RESULT_OK) {
                    int score = -1;
                    if (data != null) {
                        score = data.getIntExtra(GameActivity.EXTRA_FINAL_SCORE, -1);
                    }
                    if (score == -1) {
                        throw new IllegalArgumentException("No score was returned by the GameActivity. The GameActivity must return a score through setResult().");
                    }

                    updateLatestScore(score);
                    updateHighScore(score);
                }
        }
    }


    /*
     * If the given score is -1, read the latest score from preferences and display it. Otherwise, save the score to
     * preferences and display it.
     * @param newScore The new score, or -1.
     */
    private void updateLatestScore(int newScore) {
        if (newScore == -1) {
            newScore = preferences.getInt(PREF_LATEST_SCORE, -1);
        }
        if (newScore == -1) {
            // Game has never been played before, no previous score has been saved.
        } else {
            final SharedPreferences.Editor prefsEditor = preferences.edit();
            prefsEditor.putInt(PREF_LATEST_SCORE, newScore);
            prefsEditor.commit();
            txtLatestScore.setText(getString(R.string.latest_score, newScore));
        }
    }

    /*
     * If the given score is -1, read the high score from preferences and display it. Otherwise, if the given score is
     * higher than the one in preferences, save it to preferences and display it.
     * @param highScore The new high score, or -1.
     */
    private void updateHighScore(int highScore) {
        if (highScore == -1) {
            highScore = preferences.getInt(PREF_HIGHEST_SCORE, -1);
        }
        if (highScore == -1) {
            // Game has never been played before, no previous score has been saved.
        } else {
            int oldHighScore = preferences.getInt(PREF_HIGHEST_SCORE, -1);
            if (highScore > oldHighScore) {
                final SharedPreferences.Editor prefsEditor = preferences.edit();
                prefsEditor.putInt(PREF_HIGHEST_SCORE, highScore);
                prefsEditor.commit();
            } else {
                highScore = oldHighScore;
            }
            txtHighestScore.setText(getString(R.string.highest_score, highScore));
        }
    }


    @Override
    protected GameWorld createGameWorld() {
        // Use the current time as the seed to ensure different results each game
        return new GameWorld(this, System.currentTimeMillis(), true, false);
    }

    @Override
    protected GameLoop createGameLoop() {
        return new GameLoop();
    }
}
