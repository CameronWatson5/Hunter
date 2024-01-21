package com.example.hunter;

import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class Projectile {
    private ImageView imageView;
    private Rectangle2D boundingBox;
    private Timeline animationTimeline;
    private int currentFrameIndex = 0;
    private static final int FRAME_WIDTH = 40; // Adjust according to your sprite sheet
    private static final int FRAME_HEIGHT = 40;
    private static final int TOTAL_FRAMES = 4; // Total number of frames in the sprite sheet
    private int damage = 5;
    private double x, y;
    private double speed = 5;
    private double directionX, directionY;

    public Projectile(double x, double y, double directionX, double directionY, double speed) {
        this.x = x;
        this.y = y;
        this.directionX = directionX;
        this.directionY = directionY;
        this.speed = speed;

        // Load projectile image
        Image image = new Image(getClass().getResourceAsStream("/images/rockSpriteSheet.png"));
        this.imageView = new ImageView(image);
        int firstFrameX = 0; // X coordinate of the first frame
        int firstFrameY = 0; // Y coordinate of the first frame
        imageView.setViewport(new Rectangle2D(firstFrameX, firstFrameY, FRAME_WIDTH, FRAME_HEIGHT));
        imageView.setLayoutX(x);
        imageView.setLayoutY(y);
        setupAnimation();
        // Initialize bounding box
        boundingBox = new Rectangle2D(x, y, FRAME_WIDTH, FRAME_HEIGHT);
    }
    private void setupAnimation() {
        animationTimeline = new Timeline(new KeyFrame(Duration.millis(100), e -> updateAnimationFrame()));
        animationTimeline.setCycleCount(Timeline.INDEFINITE);
        animationTimeline.play();
    }
    private void updateAnimationFrame() {
        currentFrameIndex = (currentFrameIndex + 1) % TOTAL_FRAMES;
        int column = currentFrameIndex % 2; // Assuming 2 columns in the sprite sheet
        int row = currentFrameIndex / 2; // Assuming 2 rows in the sprite sheet
        imageView.setViewport(new Rectangle2D(column * FRAME_WIDTH, row * FRAME_HEIGHT, FRAME_WIDTH, FRAME_HEIGHT));
    }

    public void update() {
        x += directionX * speed;
        y += directionY * speed;
        //System.out.println("Updated Position - X: " + x + ", Y: " + y); // Debugging line
        imageView.setLayoutX(x);
        imageView.setLayoutY(y);

    // Update bounding box
        boundingBox = new Rectangle2D(x, y, FRAME_WIDTH, FRAME_HEIGHT);
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


    // Add other methods as needed, e.g., for collision detection
}
