package com.jakemarsden.asteroids.util;

/**
 * @author jakemarsden
 */
public class Random {
    private final java.util.Random r;
    private final long seed;


    public static Random fromSeed(long seed) {
        final java.util.Random r = new java.util.Random(seed);
        return new Random(r, seed);
    }

    private Random(java.util.Random r, long seed) {
        this.r = r;
        this.seed = seed;
    }


    public long getSeed() {
        return seed;
    }

    /**
     * @return a pseudo-random integer in the range [min, max)
     */
    public int nextInt(int min, int max) {
        if (min > max) {
            String msg = String.format("Min cannot be greater than max: %d > %d", min, max);
            throw new IllegalArgumentException(msg);
        }
        return min + r.nextInt(max - min);
    }

    /**
     * @return a pseudo-random float in the range [min, max)
     */
    public float nextFloat(float min, float max) {
        if (min > max) {
            String msg = String.format("Min cannot be greater than max: %f > %f", min, max);
            throw new IllegalArgumentException(msg);
        }
        return min + (max - min) * r.nextFloat();
    }

    /**
     * @return a pseudo-random double in the range [min, max)
     */
    public double nextDouble(double min, double max) {
        if (min > max) {
            String msg = String.format("Min cannot be greater than max: %f > %f", min, max);
            throw new IllegalArgumentException(msg);
        }
        return min + r.nextDouble(max - min);
    }
}
