package com.example.hunter.enemies;


import com.example.hunter.GameController;
import com.example.hunter.Player;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.Objects;

public class SuperSoldier extends Soldier {
    private static final int FRAME_WIDTH = 102;
    private static final int FRAME_HEIGHT = 102;
    GameController gameController;
    public SuperSoldier(double x, double y, double speed, int initialHealth, GameController gameController) {
        super(x, y, speed, initialHealth, gameController);
        this.gameController = gameController;
        // Override the sprite sheet for SuperSoldier
        Image spriteSheet = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/SuperSoldier.png")));
        this.imageView = new ImageView(spriteSheet);
        this.imageView.setViewport(new Rectangle2D(0, 0, FRAME_WIDTH, FRAME_HEIGHT));

        this.imageView.setLayoutX(this.x);
        this.imageView.setLayoutY(this.y);
    }
    // This method is used to fire projectiles after a certain time period.
    @Override
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

            double projectileSpeed = 8;
            int damage = 4;

            if (gameController != null) {
                gameController.createLaserProjectile(this.x, this.y, directionX, directionY, projectileSpeed, damage, false);
            } else {
                System.out.println("GameControllers null in Soldier.fireProjectile");
            }
        }
    }
}