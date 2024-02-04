/*
This is the Archer class, it is a subclass of the Enemy abstract superclass.
The Archer appears in the Medieval Age.
This class has the ability to fire projectiles.
This class used the Arrow class to fire projectiles.
*/

package com.example.hunter.enemies;

import com.example.hunter.GameController;
import com.example.hunter.Player;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.util.Objects;

public class Archer extends Enemy implements RangedEnemy {
    private static final int FRAME_WIDTH = 102; // pixel width of PNG photo
    private static final int FRAME_HEIGHT = 102; // pixel height of PNG photo
    private static final int TOTAL_FRAMES = 2; // animation frame in sprite sheet
    private int frameCounter = 0; // animation begins at 0
    private int currentFrameIndex = 0; // current animation
    private final boolean isKnockedBack = false; // this is used to determine if an enemy is knocked back.
    private final GameController gameController;
    long lastProjectileTime = 0; // This is used to determine when a projectile has been fired.
    long projectileCooldown = 15000; // This determines the reload time


    public Archer(double x, double y, double speed, int initialHealth, GameController gameController) {
        super(x, y, speed, initialHealth);
        this.gameController = gameController;

        // load sprite sheet
        Image spriteSheet = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/Archer.png")));
        this.imageView = new ImageView(spriteSheet);
        this.imageView.setViewport(new Rectangle2D(0, 0, FRAME_WIDTH, FRAME_HEIGHT));

        // create a debug box
        this.debugBoundingBox = new Rectangle(x, y, FRAME_WIDTH, FRAME_HEIGHT);
        this.debugBoundingBox.setStroke(Color.TRANSPARENT);
        this.debugBoundingBox.setFill(Color.TRANSPARENT);

        // match sprite sheet to object's position
        this.imageView.setLayoutX(this.x);
        this.imageView.setLayoutY(this.y);
    }
    // This method is used to fire projectiles after a certain time period.
    public void fireProjectile(Player player) {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastProjectileTime >= projectileCooldown) {
            lastProjectileTime = currentTime;

            double directionX = player.getX() - this.x;
            double directionY = player.getY() - this.y;
            double length = Math.sqrt(directionX * directionX + directionY * directionY);
            if (length != 0) {
                directionX /= length;
                directionY /= length;
            }

            double projectileSpeed = 5;
            int damage = 4;

            if (gameController != null) { // Check if gameController is not null
                playArrowSound();
                gameController.createArrowProjectile(this.x, this.y, directionX, directionY, projectileSpeed, damage, false);
            } else {
                System.out.println("GameController is null");
            }
        }
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

            fireProjectile(player);

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
    // Method to play gunshot sound
    public void playArrowSound() {
        try {
            String soundPath = Objects.requireNonNull(getClass().getResource("/sounds/arrow.mp3")).toExternalForm();
            Media sound = new Media(soundPath);
            MediaPlayer mediaPlayer = new MediaPlayer(sound);
            mediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace(); // Handle exception
        }
    }
}
