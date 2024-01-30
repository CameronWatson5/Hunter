package com.example.hunter.enemies;

import com.example.hunter.Player;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.util.Objects;
import java.util.Random;

public class Dino extends Enemy {
    private static final int FRAME_WIDTH = 102;
    private static final int FRAME_HEIGHT = 102;
    private static final int TOTAL_FRAMES = 2;
    private int frameCounter = 0;
    private int frameDelay = 10;
    private int currentFrameIndex = 0;
    private boolean isKnockedBack = false;

    public Dino(double x, double y, double speed, int initialHealth) {
        super(x, y, speed, initialHealth);

        Image spriteSheet1 = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/Dino1.png")));
        Image spriteSheet2 = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/Dino2.png")));
        Image spriteSheet3 = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/Dino3.png")));
        Image spriteSheet4 = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/Dino4.png")));
        Image spriteSheet5 = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/Dino5.png")));
        Image spriteSheet6 = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/Dino6.png")));
        // Randomly select a sprite sheet
        Random random = new Random();
        int randomNumber = random.nextInt(6);

// Select the sprite sheet based on the generated number
        Image selectedSpriteSheet;
        switch (randomNumber) {
            case 0:
                selectedSpriteSheet = spriteSheet1;
                break;
            case 1:
                selectedSpriteSheet = spriteSheet2;
                break;
            case 2:
                selectedSpriteSheet = spriteSheet3;
                break;
            case 3:
                selectedSpriteSheet = spriteSheet4;
                break;
            case 4:
                selectedSpriteSheet = spriteSheet5;
                break;
            case 5:
                selectedSpriteSheet = spriteSheet6;
                break;
            default:
                selectedSpriteSheet = null;
                break;
        }

        this.imageView = new ImageView(selectedSpriteSheet);
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
        System.out.println("Enemy received damage: " + damage);
        health -= damage;
        System.out.println("Enemy health after damage: " + this.health);
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
}

