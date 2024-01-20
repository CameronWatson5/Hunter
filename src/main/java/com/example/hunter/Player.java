package com.example.hunter;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.Objects;

public class Player {
    private ImageView characterView;
    private Image image;
    private Timeline timeline;
    private Image spriteSheetUp;
    private Image spriteSheetDown;
    private Image spriteSheetLeft;
    private Image spriteSheetRight;



    public Player(ImageView characterView, String imagePath) {
        this.characterView = Objects.requireNonNull(characterView, "characterView cannot be null");

        // Load the main image
        InputStream stream = getClass().getResourceAsStream(imagePath);
        if (stream == null) {
            throw new IllegalArgumentException("Resource not found: " + imagePath);
        }
        this.image = new Image(stream);
        characterView.setImage(image);
        loadSpriteSheets();
        // Load and setup the standing sprite sheet
        setupStandingAnimation();
    }
    private void loadSpriteSheets(){
        // In the constructor or a dedicated method
        spriteSheetUp = new Image(getClass().getResourceAsStream("/images/upWalkSpriteSheet.png"));
        spriteSheetDown = new Image(getClass().getResourceAsStream("/images/downWalkspriteSheet.png"));
        spriteSheetLeft = new Image(getClass().getResourceAsStream("/images/leftWalkspriteSheet.png"));
        spriteSheetRight = new Image(getClass().getResourceAsStream("/images/rightWalkSpriteSheet.png"));

    }
    private void setupStandingAnimation() {
        InputStream stream = getClass().getResourceAsStream("/images/standingSpriteSheet.png");
        if (stream == null) {
            throw new IllegalArgumentException("Resource not found: /images/standingSpriteSheet.png");
        }
        Image standingSpriteSheet = new Image(stream);

        characterView.setImage(standingSpriteSheet);

        // Set an initial viewport
        final int spriteWidth = 102;
        final int spriteHeight = 102;
        characterView.setViewport(new Rectangle2D(0, 0, spriteWidth, spriteHeight));

        final int numberOfFrames = 4;
        final Duration frameTime = Duration.millis(200); // Time per frame

        timeline = new Timeline(new KeyFrame(frameTime, e -> {
            int currentIndex = (int) (characterView.getViewport().getMinX() / spriteWidth) + 1;
            if (currentIndex >= numberOfFrames) currentIndex = 0;
            int column = currentIndex % 2; // Assuming 2 columns
            int row = currentIndex / 2; // Assuming 2 rows
            characterView.setViewport(new Rectangle2D(column * spriteWidth, row * spriteHeight, spriteWidth, spriteHeight));
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }



    public void move(int dx, int dy) {
        if (dx > 0) {
            walkRight();
        } else if (dx < 0) {
            walkLeft();
        } else if (dy > 0) {
            walkDown();
        } else if (dy < 0) {
            walkUp();
        }

        characterView.setX(characterView.getX() + dx);
        characterView.setY(characterView.getY() + dy);
    }

    private void walkUp() {
        characterView.setImage(spriteSheetUp);

        // Set an initial viewport for the right movement animation
        final int spriteWidth = 102; // Adjust based on your sprite sheet
        final int spriteHeight = 102; // Adjust based on your sprite sheet
        characterView.setViewport(new Rectangle2D(0, 0, spriteWidth, spriteHeight));

        final int numberOfFrames = 2; // Adjust based on your sprite sheet
        final Duration frameTime = Duration.millis(200); // Adjust timing as needed

        // If a timeline is already running, stop it before starting a new one
        if (timeline != null) {
            timeline.stop();
        }

        timeline = new Timeline(new KeyFrame(frameTime, e -> {
            int currentIndex = (int) (characterView.getViewport().getMinX() / spriteWidth) + 1;
            if (currentIndex >= numberOfFrames) currentIndex = 0;
            int column = currentIndex % 2; // Assuming 2 columns, adjust as necessary
            int row = currentIndex / 2; // Assuming 2 rows, adjust as necessary
            characterView.setViewport(new Rectangle2D(column * spriteWidth, row * spriteHeight, spriteWidth, spriteHeight));
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

    }

    private void walkDown() {
        characterView.setImage(spriteSheetDown);

        // Set an initial viewport for the right movement animation
        final int spriteWidth = 102; // Adjust based on your sprite sheet
        final int spriteHeight = 102; // Adjust based on your sprite sheet
        characterView.setViewport(new Rectangle2D(0, 0, spriteWidth, spriteHeight));

        final int numberOfFrames = 2; // Adjust based on your sprite sheet
        final Duration frameTime = Duration.millis(200); // Adjust timing as needed

        // If a timeline is already running, stop it before starting a new one
        if (timeline != null) {
            timeline.stop();
        }

        timeline = new Timeline(new KeyFrame(frameTime, e -> {
            int currentIndex = (int) (characterView.getViewport().getMinX() / spriteWidth) + 1;
            if (currentIndex >= numberOfFrames) currentIndex = 0;
            int column = currentIndex % 2; // Assuming 2 columns, adjust as necessary
            int row = currentIndex / 2; // Assuming 2 rows, adjust as necessary
            characterView.setViewport(new Rectangle2D(column * spriteWidth, row * spriteHeight, spriteWidth, spriteHeight));
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }


    private void walkLeft() {
        characterView.setImage(spriteSheetLeft);

        // Set an initial viewport for the right movement animation
        final int spriteWidth = 102; // Adjust based on your sprite sheet
        final int spriteHeight = 102; // Adjust based on your sprite sheet
        characterView.setViewport(new Rectangle2D(0, 0, spriteWidth, spriteHeight));

        final int numberOfFrames = 2; // Adjust based on your sprite sheet
        final Duration frameTime = Duration.millis(200); // Adjust timing as needed

        // If a timeline is already running, stop it before starting a new one
        if (timeline != null) {
            timeline.stop();
        }

        timeline = new Timeline(new KeyFrame(frameTime, e -> {
            int currentIndex = (int) (characterView.getViewport().getMinX() / spriteWidth) + 1;
            if (currentIndex >= numberOfFrames) currentIndex = 0;
            int column = currentIndex % 2; // Assuming 2 columns, adjust as necessary
            int row = currentIndex / 2; // Assuming 2 rows, adjust as necessary
            characterView.setViewport(new Rectangle2D(column * spriteWidth, row * spriteHeight, spriteWidth, spriteHeight));
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void walkRight() {
        characterView.setImage(spriteSheetRight);

        // Set an initial viewport for the right movement animation
        final int spriteWidth = 102; // Adjust based on your sprite sheet
        final int spriteHeight = 102; // Adjust based on your sprite sheet
        characterView.setViewport(new Rectangle2D(0, 0, spriteWidth, spriteHeight));

        final int numberOfFrames = 2; // Adjust based on your sprite sheet
        final Duration frameTime = Duration.millis(200); // Adjust timing as needed

        // If a timeline is already running, stop it before starting a new one
        if (timeline != null) {
            timeline.stop();
        }

        timeline = new Timeline(new KeyFrame(frameTime, e -> {
            int currentIndex = (int) (characterView.getViewport().getMinX() / spriteWidth) + 1;
            if (currentIndex >= numberOfFrames) currentIndex = 0;
            int column = currentIndex % 2; // Assuming 2 columns, adjust as necessary
            int row = currentIndex / 2; // Assuming 2 rows, adjust as necessary
            characterView.setViewport(new Rectangle2D(column * spriteWidth, row * spriteHeight, spriteWidth, spriteHeight));
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    public void standStill() {
        characterView.setImage(new Image(getClass().getResourceAsStream("/images/standingSpriteSheet.png")));
        setupStandingAnimation();
    }

    // Other player methods...
}

