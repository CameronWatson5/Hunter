/*
This is the player class. Player movement is handled here, as well as
animations, and attacking.
 */

package com.example.hunter;

import com.example.hunter.enemies.Enemy;
import javafx.animation.PauseTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;


import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.Random;

public class Player {
    private boolean invincible = false; // after player is hit, they are invincible for a short time
    private final Rectangle debugBoundingBox;
    boolean debugMode = false; // This is used to see the hit boxes of objects
    private static final int PLAYER_WIDTH = 102; // pixel width
    private static final int PLAYER_HEIGHT = 102; // pixel height

    private final GameController gameController;
    private final ImageView characterView;
    private Timeline timeline;
    private Image spriteSheetUp; // sprite sheet when moving up
    private Image spriteSheetDown; // sprite sheet when moving down
    private Image spriteSheetLeft; // sprite sheet when moving left
    private Image spriteSheetRight; // sprite sheet when moving right
    private Image weaponSpriteSheet; // sprite sheet for sword
    private Direction currentDirection = Direction.DOWN; // Keeps track of the current direction
    private boolean isAttacking = false; // Used to determine when a player is attacking
    private Image attackSpriteSheetUp; // sprite sheet used when attacking up
    private Image attackSpriteSheetDown; // sprite sheet used when attacking down
    private Image attackSpriteSheetLeft; // sprite sheet used when attacking left
    private Image attackSpriteSheetRight; // sprite sheet used when attacking right

    private double health = 100; // when 0, player dies
    private long lastProjectileTime = 0; // Time when the last projectile was fired

    public Player(ImageView characterView, String imagePath, GameController gameController) {
        this.gameController = gameController;
        this.characterView = Objects.requireNonNull(characterView, "characterView cannot be null");

        // Load the main image
        InputStream stream = getClass().getResourceAsStream("/" + imagePath);
        if (stream == null) {
            throw new IllegalArgumentException("Resource not found: " + imagePath);
        }
        Image image = new Image(stream);
        characterView.setImage(image);
        loadSpriteSheets();
        loadAttackSpriteSheets();
        debugBoundingBox = new Rectangle();
        debugBoundingBox.setStroke(Color.BLUE);
        debugBoundingBox.setFill(Color.TRANSPARENT);
        updateDebugBoundingBox();
        // Load and setup the standing sprite sheet
        setupStandingAnimation();
    }

    // This method checks if the player is invincible. The player is invincible for 1 second after
    // being attacked. This is to stop the player being instantly killed if attacked from multiple directions at once.
    // The UI is also updated with the new health.
    public void receiveDamage(int damage) {
        if (!invincible) {
            health -= damage;
            if (health < 0) {
                health = 0;
            }
            invincible = true;

            // Invincibility duration
            long invincibilityDuration = 1000;
            PauseTransition pause = new PauseTransition(Duration.millis(invincibilityDuration));
            pause.setOnFinished(event -> invincible = false);
            pause.play();
            Platform.runLater(gameController::updateHealthBar);
        }
    }

    // Used when debugging
    private void updateDebugBoundingBox() {
        if (debugMode) {
            Rectangle2D boundingBox = getBoundingBox();
            debugBoundingBox.setX(boundingBox.getMinX());
            debugBoundingBox.setY(boundingBox.getMinY());
            debugBoundingBox.setWidth(boundingBox.getWidth());
            debugBoundingBox.setHeight(boundingBox.getHeight());
        }
    }

    // loads the sprite sheets for walking up, down, left, and right
    private void loadSpriteSheets() {
        spriteSheetUp = loadImage("images/upWalkSpriteSheet.png");
        spriteSheetDown = loadImage("images/downWalkSpriteSheet.png");
        spriteSheetLeft = loadImage("images/leftWalkSpriteSheet.png");
        spriteSheetRight = loadImage("images/rightWalkSpriteSheet.png");
    }


    private Image loadImage(String path) {
        InputStream stream = getClass().getResourceAsStream("/" + path);
        if (stream == null) {
            System.err.println("Resource not found: " + path);
            return null;
        }
        return new Image(stream);
    }


    // loads the sprite sheets for attacking up, down, left, and right
    private void loadAttackSpriteSheets() {
        weaponSpriteSheet = loadImage("images/swordSpriteSheet.png");
        attackSpriteSheetUp = loadImage("images/attackUpSpriteSheet.png");
        attackSpriteSheetDown = loadImage("images/attackDownSpriteSheet.png");
        attackSpriteSheetLeft = loadImage("images/attackLeftSpriteSheet.png");
        attackSpriteSheetRight = loadImage("images/attackRightSpriteSheet.png");
    }


