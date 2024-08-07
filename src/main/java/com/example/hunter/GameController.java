/*
This is the GameController class, and it has a large part of the game's logic within it.
This class is responsible for:
- Initializing and resetting the game.
- spawning enemies
- removing enemies
- spawning projectiles
- removing projectiles
- keeping track of the score
- the game loop and updating the game
- the current age and the transition to the next age
- updating the user interface
- the difficulty and the amount of damage the player receives based on the difficulty
- the game over pop up
- boss logic
 */

package com.example.hunter;

import com.example.hunter.enemies.*;
import com.example.hunter.projectiles.*;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;

public class GameController {
    @FXML
    private ProgressBar healthBar; // UI
    @FXML
    private Label healthLabel; // UI
    @FXML
    private Label scoreLabel; // UI
    private Difficulty currentDifficulty = Difficulty.MEDIUM; // Default difficulty

    @FXML
    private ImageView characterView; // display
    private ArrayList<Enemy> enemies; // ArrayList of enemies
    private AnimationTimer gameLoop;
    @FXML
    private Pane gamePane; // visual display
    @FXML
    private Label ageDisplayLabel; // UI
    private long lastSpawnTime = 0; // used during spawn
    private final long spawnInterval = 1000; // spawn
    private Player player;
    private final ArrayList<Projectile> projectiles = new ArrayList<>();
    private int score = 0; // scores begins at 0 and is reset when game is reset
    private Age currentAge; // This keeps track of the current age
    private boolean bossDefeated; // This is used to update the age after the boss is killed
    private boolean isGameOver = false; // Game starts as playing
    private static GameState gameState; // An enum for game states
    private final List<Apple> apples = new ArrayList<>(); // used for apple objects.

    private final int initialPlayerX = 400; // spawn location of player
    private final int initialPlayerY = 400; // spawn location of player

