package com.example.hunter.enemies;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.Objects;

public class Robot extends Ned{
    private static final int FRAME_WIDTH = 102; // pixel width of PNG photo
    private static final int FRAME_HEIGHT = 102; // pixel height of PNG photo
    private static final int TOTAL_FRAMES = 2; // animation frame in sprite sheet
    private int frameCounter = 0; // animation begins at 0
    private int frameDelay = 10; // speed of animation
    private int currentFrameIndex = 0; // current animation
    private boolean isKnockedBack = false; // this is used to determine if an enemy is knocked back.

    public Robot(double x, double y, double speed, int initialHealth){
    super(x, y, speed, initialHealth);

    // laod the sprite sheet
    Image spriteSheet = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/Robot.png")));

        this.imageView = new ImageView(spriteSheet);
        this.imageView.setViewport(new Rectangle2D(0, 0, FRAME_WIDTH, FRAME_HEIGHT));

    // create the debug box
        this.debugBoundingBox = new Rectangle(x, y, FRAME_WIDTH, FRAME_HEIGHT);
        this.debugBoundingBox.setStroke(Color.TRANSPARENT);
        this.debugBoundingBox.setFill(Color.TRANSPARENT);

    // match the sprite sheet with the object's position
        this.imageView.setLayoutX(this.x);
        this.imageView.setLayoutY(this.y);
    }
}
