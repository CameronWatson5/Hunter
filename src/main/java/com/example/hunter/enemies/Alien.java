package com.example.hunter.enemies;

import com.example.hunter.GameController;
import com.example.hunter.Player;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.Objects;

public class Alien extends Boss{
    private static final int FRAME_WIDTH = 300; // pixel width of PNG photo
    private static final int FRAME_HEIGHT = 300; // pixel height of PNG photo
    private static final int TOTAL_FRAMES = 5; // animation frames in sprite sheet
    private int frameCounter = 0; // animation begins at 0
    private int frameDelay = 10; // speed of animation
    private int currentFrameIndex = 0; // current animation
    private boolean isKnockedBack = false; // this is used to determine if an enemy is knocked back.

    public Alien(double x, double y, double speed, int initialHealth,  GameController gameController) {
        super(x, y, speed, initialHealth, gameController);

        // laod sprite sheet
        Image spriteSheet = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/Alien.png")));

        this.imageView = new ImageView(spriteSheet);
        this.imageView.setViewport(new Rectangle2D(0, 0, FRAME_WIDTH, FRAME_HEIGHT));

        // create a debug box
        this.debugBoundingBox = new Rectangle(x, y, FRAME_WIDTH, FRAME_HEIGHT);
        this.debugBoundingBox.setStroke(Color.TRANSPARENT); // Red border for visibility
        this.debugBoundingBox.setFill(Color.TRANSPARENT); // Transparent fill

        // match sprite to object's positiob
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
