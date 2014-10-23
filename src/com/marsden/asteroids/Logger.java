package com.marsden.asteroids;

/**
 * Created by OEM on 27/03/14.
 * <p/>
 * This class is used solely for debugging purposes. It is a singleton accessible only from the INSTANCE variable. On
 * release to the PlayStore, Level.ALLOW_ALL should be changed to Level.ALLOW_NONE to cut out all log messages and
 * save a small amount of CPU and battery life.
 * <p/>
 * There are multiple "levels" you can use for logging, accessible through the Logger.Level enumeration. The Logger has
 * a separate method for each level for ease of use, named after the first letter of the level. E.g., VERBOSE logging
 * can be achieved through Logger.v() methods.
 */
public class Logger {

    /*
     * The one and only instance of this class. The only way to create more is through reflection, which you should
     * never ever need to (or want to) do.
     */
    // FIXME logging public static final Logger INSTANCE = new Logger("com.marsden.asteroids", Level.ALLOW_ALL);


    /*
     *FIXME logging
     private Logger(String tag, Level threshold) {
        super(tag, threshold);
    }
    */
}
