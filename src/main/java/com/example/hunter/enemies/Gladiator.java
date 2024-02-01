/*
This is the Gladiator class, it is a subclass of the Enemy abstract superclass.
The Gladiator appears in the Classical Age.
*/

package com.example.hunter.enemies;

import com.example.hunter.Player;
import com.example.hunter.enemies.Enemy;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.util.Objects;

public class Gladiator extends Enemy {
    private static final int FRAME_WIDTH = 102;
    private static final long APPROACH_DURATION = 4000; // 4 seconds in milliseconds
    private static final long RETREAT_DURATION = 1000;
    private static final int FRAME_HEIGHT = 102;
    private static final int TOTAL_FRAMES = 2;
    private static final double EDGE_THRESHOLD = 50; // pixels
    private double gameWidth;
    private double gameHeight;

    private int frameCounter = 0;
    private int frameDelay = 10;
    private int currentFrameIndex = 0;
    private boolean isKnockedBack = false;
    private State currentState;
    private long lastStateChangeTime;
    private long stateDuration;

    public Gladiator(double x, double y, double speed, int initialHealth, double gameWidth, double gameHeight) {
        super(x, y, speed, initialHealth);
        Image spriteSheet = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/gladiator.png")));

        this.imageView = new ImageView(spriteSheet);
        this.imageView.setViewport(new Rectangle2D(0, 0, FRAME_WIDTH, FRAME_HEIGHT));

        // Properly initialize the debugBoundingBox
        this.debugBoundingBox = new Rectangle(x, y, FRAME_WIDTH, FRAME_HEIGHT);
        this.debugBoundingBox.setStroke(Color.TRANSPARENT); // Red border for visibility
        this.debugBoundingBox.setFill(Color.TRANSPARENT); // Transparent fill

        // Position the ImageView
        this.imageView.setLayoutX(this.x);
        this.imageView.setLayoutY(this.y);

        currentState = State.APPROACHING; // Start with approaching
        lastStateChangeTime = System.currentTimeMillis();

    }

    @Override
    public void update(Player player) {
        if (isKnockedBack) {
            applyKnockback(player);
        } else {
            long currentTime = System.currentTimeMillis();
            if (currentState == State.APPROACHING && currentTime - lastStateChangeTime > APPROACH_DURATION) {
                currentState = State.RETREATING;
                lastStateChangeTime = currentTime;
            } else if (currentState == State.RETREATING && currentTime - lastStateChangeTime > RETREAT_DURATION) {
                currentState = State.APPROACHING;
                lastStateChangeTime = currentTime;
            }

            double dx = player.getX() - this.x;
            double dy = player.getY() - this.y;

            // Normalize the direction
            double length = Math.sqrt(dx * dx + dy * dy);
            if (length != 0) {
                dx /= length;
                dy /= length;
            }
            if (currentState == State.RETREATING) {
                dx = -dx;
                dy = -dy;
            }

            // Move towards the player
            this.x += dx * speed;
            this.y += dy * speed;

            // Update the gladiator's position and bounding box
            this.imageView.setLayoutX(this.x);
            this.imageView.setLayoutY(this.y);
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
            //isKnockedBack=true;
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
    private enum State {
        APPROACHING,
        RETREATING
    }
}