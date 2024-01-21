package com.example.hunter;

import javafx.animation.PauseTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.*;

public class Player {
    private boolean invincible = false;
    private long invincibilityDuration = 1000; // Invincibility duration in milliseconds
    private static final int PLAYER_WIDTH = 102; // Example width
    private static final int PLAYER_HEIGHT = 102; // Example height

    private GameController gameController;
    private ImageView characterView;
    private Image image;
    private Timeline timeline;
    private Image spriteSheetUp;
    private Image spriteSheetDown;
    private Image spriteSheetLeft;
    private Image spriteSheetRight;
    private Direction currentDirection = Direction.STANDING;
    private boolean isAttacking = false;
    private Image attackSpriteSheetUp;
    private Image attackSpriteSheetDown;
    private Image attackSpriteSheetLeft;
    private Image attackSpriteSheetRight;

    private Rectangle2D attackArea;

    private int attackDamage = 5;
    private double health = 100;
    private double maxHealth = 100;

    public void setAttackDamage(int damage) {
        this.attackDamage = damage;
    }

    public int getAttackDamage() {
        return this.attackDamage;
    }
    public void receiveDamage(int damage) {
        if (!invincible) {
            health -= damage;
            if (health < 0) {
                health = 0;
            }
            invincible = true;

            PauseTransition pause = new PauseTransition(Duration.millis(invincibilityDuration));
            pause.setOnFinished(event -> invincible = false);
            pause.play();
            Platform.runLater(() -> gameController.updateHealthBar());
        }
    }

    public Player(ImageView characterView, String imagePath, GameController gameController) {
        this.gameController = gameController;
        this.characterView = Objects.requireNonNull(characterView, "characterView cannot be null");

        // Load the main image
        InputStream stream = getClass().getResourceAsStream(imagePath);
        if (stream == null) {
            throw new IllegalArgumentException("Resource not found: " + imagePath);
        }
        this.image = new Image(stream);
        characterView.setImage(image);
        loadSpriteSheets();
        loadAttackSpriteSheets();
        // Load and setup the standing sprite sheet
        setupStandingAnimation();
    }
    private void loadSpriteSheets(){
        // In the constructor or a dedicated method
        spriteSheetUp = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/upWalkSpriteSheet.png")));
        spriteSheetDown = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/downWalkspriteSheet.png")));
        spriteSheetLeft = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/leftWalkspriteSheet.png")));
        spriteSheetRight = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/rightWalkSpriteSheet.png")));
    }
    private void loadAttackSpriteSheets() {
        attackSpriteSheetUp = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/attackUpSpriteSheet.png")));
        attackSpriteSheetDown = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/attackDownSpriteSheet.png")));
        attackSpriteSheetLeft = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/attackLeftSpriteSheet.png")));
        attackSpriteSheetRight = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/attackRightSpriteSheet.png")));
    }
    public void fireProjectile() {
        double directionX = 0;
        double directionY = 0;
        double speed = 5; // Example speed

        // Calculate the direction of the projectile based on player's current direction
        switch (currentDirection) {
            case STANDING, DOWN:
                directionY = 1;
                break;
            case UP:
                directionY = -1;
                break;
            case LEFT:
                directionX = -1;
                break;
            case RIGHT:
                directionX = 1;
                break;
        }

        // Create a new projectile through GameController
        gameController.createProjectile(getX(), getY(), directionX, directionY, speed);
    }


    public void attack() {
        if (!isAttacking) {
            isAttacking = true;

            // Store the current direction before attacking
            Direction directionBeforeAttack = currentDirection;
            double attackWidth = 100; // Width of the attack area
            double attackHeight = 100; // Height of the attack area
            double offsetX = 0; // X offset from the player's position
            double offsetY = 0; // Y offset from the player's position

            // Adjust offsetX and offsetY based on the player's direction and position
            switch (currentDirection) {
                case STANDING:
                    offsetY = characterView.getBoundsInParent().getHeight();
                    break;
                case RIGHT:
                    offsetX = characterView.getBoundsInParent().getWidth();
                    break;
                case LEFT:
                    offsetX = -attackWidth;
                    break;
                case UP:
                    offsetY = -attackHeight;
                    break;
                case DOWN:
                    offsetY = characterView.getBoundsInParent().getHeight();
                    break;
            }

            // Initialize the attack area
            attackArea = new Rectangle2D(getX() + offsetX, getY() + offsetY, attackWidth, attackHeight);

            // Setup the attack animation based on the current direction
            switch (currentDirection) {
                case RIGHT:
                    setupAnimation(attackSpriteSheetRight, 2, Duration.millis(100), false);
                    break;
                case LEFT:
                    setupAnimation(attackSpriteSheetLeft, 2, Duration.millis(100), false);
                    break;
                case UP:
                    setupAnimation(attackSpriteSheetUp, 2, Duration.millis(100), false);
                    break;
                case DOWN:
                    setupAnimation(attackSpriteSheetDown, 2, Duration.millis(100), false);
                    break;
            }
            ArrayList<Enemy> enemies = gameController.getEnemies();
            for (Enemy enemy : gameController.getEnemies()) {
                if (attackArea.intersects(enemy.getBoundingBox())) {
                    enemy.receiveDamage(attackDamage);
                    System.out.println("Enemy hit!");
                }
            }
            // Timer to end the attack and return to the previous walking animation
            new Timeline(new KeyFrame(Duration.millis(300), e -> {
                // Stop the attack animation
                timeline.stop();

                // Reset the attacking state
                isAttacking = false;

                // Transition back to the appropriate animation based on direction and movement
                if (directionBeforeAttack != Direction.STANDING) {
                    switch (directionBeforeAttack) {
                        case RIGHT:
                            walkRightAfterAttack();
                            break;
                        case LEFT:
                            walkLeftAfterAttack();
                            break;
                        case UP:
                            walkUpAfterAttack();
                            break;
                        case DOWN:
                            walkDownAfterAttack();
                            break;
                    }
                } else {
                    standStill();
                }
            })).play();
        }
    }



