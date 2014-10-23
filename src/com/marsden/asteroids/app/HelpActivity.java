package com.marsden.asteroids.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import com.marsden.asteroids.R;

/**
 * Created by OEM on 25/04/14.
 * <p/>
 * Displays information about how to play the game
 */
public class HelpActivity extends Activity {

    public HelpActivity() {
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

        // Hide the title but leave the status bar visible
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_help);
    }
}