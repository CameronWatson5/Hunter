/*
This is the Enemy abstract superclass.
This class has the all the game's bosses and enemies as subclasses of it.
*/

package com.example.hunter.enemies;

import com.example.hunter.Player;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

public abstract class Enemy {
    protected boolean shouldBeRemoved = false; // set to true to remove object
    protected ImageView imageView; // visual representation of object
    protected Rectangle2D boundingBox; // used for collision detection
    protected Rectangle debugBoundingBox; // used for bug testing
    protected double x, y;
    protected double speed;
    protected int frameCounter;
    protected int health;
    protected int currentFrameIndex;
    protected int TOTAL_FRAMES;
    protected int FRAME_WIDTH;
    protected int FRAME_HEIGHT;

    public Enemy(double x, double y, double speed, int initialHealth) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.health = initialHealth;
    }
    // set the enemies' coordinates
    public void setPosition(double x, double y) {
        // Update position
        this.imageView.setLayoutX(this.x);
        this.imageView.setLayoutY(this.y);
    }

    // The update method is a loop that keeps track of the enemy's state.
    public abstract void update(Player player);
    // The receiveDamage method allows the player to hurt the enemy
    public void receiveDamage(int damage, Player player) {
        health -= damage;
        if (health <= 0) {
            markForRemoval();
        } else {
            applyKnockback(player);
        }
    }
    // This method knocks the enemy back away from the player by 100 pixels.
    void applyKnockback(Player player) {
        double knockbackDistance = 100;
        double deltaX = this.x - player.getX();
        double deltaY = this.y - player.getY();
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        if (distance != 0) { // Check to avoid division by zero
            double dirX = deltaX / distance;
            double dirY = deltaY / distance;

            double newX = this.x + dirX * knockbackDistance;
            double newY = this.y + dirY * knockbackDistance;
        }
    }
    // This method is used to remove enemy objects from the game. This is done
    // when they have <= 0 health, the next age has begun, or the game
    // is reset.
    public void markForRemoval() {
        shouldBeRemoved = true;
    }
    // Used for collisions
    public Rectangle2D getBoundingBox() {
        return boundingBox;
    }
    // Used for visual representation
    public Node getImageView() {
        return imageView;
    }
    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public void setY(double enemyY) {
        y= enemyY;
    }
    public void setX(double enemyX) {
        x = enemyX;
    }

    public Rectangle getDebugBoundingBox() {
        return debugBoundingBox;
    }
    public boolean shouldBeRemoved() {
        return shouldBeRemoved;
    }

}