    // fires a rock projectile, has a 0.5 second cooldown.
    public void fireProjectile() {
        long currentTime = System.currentTimeMillis(); // Get the current time
        // Cooldown time in milliseconds
        long projectileCooldown = 500;
        if (currentTime - lastProjectileTime >= projectileCooldown) {

            playRockSound();
            lastProjectileTime = currentTime; // Update the last projectile time

            double directionX = 0;
            double directionY = 0;
            double speed = 5;

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
            // Create a new projectile
            int damage = 4;
            gameController.createRockProjectile(getX(), getY(), directionX, directionY, speed, damage, true);
        }
    }

    // This method is used for melee attack
    public void attack() {
        if (!isAttacking) {
            isAttacking = true;
            playSwooshSound();
            // Store the current direction before attacking
            Direction directionBeforeAttack = currentDirection;
            double attackWidth = 102; // Width of the attack area
            double attackHeight = 102; // Height of the attack area
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
            // used during attacks
            Rectangle2D attackArea = new Rectangle2D(getX() + offsetX, getY() + offsetY, attackWidth, attackHeight);

            ImageView attackAnimationView = new ImageView(weaponSpriteSheet);
            final int frameCount = 2;
            final int frameWidth = 102;
            final int frameHeight = 102;

            Timeline animationTimeline = new Timeline();
            for (int i = 0; i < frameCount; i++) {
                final int frameIndex = i;
                KeyFrame frame = new KeyFrame(Duration.millis(i * 100), e -> {
                    // Set the viewport to display the correct frame
                    attackAnimationView.setViewport(new Rectangle2D(frameIndex * frameWidth, 0, frameWidth, frameHeight));
                });
                animationTimeline.getKeyFrames().add(frame);
            }

            animationTimeline.setOnFinished(e -> gameController.getGamePane().getChildren().remove(attackAnimationView));
            attackAnimationView.setLayoutX(getX() + offsetX);
            attackAnimationView.setLayoutY(getY() + offsetY);
            Random rand = new Random();
            double RightWeaponAdjustmentX = -30;
            double LeftWeaponAdjustmentX = 30;
            switch (currentDirection) {
                case RIGHT:
                    offsetX = characterView.getBoundsInParent().getWidth() - RightWeaponAdjustmentX;

                    int angleRight = 80 + rand.nextInt(41);
                    attackAnimationView.setScaleX(1);
                    attackAnimationView.setRotate(angleRight);
                    break;
                case LEFT:
                    offsetX = characterView.getBoundsInParent().getWidth() - LeftWeaponAdjustmentX;
                    int angleLeft = 260 + rand.nextInt(41);
                    attackAnimationView.setScaleX(-1);
                    attackAnimationView.setRotate(angleLeft);
                    break;
                case UP:
                    int angleUp = rand.nextInt(41);
                    attackAnimationView.setScaleX(1);
                    attackAnimationView.setRotate(angleUp);
                    break;
                case DOWN:
                    int angleDown = rand.nextInt(41);
                    attackAnimationView.setScaleY(-1);
                    attackAnimationView.setRotate(angleDown);
                    break;
            }

            gameController.getGamePane().getChildren().add(attackAnimationView);
            animationTimeline.play();

            if (debugMode) {
                Rectangle hitboxVisual = new Rectangle(attackArea.getMinX(), attackArea.getMinY(), attackArea.getWidth(), attackArea.getHeight());
                hitboxVisual.setStroke(Color.RED);
                hitboxVisual.setFill(new Color(1, 0, 0, 0.3));

                gameController.getGamePane().getChildren().add(hitboxVisual);

                new Timeline(new KeyFrame(Duration.millis(50), e -> gameController.getGamePane().getChildren().remove(hitboxVisual))).play();
            }
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
                    // damage
                    int attackDamage = 4;
                    enemy.receiveDamage(attackDamage, this);
                    //System.out.println("Enemy hit!");
                }
            }
            new Timeline(new KeyFrame(Duration.millis(300), e -> {
                gameController.getGamePane().getChildren().remove(attackAnimationView);

                timeline.stop();

                isAttacking = false;

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

    private void setupStandingAnimation() {
        InputStream stream = getClass().getResourceAsStream("/images/downWalkSpriteSheet.png");
        if (stream == null) {
            throw new IllegalArgumentException("Resource not found: images/downWalkSpriteSheet.png");
        }
        Image standingSpriteSheet = new Image(stream);
        final int numberOfFrames = 2;
        final Duration frameTime = Duration.millis(200);

        setupAnimation(standingSpriteSheet, numberOfFrames, frameTime, true);
    }


    // Used for player movement
    public void move(double dx, double dy) {
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
        updateDebugBoundingBox();
    }

    // Sets up animation
    private void walkUp() {
        if (currentDirection != Direction.UP) {
            currentDirection = Direction.UP;
            setupAnimation(spriteSheetUp, 2, Duration.millis(200), true);
        }
    }

    // Sets up animation
    private void walkDown() {
        if (currentDirection != Direction.DOWN) {
            currentDirection = Direction.DOWN;
            setupAnimation(spriteSheetDown, 2, Duration.millis(200), true);
        }
    }

    // Sets up animation
    private void walkLeft() {
        if (currentDirection != Direction.LEFT) {
            currentDirection = Direction.LEFT;
            setupAnimation(spriteSheetLeft, 2, Duration.millis(200), true);
        }
    }

    // Sets up animation
    private void walkRight() {
        if (currentDirection != Direction.RIGHT) {
            currentDirection = Direction.RIGHT;
            setupAnimation(spriteSheetRight, 2, Duration.millis(200), true);
        }
    }

    // Sets up animation
    private void walkUpAfterAttack() {
        currentDirection = Direction.UP;
        setupAnimation(spriteSheetUp, 2, Duration.millis(200), true);
    }

    // Sets up animation
    private void walkDownAfterAttack() {
        currentDirection = Direction.DOWN;
        setupAnimation(spriteSheetDown, 2, Duration.millis(200), true);
    }

    // Sets up animation
    private void walkLeftAfterAttack() {
        currentDirection = Direction.LEFT;
        setupAnimation(spriteSheetLeft, 2, Duration.millis(200), true);
    }

    // Sets up animation
    private void walkRightAfterAttack() {
        currentDirection = Direction.RIGHT;
        setupAnimation(spriteSheetRight, 2, Duration.millis(200), true);
    }

    // Sets up animation
    private void setupAnimation(Image spriteSheet, int numberOfFrames, Duration frameTime, boolean loopAnimation) {
        characterView.setImage(spriteSheet);
        characterView.setViewport(new Rectangle2D(0, 0, 102, 102));

        if (timeline != null) {
            timeline.stop();
        }

        timeline = new Timeline(new KeyFrame(frameTime, e -> {
            int currentIndex = (int) (characterView.getViewport().getMinX() / 102) + 1;
            if (currentIndex >= numberOfFrames) currentIndex = 0;
            int column = currentIndex % 2;
            int row = currentIndex / 2;
            characterView.setViewport(new Rectangle2D(column * 102, row * 102, 102, 102));
        }));

        if (loopAnimation) {
            timeline.setCycleCount(Timeline.INDEFINITE);
        } else {
            timeline.setCycleCount(numberOfFrames);
        }
        timeline.play();
    }

    // used for collision detection
    public Rectangle2D getBoundingBox() {
        // reduction factor
        double reductionFactor = 0.2;
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
        return 100;
    }

    public void setHealth(double maxHealth) {
        health = maxHealth;
    }

    public void setX(int x) {
        characterView.setX(x);
    }

    public void setY(int y) {
        characterView.setY(y);
    }

    public Rectangle getDebugBoundingBox() {
        return debugBoundingBox;
    }

    // list of possible directions
    private enum Direction {
        UP, DOWN, LEFT, RIGHT, STANDING
    }

    public void standStill() {
        if (currentDirection != Direction.STANDING) {
            currentDirection = Direction.STANDING;
            Image standingImage = loadImage("images/standingSpriteSheet.png");
            if (standingImage != null) {
                characterView.setImage(standingImage);
                setupStandingAnimation();
            }
        }
    }

    public void playSwooshSound() {
        try {
            // Adjust the path to where your sound file is located
            URL resource = getClass().getResource("/sounds/swoosh.mp3");
            if (resource == null) {
                System.err.println("Sound file not found");
                return;
            }
            String path = resource.toString();
            Media sound = new Media(path);
            MediaPlayer mediaPlayer = new MediaPlayer(sound);
            mediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error playing the sound.");
        }
    }

    // Method to play rock sound
    public void playRockSound() {
        try {
            String soundPath = Objects.requireNonNull(getClass().getResource("/sounds/rock.mp3")).toExternalForm();
            Media sound = new Media(soundPath);
            MediaPlayer mediaPlayer = new MediaPlayer(sound);
            mediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace(); // Handle exception
        }
    }
}