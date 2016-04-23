package com.jakemarsden.asteroids.model;

import android.content.Context;
import android.media.SoundPool;
import com.jakemarsden.asteroids.InputEvent;
import com.jakemarsden.asteroids.R;

/**
 * Used to play audio during the game. This is basically just a wrapper for Android's SoundPool class.
 *
 * @author jakemarsden
 */
public class AudioController {

    /*
     * Used to actually load the audio into memory and play it.
     */
    private final SoundPool soundPool;

    /*
     * The sample IDs of sounds we may wish to play. These are given to us by SoundPool when we load each sample into
     * memory, and are used by us to later play these samples.
     */
    private final int sampleID_asteroidDestroy;

    private final int sampleID_projectileFire;

    private final int sampleID_thruster;

    /*
     * If the audio has been muted by the user, no sounds will be played.
     */
    public boolean isAudioMuted = false;

    /*
     * The stream ID for the currently playing thruster sound. We need to store this so we can stop it later. The value
     * should be 0 when no thruster sound is playing, and anything other than 0 when it is playing.
     */
    private int thrusterStreamID = 0;

    private boolean playerAccelerating = false;

    private boolean playerRotating = false;


    /*
     * @param context
     * @param maxActiveStreams How many sounds are allowed to be played at the same time before less important sounds
     *          are stopped
     * @param muted Whether or not the audio should start off muted
     */
    public AudioController(Context context, int maxActiveStreams, boolean muted) {
        soundPool = new SoundPool(maxActiveStreams, android.media.AudioManager.STREAM_MUSIC, 0);

        // load our samples into memory. The samples are stored in res/raw/<sound-file>.ogg
        sampleID_asteroidDestroy = soundPool.load(context, R.raw.sound_asteroid_destroyed, 1);
        sampleID_projectileFire = soundPool.load(context, R.raw.sound_projectile_fired, 1);
        sampleID_thruster = soundPool.load(context, R.raw.sound_thrusters, 1);

        isAudioMuted = muted;
    }


    public void handleUserInput(GameWorld world, InputEvent event) {
        if (event == InputEvent.FIRE_PROJECTILE) {
            playProjectileFireSound();
        } else if (event == InputEvent.START_PLAYER_ROTATION_LEFT || event == InputEvent.START_PLAYER_ROTATION_RIGHT) {
            playerRotating = true;
            startThrusterSound();
        } else if (event == InputEvent.STOP_PLAYER_ROTATION) {
            playerRotating = false;
            if (!playerAccelerating) {
                stopThrusterSound();
            }
        } else if (event == InputEvent.START_PLAYER_ACCELERATION) {
            playerAccelerating = true;
            startThrusterSound();
        } else if (event == InputEvent.STOP_PLAYER_ACCELERATION) {
            playerAccelerating = false;
            if (!playerRotating) {
                stopThrusterSound();
            }
        } else if (event == InputEvent.TOGGLE_AUDIO_MUTE) {
            isAudioMuted = !isAudioMuted;
            if (isAudioMuted) {
                stopThrusterSound();
            } else if (playerRotating || playerAccelerating) {
                startThrusterSound();
            }
        }
    }

    public void onAsteroidDestroyed() {
        playAsteroidDestroySound();
    }


    /*
     * Releases samples from memory once they're no longer needed. Once this method has been called, this class is no
     * longer usable and if sounds are needed again, a new instance of this class must be created.
     */
    public void release() {
        isAudioMuted = true; // Nice way of making sure sounds are no longer played.
        soundPool.release();
    }


    private void playAsteroidDestroySound() {
        playSound(sampleID_asteroidDestroy, 2, false);
    }

    private void playProjectileFireSound() {
        playSound(sampleID_projectileFire, 1, false);
    }


    private void startThrusterSound() {
        if (thrusterStreamID == 0) {
            thrusterStreamID = playSound(sampleID_thruster, 2, true);
        } else {
            // The sound is already playing, don't do anything
        }
    }

    private void stopThrusterSound() {
        if (thrusterStreamID == 0) {
            // The sound isn't actually playing at the moment, don't do anything
        } else {
            soundPool.stop(thrusterStreamID);
            thrusterStreamID = 0;
        }
    }

    /*
     * Plays a sound, returning either the stream ID of the sound so it can be stopped later, or 0 if the audio has been
     * muted and the sound is never played.
     * @param streamId
     * @param priority
     * @param shouldRepeat
     */
    private int playSound(int streamId, int priority, boolean shouldRepeat) {
        return isAudioMuted ? 0 : soundPool.play(streamId, 1, 1, priority, shouldRepeat ? -1 : 0, 1);
    }
}
