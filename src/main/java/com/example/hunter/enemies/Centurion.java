/*
This is the Centurion class, it is a subclass of the Enemy abstract superclass.
The Centurion appears in the Classical Age.
*/

package com.example.hunter.enemies;

import com.example.hunter.Player;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.Objects;

public class Centurion extends Enemy {
    private static final int FRAME_WIDTH = 102;
    private static final int FRAME_HEIGHT = 102;
    private static final int TOTAL_FRAMES = 2;
    private int frameCounter = 0;
    private int frameDelay = 10;
    private int currentFrameIndex = 0;
    private boolean isKnockedBack = false;

    public Centurion(double x, double y, double speed, int initialHealth) {
        super(x, y, speed, initialHealth);
        Image spriteSheet = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/Centurion.png")));


        this.imageView = new ImageView(spriteSheet);
        this.imageView.setViewport(new Rectangle2D(0, 0, FRAME_WIDTH, FRAME_HEIGHT));

        // Properly initialize the debugBoundingBox
        this.debugBoundingBox = new Rectangle(x, y, FRAME_WIDTH, FRAME_HEIGHT);
        this.debugBoundingBox.setStroke(Color.TRANSPARENT);
        this.debugBoundingBox.setFill(Color.TRANSPARENT);

        // Position the ImageView
        this.imageView.setLayoutX(this.x);
        this.imageView.setLayoutY(this.y);
    }

    @Override
    public void update(Player player) {

        if (isKnockedBack) {
            applyKnockback(player);
        } else {
            double dx = player.getX() - this.x;
            double dy = player.getY() - this.y;

            // Normalize the direction
            double length = Math.sqrt(dx * dx + dy * dy);
            dx /= length;
            dy /= length;

            // Move towards the player
            this.x += dx * speed;
            this.y += dy * speed;
            this.imageView.setLayoutX(this.x);
            this.imageView.setLayoutY(this.y);

            // Update bounding box
            this.debugBoundingBox.setX(this.x);
            this.debugBoundingBox.setY(this.y);
            this.boundingBox = new Rectangle2D(x, y, imageView.getImage().getWidth(), imageView.getImage().getHeight());
            updateAnimationFrame();
        }
    }

    @Override
    public void receiveDamage(int damage, Player player) {
        health -= damage;
        if (health <= 0) {
            markForRemoval();
        } else {
            applyKnockback(player);
        }
    }

    @Override
    public Rectangle getDebugBoundingBox() {
        return debugBoundingBox;
    }
    private void updateAnimationFrame() {
        frameCounter++;
        if (frameCounter >= frameDelay) {
            currentFrameIndex = (currentFrameIndex + 1) % TOTAL_FRAMES;
            imageView.setViewport(new Rectangle2D(currentFrameIndex * FRAME_WIDTH, 0, FRAME_WIDTH, FRAME_HEIGHT));
            frameCounter = 0; // Reset the counter
        }
    }
}


