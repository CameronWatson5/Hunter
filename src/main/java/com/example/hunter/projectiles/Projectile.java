package com.example.hunter.projectiles;

import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.image.ImageView;

public abstract class Projectile {
    protected ImageView imageView;
    protected Rectangle2D boundingBox;
    protected int currentFrameIndex = 0;
    protected int frameWidth;
    protected int frameHeight;
    protected int damage = 4;
    protected double x, y;
    protected double speed;
    protected double directionX, directionY;
    private boolean firedByPlayer;

    public Projectile(double x, double y, double directionX, double directionY, double speed, int damage, boolean firedByPlayer) {
        this.x = x;
        this.y = y;
        this.directionX = directionX;
        this.directionY = directionY;
        this.speed = speed;
        this.firedByPlayer = firedByPlayer;

        setupProjectile();
    }
    public boolean isFiredByPlayer() {
        return firedByPlayer;
    }


    protected abstract void setupProjectile();

    public void update() {
        x += directionX * speed;
        y += directionY * speed;
        imageView.setLayoutX(x);
        imageView.setLayoutY(y);

        // Update bounding box
        boundingBox = new Rectangle2D(x, y, frameWidth, frameHeight);
    }

    public Rectangle2D getBoundingBox() {
        return boundingBox;
    }
    public Node getView() {
        return imageView;
    }
    public int getDamage() {
        return damage;
    }
    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
}
