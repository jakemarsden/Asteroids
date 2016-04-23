package com.jakemarsden.asteroids.model;

import android.graphics.PointF;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a closed Polygon. The 'shape' of a Polygon and its 'position' are stored separately. A Polygon will have
 * at least 3 vertexes at all times and the user has no control of a Polygon's shape once it has been created.
 *
 * @author jakemarsden
 */
public class Polygon {

    /*
     * Stuff this value into memory so we don't have to recalculate all the time.
     */
    private static final double HALF_PI = Math.PI / 2d;


    /*
     * Determines the Polygon's location. Storing the position separately means the whole array of PointFs doesn't need
     * to be updated every time the Polygon moves. During collision detection and vertex retrieval, the centre is added
     * to the vertex in question to give the actual location of the point.
     */
    private final PointF centre = new PointF();
    /*
     * Determines the Polygon's shape. The rough centre of these points should generally be [0,0].
     */
    private final List<PointF> points = new ArrayList<PointF>();
    /*
     * Stores the boundaries of the Polygon for quick collision rejection. If a point to be tested lies outside of these
     * boundaries then that point cannot lie withing the Polygon itself.
     */
    private final RectF bounds = new RectF();


    /*
     * @param centreX The x-coordinate of the rough centre of the Polygon
     * @param centreY The y-coordinate or the rough centre of the Polygon
     * @param points The points which should make up the shape of the Polygon. The rough centre of these points should
     *          generally be [0,0].
     */
    public Polygon(float centreX, float centreY, Collection<? extends PointF> points) {
        if (points.size() < 3) {
            throw new IllegalArgumentException("A Polygon must have 3+ vertices");
        } else {
            this.centre.set(centreX, centreY);
            this.points.addAll(points);
            computeBounds();
        }
    }


    /*
     * Often used with getX() and getY() when looping through points
     * @return How many vertices this Polygon has
     */
    public int getVertexCount() {
        return points.size();
    }

    /*
     * @return The x-coordinate of the ith vertex of this Polygon
     */
    public float getX(int i) {
        return points.get(i).x + centre.x;
    }

    /*
     * @return The y-coordinate of the ith vertex of this Polygon
     */
    public float getY(int i) {
        return points.get(i).y + centre.y;
    }


    /*
     * @return The x-coordinate of the rough centre of this Polygon
     */
    public float getCentreX() {
        return centre.x;
    }

    /*
     * @return The y-coordinate of the rough centre of this Polygon
     */
    public float getCentreY() {
        return centre.y;
    }


    /*
     * @return The outside boundaries of this Polygon. Modifying this Object will not effect the Polygon in any way.
     */
    public RectF getBounds() {
        return new RectF(bounds.left + centre.x, bounds.top + centre.y, bounds.right + centre.x, bounds.bottom + centre.y);
    }


    /*
     * Moves the polygon by a specified distance.
     * @param x The distance to move the Polygon along the horizontal axis
     * @param y The distance to move the Polygon along the vertical axis
     */
    public void offset(float x, float y) {
        centre.offset(x, y);
    }

    /*
     * Moves the polygon to the specified location.
     * @param x The x-coordinate to move the centre of the polygon to.
     * @param y The y-coordinate to move the centre of the polygon to.
     */
    public void offsetTo(float x, float y) {
        centre.set(x, y);
    }


    /*
     * If this Polygon contains the test point.
     * @param testX The x coordinate of the point to test
     * @param testY The y coordinate of the point to test
     * @param useQuickRejection Whether or not to first check if the boundaries overlap. Pass false if you know the
     *          point lies close to the Polygon (in this case, this check is redundant).
     * @return True if the point lies within this Polygon, including if the point lies right on the edge.
     */
    public boolean contains(float testX, float testY, boolean useQuickRejection) {
        // Instead of adding [this.centre.x, this.centre.y] to everything, let's just subtract it from the test points.
        testX -= centre.x;
        testY -= centre.y;
        if (useQuickRejection && (testX < bounds.left
                || testY < bounds.top
                || testX > bounds.right
                || testY > bounds.bottom)) {
            return false;
        } else {
            boolean inside = false;
            /*
             * Adapted from Reply #10 of http://www.java-gaming.org/index.php?topic=26013.0, by pitbuller
             * Code posted 02/09/2012. Retrieved 05/04/2014.
             */
            for (int i = 0, j = points.size() - 1; i < points.size(); j = i++) {
                if (((points.get(i).y > testY) != (points.get(j).y > testY)) &&
                        (testX < (points.get(j).x - points.get(i).x) * (testY - points.get(i).y) / (points.get(j).y - points.get(i).y) + points.get(i).x)) {
                    inside = !inside;
                }
            }
            /*
             *
             */
            return inside;
        }
    }

