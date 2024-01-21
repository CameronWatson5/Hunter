package com.example.hunter;

import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

public class Enemy {
    private boolean shouldBeRemoved = false; // Flag to indicate if the enemy should be removed
    private static final int FRAME_WIDTH = 102; // Width of a single frame in the sprite sheet
    private static final int FRAME_HEIGHT = 102; // Height of a single frame

    private ImageView imageView;
    private Rectangle2D boundingBox;
    private double x, y;
    private double speed;
    private int health;
    private int frameCounter = 0;
    private int frameDelay = 10; // Number of update cycles to wait before changing frames
    private int currentFrameIndex = 0;
    private static final int TOTAL_FRAMES = 2; // Assuming 2 frames in your sprite sheet



    public Enemy(double x, double y, double speed, int initialHealth) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.health = initialHealth;

        Image spriteSheet = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/enemy1.png")));
        this.imageView = new ImageView(spriteSheet);
        this.imageView.setViewport(new Rectangle2D(0, 0, 102, 102));
        this.boundingBox = new Rectangle2D(x, y, 102, 102);

        // Position the ImageView
        this.imageView.setLayoutX(this.x);
        this.imageView.setLayoutY(this.y);
    }

    public void update(Player player) {
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
        this.boundingBox = new Rectangle2D(x, y, imageView.getImage().getWidth(), imageView.getImage().getHeight());
        updateAnimationFrame();
    }

    private void updateAnimationFrame() {
        frameCounter++;
        if (frameCounter >= frameDelay) {
            currentFrameIndex = (currentFrameIndex + 1) % TOTAL_FRAMES;
            imageView.setViewport(new Rectangle2D(currentFrameIndex * FRAME_WIDTH, 0, FRAME_WIDTH, FRAME_HEIGHT));
            frameCounter = 0; // Reset the counter
        }
    }

    public void receiveDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            markForRemoval();
        }
    }

    private void markForRemoval() {
        shouldBeRemoved = true;
    }
    public boolean shouldBeRemoved() {
        return shouldBeRemoved;
    }

    public Rectangle2D getBoundingBox() {
        return boundingBox;
    }

    public Node getImageView() {
        return imageView;
    }

    // Other methods...
}

