package com.marsden.asteroids.view;

import android.content.res.Resources;
import android.graphics.*;
import com.marsden.asteroids.R;
import com.marsden.asteroids.model.AIPlayer;
import com.marsden.asteroids.model.Player;

public class PlayerDrawer implements Drawer<Player> {

    /*
     * The image used to draw the Player is slightly larger than the Player's actual collision boundaries. This is
     * to allow for the additional thruster when moving forwards. This value defines the size of said overhang and
     * it MUST match the actual image.
     * <p/>
     * The factor here (0.4f) MUST be the same as Player.SHAPE_SCALE. Otherwise, the drawn image will stop matching
     * the collision boundaries of the Player, resulting in strange behaviour.
     */
    private static final float PLAYER_IMAGE_OVERHANG = 75f * 0.4f;

    /*
     * Represents the index of each sprite in the array 'sprites'.
     */
    private static final int
            SPRITE_NORMAL = 0,
            SPRITE_ROT_CW = 1,
            SPRITE_ROT_CCW = 2,
            SPRITE_ACC = 3,
            SPRITE_ROT_CW_ACC = 4,
            SPRITE_ROT_CCW_ACC = 5;

    /*
     * Holds rectangles which represent the position of each sprite in the sprite sheet.
     */
    private final Rect[] sprites = new Rect[6];

    private final Bitmap spriteSheet;


    public PlayerDrawer(Resources resources) {
        // Load the image file res/drawable/player_sprites.png into memory. This image is a sprite sheet containing
        // sprites for the player. Different sprites will be used depending on what the player is doing.
        spriteSheet = BitmapFactory.decodeResource(resources, R.drawable.player_sprites);

        final int spriteWidth = spriteSheet.getWidth() / sprites.length,
                spriteHeight = spriteSheet.getHeight();

        // Set up the sprite positions based on the size of the sprite sheet and the number of sprites we're expecting.
        for (int i = 0; i < sprites.length; i++) {
            sprites[i] = new Rect(i * spriteWidth, 0, (i + 1) * spriteWidth, spriteHeight);
        }
    }


    @Override
    public void draw(Canvas canvas, Player object) {
        // Decide which image from the sprite sheet to draw, based on what the player is currently doing.
        int spriteIndex;
        if (object.acceleration > 0) {
            if (object.angularVelocity > 0) {
                spriteIndex = SPRITE_ROT_CW_ACC;
            } else if (object.angularVelocity < 0) {
                spriteIndex = SPRITE_ROT_CCW_ACC;
            } else {
                spriteIndex = SPRITE_ACC;
            }
        } else {
            if (object.angularVelocity > 0) {
                spriteIndex = SPRITE_ROT_CW;
            } else if (object.angularVelocity < 0) {
                spriteIndex = SPRITE_ROT_CCW;
            } else {
                spriteIndex = SPRITE_NORMAL;
            }
        }

        // Draw the image
        // canvas.save(), canvas.rotate() and canvas.restore() are used to rotate the image.
        final RectF bounds = object.position.getBounds();
        canvas.save();
        canvas.rotate((float) Math.toDegrees(object.angle) + 90, object.position.getCentreX(), object.position.getCentreY());
        canvas.drawBitmap(spriteSheet, sprites[spriteIndex], new RectF(bounds.left, bounds.top, bounds.right, bounds.bottom + PLAYER_IMAGE_OVERHANG), null);
        canvas.restore();


        // Draw the outline of the player's collision bounds. Helpful for debugging.
        /*
        final Paint green = new Paint();
        green.setStrokeWidth(3);
        green.setColor(0xff00ff00);
        for (int i = 1; i < object.position.getVertexCount(); i++) {
            canvas.drawLine(object.position.getX(i - 1), object.position.getY(i - 1), object.position.getX(i), object.position.getY(i), green);
        }
        canvas.drawLine(object.position.getX(object.position.getVertexCount() - 1), object.position.getY(object.position.getVertexCount() - 1), object.position.getX(0), object.position.getY(0), green);
        */

        // Draw a border around the targeted Asteroid if the player is AI-controlled. Helpful for debugging.
        /*
        if (object instanceof AIPlayer) {
            final AIPlayer aiPlayer = (AIPlayer) object;
            if (aiPlayer.currentTarget != null) {
                final Paint paint = new Paint();
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(3);
                paint.setColor(0xffff0000);
                canvas.drawRect(aiPlayer.currentTarget.position.getBounds(), paint);
            }
        }
        */
    }
}