    /*
     * If this Polygon completely encapsulates the Polygon under test.
     * @param testPoly The Polygon to test
     * @param useQuickRejection Whether or not to first check if the boundaries overlap. Pass false if you know the
     *          Polygons are close to each other (in this case, this check is redundant)
     * @return True if this Polygon contains all of the test Polygon's vertices, including if they lie right on the
     *          edge.
     */
    public boolean contains(Polygon testPoly, boolean useQuickRejection) {
        if (useQuickRejection && quickRejectOverlappingBoundaries(testPoly)) {
            // Boundaries don't overlap so this Polygon can't contain the test Polygon.
            return false;
        } else {
            for (int i = 0; i < testPoly.getVertexCount(); i++) {
                // Needs to contain ALL points to contain. No quick rejection here as we've already done it.
                if (!contains(testPoly.getX(i), testPoly.getY(i), false)) {
                    return false;
                }
            }
            return true;
        }
    }

    /*
     * If some or all of the test Polygon is inside this Polygon
     * @param testPoly The Polygon to test
     * @param useQuickRejection Whether or not to first check if the boundaries overlap. Pass false if you know the
     *          Polygons are close to each other (in this case, this check is redundant)
     * @param True if this Polygon contains any or all of the test Polygon's vertices, including if they lie right on
     *          the edge.
     */
    public boolean overlaps(Polygon testPoly, boolean useQuickRejection) {
        if (useQuickRejection && quickRejectOverlappingBoundaries(testPoly)) {
            // Boundaries don't overlap so the Polygons can't overlap.
            return false;
        } else {
            for (int i = 0; i < testPoly.getVertexCount(); i++) {
                // Only needs to contain ONE point to overlap. No quick rejection here as we've already done it.
                if (contains(testPoly.getX(i), testPoly.getY(i), false)) {
                    return true;
                }
            }
            return false;
        }
    }

    /*
      * If some or all of the rotated test Polygon is inside this Polygon.
      * @param testPoly The Polygon to test
      * @param testPolyRotation By how much testPoly has been rotated
      * @param useQuickRejection Whether or not to first check if each point is in the boundary box. Pass false if you
      *         know the Polygons are close to each other (in this case, this check is redundant)
      * @param True if this Polygon contains any or all of the test Polygon's vertices, including if they lie right on
      *          the edge.
      */
    public boolean overlaps(Polygon testPoly, double testPolyRotation, boolean useQuickRejection) {
        // TODO this isn't optimal as we need to check both ways. However, for its current application, this works fine.
        for (int i = 0; i < testPoly.getVertexCount(); i++) {
            final PointF rotatedPoint = rotatePoint(testPoly.getX(i), testPoly.getY(i), testPoly.getCentreX(), testPoly.getCentreY(), testPolyRotation);
            if (contains(rotatedPoint.x, rotatedPoint.y, useQuickRejection)) {
                return true;
            }
        }
        return false;
    }

    /*
     * Rotates a point around the origin by a number of radians
     * @param x The x-coordinate of the point to rotate
     * @param y The y-coordinate of the point to rotate
     * @param originX The x-coordinate of the origin to rotate the point about
     * @param originY The y-coordinate of the origin to rotate the point about
     * @param angle The angle to rotate the point, in radians
     * @return The specified point, but rotated the specified number of radians about the specified origin.
     */
    private static PointF rotatePoint(float x, float y, float originX, float originY, double angle) {
        final double newX = Math.cos(angle + HALF_PI) * (x - originX) - Math.sin(angle + HALF_PI) * (y - originY) + originX;
        final double newY = Math.sin(angle + HALF_PI) * (x - originX) + Math.cos(angle + HALF_PI) * (y - originY) + originY;
        return new PointF((float) newX, (float) newY);
    }


    /*
     * If some or all of the test circle is inside this Polygon
     * @param testX The x-coordinate of the centre of the test circle
     * @param testY The y-coordinate of the centre of the test circle
     * @param testRadius The radius of the test circle
     * @param useQuickRejection Whether or not to first check if the boundaries overlap. Pass false if you know the
     *          circle is close to the Polygon (in this case, this check is redundant)
     * @param True if any of this Polygon's edges crosses the test circle, including if one is right on the edge.
     */
    public boolean overlaps(float testX, float testY, float testRadius, boolean useQuickRejection) {
        if (useQuickRejection && quickRejectOverlappingBoundaries(testX - testRadius, testY - testRadius, testX + testRadius, testY + testRadius)) {
            // Boundaries of circle don't overlap boundaries of Polygon so this Polygon can't overlap the circle.
            return false;
        } else if (contains(testX, testY, false)) {
            // Contains the centre point so the Polygon must overlap the circle.
            return true;
        } else {
            for (int i = 1; i < getVertexCount(); i++) {
                final PointF closestPointOnEdge = closestPointOnEdge(i - 1, i, testX, testY);
                if (Math.hypot(testX - closestPointOnEdge.x, testY - closestPointOnEdge.y) <= testRadius) {
                    // Only ONE edge needs to cross the circle for an overlap.
                    return true;
                }
            }
            // Check if the last edge of the Polygon crosses the circle.
            final PointF closestPointOnLastEdge = closestPointOnEdge(0, getVertexCount() - 1, testX, testY);
            return Math.hypot(testX - closestPointOnLastEdge.x, testY - closestPointOnLastEdge.y) <= testRadius;
        }
    }


