package com.marsden.asteroids.model;

import android.graphics.PointF;
import jake.utils.Random;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by OEM on 5/04/14.
 * <p/>
 * A static class to generate random Polygons.
 */
public class RandomPolygonGenerator {

    private RandomPolygonGenerator() {
        throw new UnsupportedOperationException();
    }


    /*
     * Creates a random Polygon with the specified arguments. Note that although concave Polygons can be produced,
     * Polygons which "go back in on themselves" cannot as the angle between each point is always positive.
     * @param rand A random-number generator to use to generate the Polygon
     * @param centreX The x-coordinate of the centre of the Polygon
     * @param centreY the y-coordinate of the centre of the Polygon
     * @param minInternalRadius The minimum distance any of the Polygon's points are allowed to be from the centre
     * @param maxExternalRadius The maximum distance any of the Polygon's points are allowed to be from the centre
     * @return A randomly-generated Polygon.
     */
    public static Polygon createPolygon(Random rand, float centreX, float centreY, double minInternalRadius, double maxExternalRadius) {
        List<PointF> points = new ArrayList<PointF>();
        points.add(new PointF((float) rand.nextDouble(minInternalRadius, maxExternalRadius), 0));
        for (double angle = randAngle(rand); angle < 2d * Math.PI; angle += randAngle(rand)) {
            final double radius = rand.nextDouble(minInternalRadius, maxExternalRadius);
            points.add(new PointF(
                    (float) (radius * Math.cos(angle)),
                    (float) (radius * Math.sin(angle))
            ));
        }
        return new Polygon(centreX, centreY, points);
    }

    private static double randAngle(Random rand) {
        return rand.nextDouble(Math.PI / 16d, Math.PI / 3d);
    }
}
