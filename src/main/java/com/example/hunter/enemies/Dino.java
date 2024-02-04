/*
This is the Dino class, it is a subclass of the Enemy abstract superclass.
The Dino appears in the Stone Age.
The Dino class is unique from other enemies, as when it is constructed,
it will randomly select a spriteSheet to use.
*/


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
    private static final int FRAME_WIDTH = 102; // pixel width of PNG photo
    private static final int FRAME_HEIGHT = 102; // pixel height of PNG photo
    private static final int TOTAL_FRAMES = 2; // animation frame in sprite sheet
    private int frameCounter = 0; // animation begins at 0
    private int currentFrameIndex = 0; // current animation
    private final boolean isKnockedBack; // this is used to determine if an enemy is knocked back.

    public Dino(double x, double y, double speed, int initialHealth) {
        super(x, y, speed, initialHealth);

        // load sprite sheets
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

        // create the debug box
        this.debugBoundingBox = new Rectangle(x, y, FRAME_WIDTH, FRAME_HEIGHT);
        this.debugBoundingBox.setStroke(Color.TRANSPARENT);
        this.debugBoundingBox.setFill(Color.TRANSPARENT);

        // match the sprite sheet with the object's position
        this.imageView.setLayoutX(this.x);
        this.imageView.setLayoutY(this.y);
        isKnockedBack = false;
    }
    // The update method is a loop that keeps track of the enemy's state.
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
    // This method cycles through the animation of the sprite sheet
    private void updateAnimationFrame() {
        frameCounter++;
        // speed of animation
        int frameDelay = 10;
        if (frameCounter >= frameDelay) {
            currentFrameIndex = (currentFrameIndex + 1) % TOTAL_FRAMES;
            imageView.setViewport(new Rectangle2D(currentFrameIndex * FRAME_WIDTH, 0, FRAME_WIDTH, FRAME_HEIGHT));
            frameCounter = 0; // Reset the counter
        }
    }
}