    /*
     * Finds the closest point to [testX,testY] which lies on the line given by [vertex1,vertex2].
     * @param vertex1 The index of the first point which makes up the edge.
     * @param vertex2 The index of the second point which makes up the edge. This should be (vertex1 - 1) or
     *          (vertex1 + 1). However, if (vertex1 == 0), vertex2 should be (getVertexCount() - 1).
     * @param testX The x-coordinate of the point to test
     * @param testY The y-coordinate of the point to test
     * @return The point on the line between vertex1 and vertex2 which is closest to the point [testX, testY]. This
     *          could also be vertex1 or vertex2.
     */
    private PointF closestPointOnEdge(int vertex1, int vertex2, float testX, float testY) {
        final float vx1 = getX(vertex1),
                vy1 = getY(vertex1),
                vx2 = getX(vertex2),
                vy2 = getY(vertex2);

        double u = ((testX - vx1) * (vx2 - vx1) + (testY - vy1) * (vy2 - vy1)) / ((vx2 - vx1) * (vx2 - vx1) + (vy2 - vy1) * (vy2 - vy1));
        if (u > 1.0) {
            return new PointF(vx2, vy2);
        } else if (u <= 0.0) {
            return new PointF(vx1, vy1);
        } else {
            return new PointF((float) (vx2 * u + vx1 * (1.0 - u) + 0.5), (float) (vy2 * u + vy1 * (1.0 - u) + 0.5));
        }
    }


    /*
     * Quickly check if the test Polygon's boundaries intersect with ours. This check is generally performed just before
     * a more accurate and more expensive check to make sure the expensive check is really necessary.
     * @return False if the test polygon's boundaries overlap with the boundaries of this polygon.
     */
    private boolean quickRejectOverlappingBoundaries(Polygon testPoly) {
        return quickRejectOverlappingBoundaries(
                testPoly.bounds.left + testPoly.centre.x,
                testPoly.bounds.top + testPoly.centre.y,
                testPoly.bounds.right + testPoly.centre.x,
                testPoly.bounds.bottom + testPoly.centre.y
        );
    }

    /*
     * Quickly check if the test rectangle intersects with the boundaries of this Polygon. This check is generally
     * performed just before a more accurate and more expensive check to make sure the expensive check is really
     * necessary.
     * @return False if the boundaries of the test rectangle intersect with the boundaries of this Polygon.
     */
    private boolean quickRejectOverlappingBoundaries(float left, float top, float right, float bottom) {
        return left > bounds.right + centre.x
                || right < bounds.left + centre.x
                || top > bounds.bottom + centre.y
                || bottom < bounds.top + centre.y;
    }


    /*
     * Should be called whenever the shape of the Polygon changes. This, however, should only happen as part of the
     * constructor as the calling class should never have access to the Polygon's shape post-initialization. This
     * does not need to be called when the shape is just moved, i.e. by calling offset().
     */
    private void computeBounds() {
        float left, top, right, bottom;
        left = top = Float.MAX_VALUE;
        right = bottom = Float.MIN_VALUE;
        for (PointF testPoint : points) {
            if (testPoint.x < left) {
                left = testPoint.x;
            }
            if (testPoint.y < top) {
                top = testPoint.y;
            }
            if (testPoint.x > right) {
                right = testPoint.x;
            }
            if (testPoint.y > bottom) {
                bottom = testPoint.y;
            }
        }
        bounds.set(left, top, right, bottom);
    }


    /*
     * @return A human-readable string representing this class. Useful for debugging
     */
    @Override
    public String toString() {
        // Use a StringBuilder as there could be many points and String concatenation is expensive.
        final StringBuilder result = new StringBuilder(48) // enough room for the minimum 3 points
                .append(getClass().getSimpleName())
                .append("{centre=[")
                .append(centre.x)
                .append(",")
                .append(centre.y)
                .append("], points={");
        for (int i = 0; i < points.size(); i++) {
            result.append("[")
                    .append(points.get(i).x)
                    .append(",")
                    .append(points.get(i).y)
                    .append("]");
            if (i < points.size() - 1) {
                result.append(", ");
            }
        }
        return result.append("}")
                .toString();
    }
}
