/*
This is the Cerberus class, it is a subclass of the Boss abstract class,
which itself is a subclass of the abstract enemy superclass.
The Cerberus appears in the Classical Age.
*/

package com.example.hunter.enemies;

import com.example.hunter.GameController;
import com.example.hunter.Player;
import com.example.hunter.enemies.Boss;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.util.Objects;
import java.util.Random;

public class Cerberus extends Boss {
    private static final int FRAME_WIDTH = 300; // pixel width of PNG photo
    private static final int FRAME_HEIGHT = 300; // pixel height of PNG photo
    private static final int TOTAL_FRAMES = 2; // animation frames in sprite sheet
    private int frameCounter = 0; // animation begins at 0
    private int frameDelay = 10; // speed of animation
    private int currentFrameIndex = 0; // current animation
    private boolean isKnockedBack = false; // this is used to determine if an enemy is knocked back.
    private double randomDirectionX = 0;
    private double randomDirectionY = 0;
    private int movementDuration = 600;
    private int movementTimer = 0;

    public Cerberus(double x, double y, double speed, int initialHealth,  GameController gameController) {
        super(x, y, speed, initialHealth, gameController);

        // load the sprite sheet
        Image spriteSheet = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/Cerberus.png")));

        this.imageView = new ImageView(spriteSheet);
        this.imageView.setViewport(new Rectangle2D(0, 0, FRAME_WIDTH, FRAME_HEIGHT));

        // create the debug box
        this.debugBoundingBox = new Rectangle(x, y, FRAME_WIDTH, FRAME_HEIGHT);
        this.debugBoundingBox.setStroke(Color.TRANSPARENT);
        this.debugBoundingBox.setFill(Color.TRANSPARENT);

        // match the sprite sheet with the position
        this.imageView.setLayoutX(this.x);
        this.imageView.setLayoutY(this.y);
    }

    @Override
    public void update(Player player) {

        if (isKnockedBack) {
            applyKnockback(player);
        } else {
            // Change direction
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