    private void transitionToNextAnimation() {
        System.out.println("Transitioning animation. Current direction: " + currentDirection + ", Attacking: " + isAttacking);
        if (isAttacking) {
            isAttacking = false; // Reset the attacking state
            switch (currentDirection) {
                case RIGHT:
                    walkRight();
                    break;
                case LEFT:
                    walkLeft();
                    break;
                case UP:
                    walkUp();
                    break;
                case DOWN:
                    walkDown();
                    break;
                default:
                    standStill();
                    break;
            }
        }
    }
    private void setupStandingAnimation() {
        InputStream stream = getClass().getResourceAsStream("/images/downWalkSpriteSheet.png");
        if (stream == null) {
            throw new IllegalArgumentException("Resource not found: /images/downWalkSpriteSheet.png");
        }
        Image standingSpriteSheet = new Image(stream);

        final int numberOfFrames = 2;
        final Duration frameTime = Duration.millis(200); // Time per frame

        setupAnimation(standingSpriteSheet, numberOfFrames, frameTime, true);
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
        if (currentDirection != Direction.UP) {
            currentDirection = Direction.UP;
            setupAnimation(spriteSheetUp, 2, Duration.millis(200), true);
        }
    }
    private void walkDown() {
        if (currentDirection != Direction.DOWN) {
            currentDirection = Direction.DOWN;
            setupAnimation(spriteSheetDown, 2, Duration.millis(200), true);
        }
    }
    private void walkLeft() {
        if (currentDirection != Direction.LEFT) {
            currentDirection = Direction.LEFT;
            setupAnimation(spriteSheetLeft, 2, Duration.millis(200), true);
        }
    }
    private void walkRight() {
        if (currentDirection != Direction.RIGHT) {
            currentDirection = Direction.RIGHT;
            setupAnimation(spriteSheetRight, 2, Duration.millis(200), true);
        }
    }
    private void walkUpAfterAttack() {
        currentDirection = Direction.UP;
        setupAnimation(spriteSheetUp, 2, Duration.millis(200), true);
    }

    private void walkDownAfterAttack() {
        currentDirection = Direction.DOWN;
        setupAnimation(spriteSheetDown, 2, Duration.millis(200), true);
    }

    private void walkLeftAfterAttack() {
        currentDirection = Direction.LEFT;
        setupAnimation(spriteSheetLeft, 2, Duration.millis(200), true);
    }

    private void walkRightAfterAttack() {
        currentDirection = Direction.RIGHT;
        setupAnimation(spriteSheetRight, 2, Duration.millis(200), true);
    }

    private void setupAnimation(Image spriteSheet, int numberOfFrames, Duration frameTime, boolean loopAnimation) {
        characterView.setImage(spriteSheet);
        characterView.setViewport(new Rectangle2D(0, 0, 102, 102));

        // If a timeline is already running, stop it before starting a new one
        if (timeline != null) {
            timeline.stop();
        }

        timeline = new Timeline(new KeyFrame(frameTime, e -> {
            int currentIndex = (int) (characterView.getViewport().getMinX() / 102) + 1;
            if (currentIndex >= numberOfFrames) currentIndex = 0;
            int column = currentIndex % 2; // Assuming 2 columns, adjust as necessary
            int row = currentIndex / 2; // Assuming 2 rows, adjust as necessary
            characterView.setViewport(new Rectangle2D(column * 102, row * 102, 102, 102));
        }));

        if (loopAnimation) {
            timeline.setCycleCount(Timeline.INDEFINITE);
        } else {
            timeline.setCycleCount(numberOfFrames);
        }
        timeline.play();
    }
    public Rectangle2D getBoundingBox() {
        // Define a reduction factor if the bounding box should be smaller than the sprite
        double reductionFactor = 0.5;
        double reducedWidth = PLAYER_WIDTH * reductionFactor;
        double reducedHeight = PLAYER_HEIGHT * reductionFactor;

        // Calculate offsets to center the smaller bounding box on the sprite
        double offsetX = (PLAYER_WIDTH - reducedWidth) / 2;
        double offsetY = (PLAYER_HEIGHT - reducedHeight) / 2;

        return new Rectangle2D(getX() + offsetX, getY() + offsetY, reducedWidth, reducedHeight);
    }
    public double getX() {
        return characterView.getX();
    }

    public double getY() {
        return characterView.getY();
    }

    public double getHealth() {
        return health;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    private enum Direction {
            UP, DOWN, LEFT, RIGHT, STANDING
        }
    public void standStill() {
        if (currentDirection != Direction.STANDING) {
            currentDirection = Direction.STANDING;
            characterView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/standingSpriteSheet.png"))));
            setupStandingAnimation();
        }
    }
        // ... rest of your code ...
    }
