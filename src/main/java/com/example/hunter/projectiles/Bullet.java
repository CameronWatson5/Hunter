/*
This is the Bullet class, it is a subclass of the Projectile superclass.
The Arrow is used by the Soldier class.
*/


package com.example.hunter.projectiles;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.util.Objects;

public class Bullet extends Projectile {
    private static final int FRAME_WIDTH = 10; // pixel width
    private static final int FRAME_HEIGHT = 10; // pixel height
    private static final int TOTAL_FRAMES = 2; // animation frames
    private static final int DAMAGE = 1;
    private static final double SPEED = 3;

    public Bullet(double x, double y, double directionX, double directionY, double speed, int damage, boolean firedByPlayer) {
        super(x, y, directionX, directionY, SPEED, DAMAGE, firedByPlayer);

        setupProjectile();
    }
    // sets up the visual representation of the projectile and creates a bounding box
    // which is used for collisions.
    @Override
    protected void setupProjectile() {
        this.frameWidth = FRAME_WIDTH;
        this.frameHeight = FRAME_HEIGHT;
        this.damage = DAMAGE;

        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/bullet.png")));
        this.imageView = new ImageView(image);
        imageView.setViewport(new Rectangle2D(0, 0, FRAME_WIDTH, FRAME_HEIGHT));
        imageView.setLayoutX(x);
        imageView.setLayoutY(y);
        boundingBox = new Rectangle2D(x, y, FRAME_WIDTH, FRAME_HEIGHT);

        setupAnimation();
    }
    // The animation always plays.
    private void setupAnimation() {
        Timeline animationTimeline = new Timeline(new KeyFrame(Duration.millis(100), e -> updateAnimationFrame()));
        animationTimeline.setCycleCount(Timeline.INDEFINITE);
        animationTimeline.play();
    }

    private void updateAnimationFrame() {
        currentFrameIndex = (currentFrameIndex + 1) % TOTAL_FRAMES;
        int column = currentFrameIndex % 2;
        int row = currentFrameIndex / 2;
        imageView.setViewport(new Rectangle2D(column * FRAME_WIDTH, row * FRAME_HEIGHT, FRAME_WIDTH, FRAME_HEIGHT));
    }
}