    private List<ScoreEntry> scoreList = new ArrayList<>(); // list of top 10 best scores.
    private boolean bossSpawned; // keeps track of if the boss is on the screen
    private long lastPauseTime = 0; // This stops someone accidentally pausing then unpausing instantly
    private final Set<KeyCode> keysPressed = new HashSet<>(); // HashSet is for responsive controls
    // This initialises the game and sets up all the necessary elements.
    public void initialize() {
        bossSpawned = false;
        bossDefeated = false;
        score = 0;
        this.currentAge = Age.STONE_AGE; // Start at Stone Age
        try {
            URL imageUrl = getClass().getResource("/images/stoneAgeBackground.png");
            if (imageUrl == null) {
                System.out.println("Background image URL is null. Image not found.");
            } else {
                Image backgroundImage = new Image(imageUrl.toExternalForm());
                BackgroundImage bgImage = new BackgroundImage(backgroundImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
                Background background = new Background(bgImage);
                gamePane.setBackground(background);
            }
        } catch (Exception e) {
            System.out.println("Error loading background image: " + e.getMessage());
            e.printStackTrace();
        }
        gameState = GameState.PLAYING;
        updateScoreDisplay();
        enemies = new ArrayList<>();
        player = new Player(characterView, "images/character1.png", this);

        if (!gamePane.getChildren().contains(characterView)) {
            gamePane.getChildren().add(characterView);
        }
        boolean debugMode = false;
        if (debugMode && !gamePane.getChildren().contains(player.getDebugBoundingBox())) {
            gamePane.getChildren().add(player.getDebugBoundingBox());
        }

        Platform.runLater(() -> gamePane.requestFocus());
        gamePane.setOnKeyPressed(this::onKeyPressed);
        updateHealthBar();
        characterView.setX(initialPlayerX);
        characterView.setY(initialPlayerY);

        scoreList = ScoreEntry.loadScores();
        gamePane.setFocusTraversable(true);
        gamePane.requestFocus();
    }
    public void setScene(Scene scene) {
    }

    public ArrayList<Enemy> getEnemies() {
        return this.enemies;
    }
    // This spawns enemies and picks a random direction for them to spawn from. Also,
    // The enemies that are spawned depend on the current game's age.
    private void spawnEnemy() {
        if (enemies.size() >= 15) {
            return; // Do not spawn more enemies if the limit is reached
        }
        double x, y;
        int edge = (int) (Math.random() * 4); // 0: top, 1: right, 2: bottom, 3: left

        y = switch (edge) {
            case 0 -> { // top
                x = Math.random() * gamePane.getWidth();
                yield 0;
            }
            case 1 -> { // right
                x = gamePane.getWidth();
                yield Math.random() * gamePane.getHeight();
            }
            case 2 -> { // bottom
                x = Math.random() * gamePane.getWidth();
                yield gamePane.getHeight();
            }
            case 3 -> { // left
                x = 0;
                yield Math.random() * gamePane.getHeight();
            }
            default -> throw new IllegalStateException("Unexpected value: " + edge);
        };
        Enemy enemy;
        Random random = new Random();

        if (currentAge == Age.CLASSICAL_AGE) {
            int enemyType = random.nextInt(3); // Random number between 0 and 2
            switch (enemyType) {
                case 0:
                    enemy = new Gladiator(x, y, 1.5, 8, gamePane.getWidth(), gamePane.getHeight());
                    break;
                case 1:
                    enemy = new Hoplite(x, y, 0.8, 12, gamePane.getWidth(), gamePane.getHeight());
                    break;
                case 2:
                    enemy = new Centurion(x, y, 0.5, 16);
                    break;
                default:
                    throw new IllegalStateException("Unexpected enemy type: " + enemyType);
            }
        } else if (currentAge == Age.MEDIEVAL_AGE) {
            int enemyType = random.nextInt(3); // Random number between 0 and 2
            enemy = switch (enemyType) {
                case 0 -> new Archer(x, y, 1, 8, this);
                case 1 -> new Knight(x, y, 3.5, 12);
                case 2 -> new Peasant(x, y, 3, 8);
                default -> throw new IllegalStateException("Unexpected enemy type: " + enemyType);
            };
        } else if (currentAge == Age.MODERN_AGE) {
            int enemyType = random.nextInt(3); // Random number between 0 and 2
            enemy = switch (enemyType) {
                case 0 -> new Soldier(x, y, 1, 4, this);
                case 1 -> new Police(x, y, 0.5, 16);
                case 2 -> new Ned(x, y, 3.5, 8);
                default -> throw new IllegalStateException("Unexpected enemy type: " + enemyType);
            };
        } else if (currentAge == Age.FUTURE_AGE) {
            int enemyType = random.nextInt(3); // Random number between 0 and 2
            switch (enemyType) {
                case 0:
                    enemy = new SuperSoldier(x, y, 2, 12, this);
                    break;
                case 1:
                    enemy = new Dino(x, y, 3, 12);
                    break;
                case 2:
                    enemy = new Robot(x, y, 4, 40);
                    break;
                default:
                    throw new IllegalStateException("Unexpected enemy type: " + enemyType);
            }
        }else {
            enemy = new Dino(x, y, 1, 2);
        }
        if (isTimeForBoss() && !bossSpawned) {
            Enemy boss = selectBossForCurrentAge();
            gamePane.getChildren().add(boss.getImageView());
            enemies.add(boss);
            bossSpawned = true; // Prevent further boss spawning in this age
        }
        gamePane.getChildren().add(enemy.getImageView()); // Add the enemy image to the scene
        gamePane.getChildren().add(enemy.getDebugBoundingBox());
        if (!gamePane.getChildren().contains(enemy.getImageView())) {
            gamePane.getChildren().add(enemy.getImageView());
        }
        addEnemy(enemy);
    }

    public void addEnemy(Enemy enemy) {
        enemies.add(enemy);
    }
    // This method has a 10% chance of randomly dropping an apple
    // object when an enemy dies. It also removes the enemy object and its visual representation.
    public void removeEnemy(Enemy enemy) {
        playDeathSound();
        Random rand = new Random();
        if (rand.nextDouble() <=0.1) { // 10% chance to drop an apple
            Apple apple = new Apple(enemy.getX(), enemy.getY(), gamePane);
            apples.add(apple);
            gamePane.getChildren().add(apple.getImageView());
        }
        enemies.remove(enemy);
        gamePane.getChildren().remove(enemy.getImageView());
    }
    // This is used by the Player class to throw rocks
    public void createRockProjectile(double x, double y, double directionX, double directionY, double speed, int damage, boolean firedByPlayer) {
        Projectile projectile = new Rock(x, y, directionX, directionY, speed, damage, firedByPlayer);
        this.projectiles.add(projectile);
        addProjectileToGame(projectile);
    }
    // This is used by the Archer class to fire arrows.
    public void createArrowProjectile(double x, double y, double directionX, double directionY, double speed, int damage, boolean firedByPlayer) {
        Projectile projectile = new Arrow(x, y, directionX, directionY, speed, damage, firedByPlayer);
        this.projectiles.add(projectile);
        addProjectileToGame(projectile);
    }
    // Adds projectile objects to the game.
    public void addProjectileToGame(Projectile projectile) {
        gamePane.getChildren().add(projectile.getView());
    }
    // Begins the gameLoop by constantly calling the updateGame method
    private void setupGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                //System.out.println("GameLoop running"); // This will print continuously when the game loop is running
                if (gameState != GameState.PAUSED) {
                    if (now - lastSpawnTime >= spawnInterval * 1_000_000) {
                        spawnEnemy();
                        lastSpawnTime = now;
                    }
                    updateGame();
                    updateMovement();
                }
            }
        };
        gameLoop.start();
    }
    // Sets up the UI score
    private void updateScoreDisplay() {
        Platform.runLater(() -> {
            scoreLabel.setText("Score: " + score);
        });
    }
    // This is constantly called by the gameLoop, and
    // this continually updates the game and makes gameplay possible.
    private void updateGame() {
        if (gameState == GameState.PAUSED) {
            return;
        }

        if (gameState == GameState.GAME_OVER) {
            return;
        }
        if (player.getHealth() <= 0 && gameState != GameState.GAME_OVER) {
            endGame();
        }
        updatePlayerPosition(player);
        for (Enemy enemy : enemies) {
            updateEnemyPosition(enemy);
        }
        Iterator<Apple> appleIterator = apples.iterator();
        while (appleIterator.hasNext()) {
            Apple apple = appleIterator.next();
            if (apple.checkCollision(player)) {
                gamePane.getChildren().remove(apple.getImageView());
                player.setHealth(Math.min(player.getHealth() + 10, 100));
                appleIterator.remove();
                playAppleSound();
                updateHealthBar();
            }
        }
        ArrayList<Enemy> toRemove = new ArrayList<>();
        if (player.getHealth() <= 0 && gameState == GameState.PLAYING) {
            gameState = GameState.GAME_OVER;
            Platform.runLater(this::showGameOverPopup);
        }
        if (isGameOver) {
            Platform.runLater(this::showGameOverPopup);
            isGameOver = false;
        }

        for (Enemy enemy : enemies) {
            enemy.update(player);

            // Check for removal
            if (enemy.shouldBeRemoved()) {
                toRemove.add(enemy);
                if (enemy instanceof Boss) {
                    bossDefeated = true; // Set boss defeated flag if a Boss is removed
                }
            } else {
                // Perform collision check
                checkCollisionWithPlayer(enemy);
            }
        }
        // Remove the enemies that are marked for removal
        for (Enemy enemy : toRemove) {
            score++;
            updateScoreDisplay();
            removeEnemy(enemy);
        }
        ArrayList<Projectile> projectilesToRemove = new ArrayList<>();

        for (Projectile projectile : projectiles) {
            // Update each projectile
            projectile.update();

            // Check for collision with the player
            if (projectile.getBoundingBox().intersects(player.getBoundingBox())) {
                player.receiveDamage(projectile.getDamage());
                projectilesToRemove.add(projectile); // Remove the arrow after hitting the player
                continue; // Skip further checks and continue to the next projectile
            }

            // Check for collisions with enemies
            for (Enemy enemy : enemies) {
                if (projectile.getBoundingBox().intersects(enemy.getBoundingBox())) {
                    if (projectile.isFiredByPlayer()) {
                        enemy.receiveDamage(projectile.getDamage(), player);
                        projectilesToRemove.add(projectile);
                        break;
                    }
                }
            }
            // Check if projectile is out of bounds
            if (isOutOfBounds(projectile)) {
                projectilesToRemove.add(projectile);
            }
        }
        // Remove projectiles that are marked for removal
        projectiles.removeAll(projectilesToRemove);
        for (Projectile projectile : projectilesToRemove) {
            gamePane.getChildren().remove(projectile.getView());
        }
        if (bossDefeated) {
            transitionToNextAge();
            bossDefeated = false; // Reset the flag for the next age
        }
        // Remove projectiles
        projectiles.removeAll(projectilesToRemove);
        projectilesToRemove.forEach(projectile -> gamePane.getChildren().remove(projectile.getView()));
    }
    // the damage based on collision done to the player depends on the difficulty.
    private void checkCollisionWithPlayer(Enemy enemy) {
        int damage = switch (currentDifficulty) {
            case EASY -> 3;
            case HARD -> 15;
            default -> 10;
        };
        if (player.getBoundingBox().intersects(enemy.getBoundingBox())) {
            player.receiveDamage(damage);
        }
    }
    public void setPlayer(Player player) {
        this.player = player;
    }
    // This pauses the game if Q is pressed. Else, the player can move and attack.
    public void onKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.Q) {
            togglePauseState();
        } else {
            keysPressed.add(keyEvent.getCode());
            updateMovement();
        }
    }
    // stops the gameplay when paused with the Q key.
    private void togglePauseState() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPauseTime < 500) { // 500 milliseconds threshold
            return;
        }
        lastPauseTime = currentTime;

        System.out.println("Current GameState before toggling: " + gameState);
        if (gameState == GameState.PLAYING) {
            gameState = GameState.PAUSED;
            gameLoop.stop(); // Stop the game loop
            System.out.println("Game Paused");
        } else if (gameState == GameState.PAUSED) {
            gameState = GameState.PLAYING;
            gameLoop.start(); // Start the game loop
            gamePane.requestFocus(); // Ensure gamePane has focus
            System.out.println("Game Resumed");
        }
        System.out.println("Current GameState after toggling: " + gameState);
    }
    // Checks if a key has stopped being pushed down
    public void onKeyReleased(KeyEvent keyEvent) {
        keysPressed.remove(keyEvent.getCode());
        updateMovement();
    }
    // Used for the player to move either by WASD of arrows. Also, SPACE for attack and
    // Z for projectiles
    private void updateMovement() {
        if (gameState == GameState.PAUSED) {
            return; // Ignore movement commands if the game is paused
        }
        if (player == null) return;
        //System.out.println("updateMovement() called");

        double deltaX = 0;
        double deltaY = 0;

        if (keysPressed.contains(KeyCode.UP) || keysPressed.contains(KeyCode.W)) {
            deltaY = -5;
        }
        if (keysPressed.contains(KeyCode.DOWN) || keysPressed.contains(KeyCode.S)) {
            deltaY = 5;
        }
        if (keysPressed.contains(KeyCode.LEFT) || keysPressed.contains(KeyCode.A)) {
            deltaX = -5;
        }
        if (keysPressed.contains(KeyCode.RIGHT) || keysPressed.contains(KeyCode.D)) {
            deltaX = 5;
        }

        player.move(deltaX, deltaY);

        if (keysPressed.contains(KeyCode.SPACE)) {
            player.attack();
        }
        if (keysPressed.contains(KeyCode.Z)) {
            player.fireProjectile();
        }
    }
    // updates the UI health bar
    void updateHealthBar() {
        if (player != null) {
            double healthPercentage = player.getHealth() / player.getMaxHealth();
            healthBar.setProgress(healthPercentage);
            healthLabel.setText(String.format("%.0f", player.getHealth())); // Update label
        }
    }
    // The game state is set to GAME_OVER when the game ends.
    private void endGame() {
        if (gameState != GameState.GAME_OVER) {
            gameState = GameState.GAME_OVER;
            promptForPlayerName(score);
        }
    }
    // When the player's health is <= 0, this popup appears.
    private void showGameOverPopup() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Game Over");

        Label gameOverLabel = new Label("Game Over! Your score: " + score);
        Button resetButton = new Button("Restart Game");
        resetButton.setOnAction(event -> {
            resetGame();
            popupStage.close();
        });
        ToggleGroup difficultyGroup = new ToggleGroup();

        RadioButton easyButton = new RadioButton("Easy");
        easyButton.setToggleGroup(difficultyGroup);
        easyButton.setUserData(GameController.Difficulty.EASY);

        RadioButton mediumButton = new RadioButton("Medium");
        mediumButton.setToggleGroup(difficultyGroup);
        mediumButton.setUserData(GameController.Difficulty.MEDIUM);

        RadioButton hardButton = new RadioButton("Hard");
        hardButton.setToggleGroup(difficultyGroup);
        hardButton.setUserData(GameController.Difficulty.HARD);

        mediumButton.setSelected(true);

        difficultyGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                onDifficultySelected((GameController.Difficulty) newValue.getUserData());
            }
        });

        HBox difficultyLayout = new HBox(40, easyButton, mediumButton, hardButton);
        difficultyLayout.setAlignment(Pos.BOTTOM_CENTER);
        difficultyLayout.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));


        VBox layout = new VBox(10, gameOverLabel, difficultyLayout, resetButton);
        layout.setAlignment(Pos.CENTER);

        Scene popupScene = new Scene(layout, 300, 200);
        popupStage.setScene(popupScene);

        popupStage.showAndWait();
    }
    // resets the game state to make it possible to replay.
    private void resetGame() {
        keysPressed.clear();
        bossSpawned = false;
        bossDefeated = false;
        for (Enemy enemy : enemies) {
            gamePane.getChildren().remove(enemy.getImageView());
        }
        gamePane.getChildren().remove(characterView);
        characterView.setX(initialPlayerX);
        characterView.setY(initialPlayerY);
        gamePane.getChildren().add(characterView);
        score = 0;
        currentAge = Age.STONE_AGE;
        player.setHealth(player.getMaxHealth());
        updateScoreDisplay();
        updateHealthBar();
        // Clear projectiles and enemies
        for (Projectile projectile : projectiles) {
            gamePane.getChildren().remove(projectile.getView()); // Remove each projectile's visual from the pane
        }
        projectiles.clear(); // Clear the list of projectiles

        enemies.clear();

        // Update the age display label
        ageDisplayLabel.setText("Current Age: Stone Age");
        updateBackgroundImage("images/stoneAgeBackground.png");

        gameState = GameState.PLAYING;
        gameLoop.start();
    }
    public void onDifficultySelected(GameController.Difficulty selectedDifficulty) {
        setCurrentDifficulty(selectedDifficulty);
    }
    // Checks if a projectile is out of bounds
    private boolean isOutOfBounds(Projectile projectile) {
        double x = projectile.getX();
        double y = projectile.getY();
        return x < 0 || x > gamePane.getWidth() || y < 0 || y > gamePane.getHeight();
    }
    // If the player goes out of bounds, they are sent to the other side of the screen.
    public void updatePlayerPosition(Player player) {
        if (player.getX() > gamePane.getWidth()) {
            player.setX(0);
        } else if (player.getX() < 0) {
            player.setX((int) gamePane.getWidth());
        }
        if (player.getY() > gamePane.getHeight()) {
            player.setY(0);
        } else if (player.getY() < 0) {
            player.setY((int) gamePane.getHeight());
        }
    }
    // If an enemy goes out of bounds, they are sent to the other side of the screen.
    public void updateEnemyPosition(Enemy enemy) {
        if (enemy.getX() > gamePane.getWidth()) {
            enemy.setX(0);
        } else if (enemy.getX() < 0) {
            enemy.setX((int) gamePane.getWidth());
        }
        if (enemy.getY() > gamePane.getHeight()) {
            enemy.setY(0);
        } else if (enemy.getY() < 0) {
            enemy.setY((int) gamePane.getHeight());
        }
    }
    // used to start the game
    public void startGame() {
        setupGameLoop();
    }
    // Used by the Soldier class to fire bullets.
    public void createBulletProjectile(double x, double y, double directionX, double directionY, double speed, int damage, boolean firedByPlayer) {
            Projectile projectile = new Bullet(x, y, directionX, directionY, speed, damage, firedByPlayer);
            this.projectiles.add(projectile);
            addProjectileToGame(projectile);
    }
    // Used by the Tank class to fire rockets.
    public void createRocketProjectile(double x, double y, double directionX, double directionY, double speed, int damage, boolean firedByPlayer) {
        Projectile projectile = new Rocket(x, y, directionX, directionY, speed, damage, firedByPlayer);
        this.projectiles.add(projectile);
        addProjectileToGame(projectile);
    }
    // Used by the SuperSoldier class to fire lasers.
    public void createLaserProjectile(double x, double y, double directionX, double directionY, double projectileSpeed, int damage, boolean b) {
        double speed = 8;
        boolean firedByPlayer = false;
        Projectile projectile = new Laser(x, y, directionX, directionY, speed, damage, firedByPlayer);
        this.projectiles.add(projectile);
        addProjectileToGame(projectile);
    }
    // A list of the Game's ages.
    public enum Age {
        STONE_AGE,
        CLASSICAL_AGE,
        MEDIEVAL_AGE,
        MODERN_AGE,
        FUTURE_AGE
    }
    // When playing, the gameLoop runs and player movement and attacks are possible.
    // These are suspended when the game is paused or over.
    public enum GameState {
        PLAYING,
        PAUSED,
        GAME_OVER;


        public double getCharacterX() {
            return gameState.getCharacterX();
        }

        public void setCharacterX(double characterX) {
            gameState.setCharacterX(characterX);
        }

        public double getCharacterY() {
            return gameState.getCharacterY();
        }

        public void setCharacterY(double characterY) {
            gameState.setCharacterY(characterY);
        }
    }

    public Pane getGamePane() {
        return gamePane;
    }
    // After the player loses the game, they are asked to provide a String.
    // Then, this may be listed if it is in the top 10 recorded scores.
    private void promptForPlayerName(int score) {
        gameLoop.stop();
        Platform.runLater(() -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Game Over");
            dialog.setHeaderText("Enter your name for the scoreboard:");
            dialog.setContentText("Name:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(name -> {
                scoreList.add(new ScoreEntry(name, score));
                Collections.sort(scoreList);
                if (scoreList.size() > 10) {
                    scoreList = scoreList.subList(0, 10); // Keep only top 10 scores
                }
                ScoreEntry.saveScores(scoreList); // Save the updated list of scores
                showScoreboard();
                Platform.runLater(this::showGameOverPopup);
            });
        });
    }
    // updates the age based on score. The boss also needs to be defeated.
    private void updateAge() {
        if(currentAge == Age.FUTURE_AGE){
        return;}
        Age newAge = currentAge;

        if (score >= 200) {
            newAge = Age.FUTURE_AGE;
        } else if (score >= 120) {
            newAge = Age.MODERN_AGE;
        } else if (score >= 40) {
            newAge = Age.MEDIEVAL_AGE;
        } else if (score >= 20) {
            newAge = Age.CLASSICAL_AGE;
        }
        if (newAge != currentAge) {
            clearEnemies(); // Clear all existing enemies before changing the age
            currentAge = newAge;
            changeGameElementsForAge();
        }
    }
    // provides the background image for each age.
    private void changeGameElementsForAge() {
        String ageText = "";
        String backgroundImageUrl = switch (currentAge) {
            case STONE_AGE -> {
                ageText = "Stone Age";
                yield "/images/stoneAgeBackground.png";
            }
            case CLASSICAL_AGE -> {
                ageText = "Classical Age";
                yield "/images/classicalAgeBackground.png";
            }
            case MEDIEVAL_AGE -> {
                ageText = "Medieval Age";
                yield "/images/medievalAgeBackground.png";
            }
            case MODERN_AGE -> {
                ageText = "Modern Age";
                yield "/images/modernAgeBackground.png";
            }
            case FUTURE_AGE -> {
                ageText = "Future Age";
                yield "/images/futureAgeBackground.png";
            }
        };
        updateBackgroundImage(backgroundImageUrl);
        String finalAgeText = ageText;
        Platform.runLater(() -> ageDisplayLabel.setText("Current Age: " + finalAgeText));
    }
    // Shows a list of the top 10 scores with the names associated with the scores.
    private void showScoreboard() {
        List<ScoreEntry> scoreList = ScoreEntry.loadScores();
        Stage scoreboardStage = new Stage();
        scoreboardStage.initModality(Modality.APPLICATION_MODAL);
        scoreboardStage.setTitle("Scoreboard");

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);

        for (ScoreEntry entry : scoreList) {
            Label scoreLabel = new Label(entry.playerName() + ": " + entry.score());
            layout.getChildren().add(scoreLabel);
        }

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> scoreboardStage.close());

        layout.getChildren().add(closeButton);

        Scene scene = new Scene(layout, 300, 400);
        scoreboardStage.setScene(scene);
        scoreboardStage.showAndWait();
    }
    // Changed the background image.
    private void updateBackgroundImage(String imageUrl) {
        try {
            Image background = new Image(Objects.requireNonNull(getClass().getResourceAsStream(imageUrl)));
            BackgroundSize bgSize = new BackgroundSize(
                    BackgroundSize.AUTO, BackgroundSize.AUTO,
                    false, false,
                    true, true
            );
            BackgroundImage bgImage = new BackgroundImage(
                    background,
                    BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    bgSize
            );
            gamePane.setBackground(new Background(bgImage));
        } catch (Exception e) {
            System.out.println("Error loading background image: " + e.getMessage());
            e.printStackTrace();
        }
    }
    // removes enemy objects from the game.
    private void clearEnemies() {
        for (Enemy enemy : enemies) {
            gamePane.getChildren().remove(enemy.getImageView());
            gamePane.getChildren().remove(enemy.getDebugBoundingBox());
        }
        enemies.clear();
    }
    // Checks when the boss should spawn based on score.
    private boolean isTimeForBoss() {
        // Check if the boss has already been spawned in the current age
        if (bossSpawned) {
            return false; // Boss already spawned, don't spawn again
        }
        // Spawn boss based on score thresholds and current age
        return switch (currentAge) {
            case STONE_AGE -> score >= 20;
            case CLASSICAL_AGE -> score >= 40;
            case MEDIEVAL_AGE -> score >= 120;
            case MODERN_AGE -> score >= 200;
            case FUTURE_AGE -> score >= 220;
        };
    }
    // The enemies are cleared before changing the age. Also, the assets are changed,
    // bossSpawned is reset to false so that the next boss can spawn.
    private void transitionToNextAge() {
        clearEnemies();
        updateAge();
        changeGameElementsForAge();
        bossSpawned = false;
    }
    // This method changed the boss based on the current age then spawns the boss.
    private Enemy selectBossForCurrentAge() {
        double x, y;
        int edge = (int) (Math.random() * 4); // 0: top, 1: right, 2: bottom, 3: left

        y = switch (edge) {
            case 0 -> { // top
                x = Math.random() * gamePane.getWidth();
                yield 0;
            }
            case 1 -> { // right
                x = gamePane.getWidth();
                yield Math.random() * gamePane.getHeight();
            }
            case 2 -> { // bottom
                x = Math.random() * gamePane.getWidth();
                yield gamePane.getHeight();
            }
            case 3 -> { // left
                x = 0;
                yield Math.random() * gamePane.getHeight();
            }
            default -> throw new IllegalStateException("Unexpected value: " + edge);
        };
        return switch (currentAge) {
            case STONE_AGE -> new BigDino(x, y, 4, 20, this);
            case CLASSICAL_AGE -> new Cerberus(x, y, 5, 20, this);
            case MEDIEVAL_AGE -> new Bunny(x, y, 10, 20, this);
            case MODERN_AGE -> new Tank(x, y, 2, 40, this);
            case FUTURE_AGE -> new Alien(x, y, 2, 1000, this);
        };
    }
    // set boss to defeated in order to progress to next age.
    public void setBossDefeated(boolean defeated) {
        this.bossDefeated = defeated;
    }
    // List of difficulties.
    public enum Difficulty {
        EASY,
        MEDIUM,
        HARD
    }
    // Method to play death sound
    public void playDeathSound() {
        try {
            String soundPath = Objects.requireNonNull(getClass().getResource("/sounds/die.mp3")).toExternalForm();
            Media sound = new Media(soundPath);
            MediaPlayer mediaPlayer = new MediaPlayer(sound);
            mediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace(); // Handle exception
        }
    }
    public void playAppleSound() {
        try {
            String soundPath = Objects.requireNonNull(getClass().getResource("/sounds/apple.mp3")).toExternalForm();
            Media sound = new Media(soundPath);
            MediaPlayer mediaPlayer = new MediaPlayer(sound);
            mediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace(); // Handle exception
        }
    }
    // sets the difficult to easy, medium, or hard/
    public void setCurrentDifficulty(Difficulty difficulty) {
        this.currentDifficulty = difficulty;
    }
}