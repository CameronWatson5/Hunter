/*
This is the Knight class, it is a subclass of the Enemy abstract superclass.
The Knight appears in the Medieval Age.
*/

package com.example.hunter.enemies;


import com.example.hunter.Player;
import com.example.hunter.enemies.Enemy;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Knight extends Enemy {
    private static final int FRAME_WIDTH = 204; // pixel width of PNG photo
    private static final int FRAME_HEIGHT = 102; // pixel height of PNG photo
    private static final int TOTAL_FRAMES = 2; // animation frame in sprite sheet
    private int frameCounter = 0; // animation begins at 0
    private int frameDelay = 10; // speed of animation
    private int currentFrameIndex = 0; // current animation
    private boolean isKnockedBack = false; // this is used to determine if an enemy is knocked back.
    private boolean movingRight = true; // Initial movement direction

    private Image spriteSheetRight;
    private Image spriteSheetLeft;
    private Pane gamePane;

    public Knight(double x, double y, int speed, int initialHealth, double width, double height, Pane gamePane) {
        super(x, y, speed, initialHealth);
        this.gamePane = gamePane;
        loadSpriteSheets();
        setupSprite();
    }

    private void loadSpriteSheets() {
        spriteSheetRight = new Image(getClass().getResourceAsStream("/images/KnightRight.png"));
        spriteSheetLeft = new Image(getClass().getResourceAsStream("/images/KnightLeft.png"));
    }

    private void setupSprite() {
        // Set initial sprite sheet
        this.imageView = new ImageView(movingRight ? spriteSheetRight : spriteSheetLeft);
        this.imageView.setViewport(new Rectangle2D(0, 0, FRAME_WIDTH, FRAME_HEIGHT));

        this.debugBoundingBox = new Rectangle(x, y, FRAME_WIDTH, FRAME_HEIGHT);
        this.debugBoundingBox.setStroke(Color.TRANSPARENT);
        this.debugBoundingBox.setFill(Color.TRANSPARENT);

        this.imageView.setLayoutX(this.x);
        this.imageView.setLayoutY(this.y);
    }

    @Override
    public void update(Player player) {
        if (isKnockedBack) {
            applyKnockback(player);
        } else if (gamePane != null) {
            double gamePaneWidth = gamePane.getWidth();

            // Update the knight's position based on the current direction
            if (movingRight) {
                this.x += speed;
                // Check if the knight has reached the right edge
                if (this.x > gamePaneWidth - FRAME_WIDTH) {
                    this.x = gamePaneWidth - FRAME_WIDTH; // Adjust position to the edge
                    movingRight = false; // Change direction to left
                }
            } else {
                this.x -= speed;
                // Check if the knight has reached the left edge
                if (this.x < 0) {
                    this.x = 0; // Adjust position to the edge
                    movingRight = true; // Change direction to right
                }
            }
        }
        // Update sprite sheet based on direction
        this.imageView.setImage(movingRight ? spriteSheetRight : spriteSheetLeft);
        this.imageView.setLayoutX(this.x);
        this.imageView.setLayoutY(this.y);

        this.boundingBox = new Rectangle2D(x, y, imageView.getImage().getWidth(), imageView.getImage().getHeight());
        updateAnimationFrame();
    }

    private void updateAnimationFrame() {
        frameCounter++;
        if (frameCounter >= frameDelay) {
            currentFrameIndex = (currentFrameIndex + 1) % TOTAL_FRAMES;
            imageView.setViewport(new Rectangle2D(currentFrameIndex * FRAME_WIDTH, 0, FRAME_WIDTH, FRAME_HEIGHT));
            frameCounter = 0; // Reset the counter
        }
    }
    @Override
    public Rectangle getDebugBoundingBox() {
        return debugBoundingBox;
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
}
