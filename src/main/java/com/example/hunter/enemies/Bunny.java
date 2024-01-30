package com.example.hunter.enemies;

import com.example.hunter.GameController;
import com.example.hunter.Player;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.Objects;
import java.util.Random;

public class Bunny extends Boss {
    private static final int FRAME_WIDTH = 300;
    private static final int FRAME_HEIGHT = 300;
    private static final int TOTAL_FRAMES = 2;
    private int frameCounter = 0;
    private int frameDelay = 10;
    private int currentFrameIndex = 0;
    private boolean isKnockedBack = false;
    private double randomDirectionX = 0;
    private double randomDirectionY = 0;
    private int movementDuration = 600;
    private int movementTimer = 0;

    public Bunny(double x, double y, double speed, int initialHealth, GameController gameController) {
        super(x, y, speed, initialHealth, gameController);

        Image spriteSheet = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/Bunny.png")));

        this.imageView = new ImageView(spriteSheet);
        this.imageView.setViewport(new Rectangle2D(0, 0, FRAME_WIDTH, FRAME_HEIGHT));


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
            // Change direction every 10 seconds
            if (movementTimer <= 0) {
                Random random = new Random();
                randomDirectionX = -1 + 2 * random.nextDouble(); // generates a value between -1 and 1
                randomDirectionY = -1 + 2 * random.nextDouble(); // generates a value between -1 and 1

                // Normalize the direction vector
                double length = Math.sqrt(randomDirectionX * randomDirectionX + randomDirectionY * randomDirectionY);
                randomDirectionX /= length;
                randomDirectionY /= length;

                movementTimer = movementDuration;
            } else {
                movementTimer--;
            }

            // Apply the random movement
            this.x += randomDirectionX * speed;
            this.y += randomDirectionY * speed;
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
        System.out.println("Enemy received damage: " + damage);
        health -= damage;
        System.out.println("Enemy health after damage: " + this.health);
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
