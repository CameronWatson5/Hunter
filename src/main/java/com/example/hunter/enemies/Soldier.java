package com.example.hunter.enemies;

import com.example.hunter.GameController;
import com.example.hunter.Player;
import com.example.hunter.enemies.Enemy;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.util.Objects;

public class Soldier extends Enemy {
    private int shotsFired = 0;
    private final int maxShots = 3;
    private long lastReloadTime = 0;
    private final long reloadPause = 3000;
    private static final int FRAME_WIDTH = 102;
    private static final int FRAME_HEIGHT = 102;
    private static final int TOTAL_FRAMES = 2;
    private int frameCounter = 0;
    private int frameDelay = 10;
    private int currentFrameIndex = 0;
    private boolean isKnockedBack = false;
    private GameController gameController;
    long lastProjectileTime = 0;
    long projectileCooldown = 200;


    public Soldier(double x, double y, double speed, int initialHealth, GameController gameController) {
        super(x, y, speed, initialHealth);
        this.gameController = gameController;

        Image spriteSheet = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/Soldier.png")));
        this.imageView = new ImageView(spriteSheet);
        this.imageView.setViewport(new Rectangle2D(0, 0, FRAME_WIDTH, FRAME_HEIGHT));

        this.debugBoundingBox = new Rectangle(x, y, FRAME_WIDTH, FRAME_HEIGHT);
        this.debugBoundingBox.setStroke(Color.TRANSPARENT);
        this.debugBoundingBox.setFill(Color.TRANSPARENT);

        this.imageView.setLayoutX(this.x);
        this.imageView.setLayoutY(this.y);
    }


    public void fireProjectile(Player player) {
        long currentTime = System.currentTimeMillis();

        if (shotsFired >= maxShots) {
            if (currentTime - lastReloadTime >= reloadPause) {
                shotsFired = 0;
                lastReloadTime = currentTime;
            }
            return;
        }

        if (currentTime - lastProjectileTime >= projectileCooldown) {
            lastProjectileTime = currentTime;
            shotsFired++;

            double directionX = player.getX() - this.x;
            double directionY = player.getY() - this.y;

            double length = Math.sqrt(directionX * directionX + directionY * directionY);
            if (length != 0) {
                directionX /= length;
                directionY /= length;
            }

            double projectileSpeed = 5;
            int damage = 4;

            if (gameController != null) {
                gameController.createBulletProjectile(this.x, this.y, directionX, directionY, projectileSpeed, damage, false);
            } else {
                System.out.println("GameController is null in Soldier.fireProjectile");
            }
        }
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


            fireProjectile(player);

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