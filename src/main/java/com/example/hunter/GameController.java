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
import javafx.event.ActionEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.*;

import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class GameController {
    @FXML
    private ProgressBar healthBar;
    @FXML
    private Label healthLabel;
    @FXML
    private Label scoreLabel;
    private Difficulty currentDifficulty = Difficulty.MEDIUM;

    @FXML
    private ImageView characterView;
    private ArrayList<Enemy> enemies;
    private AnimationTimer gameLoop;
    @FXML
    private Pane gamePane;
    @FXML
    private Label ageDisplayLabel;
    private long lastSpawnTime = 0;
    private long spawnInterval = 1000; // spawn
    private Player player;
    private ArrayList<Projectile> projectiles = new ArrayList<>();
    private int score = 0;
    private Age currentAge;
    private boolean gameOverPending = false;
    private boolean bossDefeated;
    private boolean isGameOver = false;
    private static GameState gameState; // An enum for game states
    private List<Apple> apples = new ArrayList<>();

    private int initialPlayerX = 400;
    private int initialPlayerY = 400;
    private boolean debugMode = true;
    private List<ScoreEntry> scoreList = new ArrayList<>();
    private boolean bigDinoDead = false;
    private boolean zeusDead = false;
    private boolean bossSpawned;
    private Scene scene;
    private Set<KeyCode> keysPressed = new HashSet<>();

    public void setScene(Scene scene) {
        this.scene = scene; // Set the Scene in your GameController
    }

    public ArrayList<Enemy> getEnemies() {
        return this.enemies;
    }

    private void spawnEnemy() {
        if (enemies.size() >= 15) {
            return; // Do not spawn more enemies if the limit is reached
        }
        double x, y;
        int edge = (int) (Math.random() * 4); // 0: top, 1: right, 2: bottom, 3: left

        switch (edge) {
            case 0: // top
                x = Math.random() * gamePane.getWidth();
                y = 0;
                break;
            case 1: // right
                x = gamePane.getWidth();
                y = Math.random() * gamePane.getHeight();
                break;
            case 2: // bottom
                x = Math.random() * gamePane.getWidth();
                y = gamePane.getHeight();
                break;
            case 3: // left
                x = 0;
                y = Math.random() * gamePane.getHeight();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + edge);
        }
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
                    enemy = new Centurion(x, y, 0.5, 50);
                    break;
                default:
                    throw new IllegalStateException("Unexpected enemy type: " + enemyType);
            }
        } else if (currentAge == Age.MEDIEVAL_AGE) {
            int enemyType = random.nextInt(3); // Random number between 0 and 2
            switch (enemyType) {
                case 0:
                    enemy = new Archer(x, y, 1, 8, this);
                    break;
                case 1:
                    enemy = new Knight(x, y, 3, 12, gamePane.getWidth(), gamePane.getHeight(), gamePane);
                    break;
                case 2:
                    enemy = new Peasant(x, y, 3.5, 8);
                    break;
                default:
                    throw new IllegalStateException("Unexpected enemy type: " + enemyType);
            }
        } else if (currentAge == Age.MODERN_AGE) {
            int enemyType = random.nextInt(3); // Random number between 0 and 2
            switch (enemyType) {
                case 0:
                    enemy = new Soldier(x, y, 1, 4, this);
                    break;
                case 1:
                    enemy = new Police(x, y, 0.5, 50);
                    break;
                case 2:
                    enemy = new Ned(x, y, 3.5, 8);
                    break;
                default:
                    throw new IllegalStateException("Unexpected enemy type: " + enemyType);
            }
        }else {
            enemy = new Dino(x, y, 1, 2); // For ages other than CLASSICAL_AGE
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


    public void removeEnemy(Enemy enemy) {
        Random rand = new Random();
        if (rand.nextDouble() <=0.1) { // 10% chance to drop an apple
            Apple apple = new Apple(enemy.getX(), enemy.getY(), gamePane);
            apples.add(apple);
            gamePane.getChildren().add(apple.getImageView());
        }
        enemies.remove(enemy);
        gamePane.getChildren().remove(enemy.getImageView());
    }
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
        player = new Player(characterView, "/images/character1.png", this);

        if (!gamePane.getChildren().contains(characterView)) {
            gamePane.getChildren().add(characterView);
        }
        if (debugMode && !gamePane.getChildren().contains(player.getDebugBoundingBox())) {
            gamePane.getChildren().add(player.getDebugBoundingBox());
        }

        Platform.runLater(() -> gamePane.requestFocus());
        gamePane.setOnKeyPressed(this::onKeyPressed);
        updateHealthBar();
        characterView.setX(initialPlayerX);
        characterView.setY(initialPlayerY);

        scoreList = ScoreEntry.loadScores();

    }

    public void createRockProjectile(double x, double y, double directionX, double directionY, double speed, int damage, boolean firedByPlayer) {

        Projectile projectile = new Rock(x, y, directionX, directionY, speed, damage, firedByPlayer);
        this.projectiles.add(projectile);
        addProjectileToGame(projectile);
    }
    public void createArrowProjectile(double x, double y, double directionX, double directionY, double speed, int damage, boolean firedByPlayer) {

        Projectile projectile = new Arrow(x, y, directionX, directionY, speed, damage, firedByPlayer);
        this.projectiles.add(projectile);
        addProjectileToGame(projectile);
    }

    public void addProjectileToGame(Projectile projectile) {
        gamePane.getChildren().add(projectile.getView());
    }

    private void setupGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - lastSpawnTime >= spawnInterval * 1_000_000) {
                    spawnEnemy();
                    lastSpawnTime = now;
                }
                updateGame();
            }
        };
        gameLoop.start();
    }

    private void updateScoreDisplay() {
        Platform.runLater(() -> {
            scoreLabel.setText("Score: " + score);
        });
    }

    private void updateGame() {

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
                updateHealthBar();
            }
        }
        ArrayList<Enemy> toRemove = new ArrayList<>();
        if (player.getHealth() <= 0 && gameState == GameState.PLAYING) {
            gameState = GameState.GAME_OVER;
            Platform.runLater(() -> showGameOverPopup());
        }
        if (isGameOver) {
            Platform.runLater(() -> showGameOverPopup());
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
        // Handle projectiles
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

    private void checkCollisionWithPlayer(Enemy enemy) {
        int damage;
        switch (currentDifficulty) {
            case EASY:
                damage = 1;
                break;
            case HARD:
                damage = 15;
                break;
            default:
                damage = 10;
        }
        if (player.getBoundingBox().intersects(enemy.getBoundingBox())) {
            player.receiveDamage(damage);
        }
    }


    public void setPlayer(Player player) {
        this.player = player;
    }

    public void onKeyPressed(KeyEvent keyEvent) {
        keysPressed.add(keyEvent.getCode());
        updateMovement();
    }

    public void onKeyReleased(KeyEvent keyEvent) {
        keysPressed.remove(keyEvent.getCode());
        updateMovement();
    }

    private void updateMovement() {
        if (player == null) return;

        double deltaX = 0;
        double deltaY = 0;

        if (keysPressed.contains(KeyCode.UP) || keysPressed.contains(KeyCode.W)) {
            deltaY -= 10;
        }
        if (keysPressed.contains(KeyCode.DOWN) || keysPressed.contains(KeyCode.S)) {
            deltaY += 10;
        }
        if (keysPressed.contains(KeyCode.LEFT) || keysPressed.contains(KeyCode.A)) {
            deltaX -= 10;
        }
        if (keysPressed.contains(KeyCode.RIGHT) || keysPressed.contains(KeyCode.D)) {
            deltaX += 10;
        }

        player.move(deltaX, deltaY);

        if (keysPressed.contains(KeyCode.SPACE)) {
            player.attack();
        }
        if (keysPressed.contains(KeyCode.Z)) {
            player.fireProjectile();
        }
    }

    void updateHealthBar() {
        if (player != null) {
            double healthPercentage = (double) player.getHealth() / player.getMaxHealth();
            healthBar.setProgress(healthPercentage);
            healthLabel.setText(String.format("%.0f", player.getHealth())); // Update label
        }
    }

    public void onPlayerHealthChanged() {
        updateHealthBar();
    }

    private void endGame() {
        if (gameState != GameState.GAME_OVER) {
            gameState = GameState.GAME_OVER;
            promptForPlayerName(score);

        }
    }

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

    private void resetGame() {
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
        updateBackgroundImage("/images/stoneAgeBackground.png");

        gameState = GameState.PLAYING;
        gameLoop.start();
    }
    public void onDifficultySelected(GameController.Difficulty selectedDifficulty) {
        setCurrentDifficulty(selectedDifficulty);
    }
    public void handleButtonClick(ActionEvent actionEvent) {
    }

    private boolean isOutOfBounds(Projectile projectile) {
        double x = projectile.getX();
        double y = projectile.getY();
        return x < 0 || x > gamePane.getWidth() || y < 0 || y > gamePane.getHeight();
    }

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

    public void startGame() {
        setupGameLoop();
    }

    public void createBulletProjectile(double x, double y, double directionX, double directionY, double speed, int damage, boolean firedByPlayer) {
        {
            Projectile projectile = new Bullet(x, y, directionX, directionY, speed, damage, firedByPlayer);
            this.projectiles.add(projectile);
            addProjectileToGame(projectile);
        }
    }

    public void createRocketProjectile(double x, double y, double directionX, double directionY, double speed, int damage, boolean firedByPlayer) {
        Projectile projectile = new Rocket(x, y, directionX, directionY, speed, damage, firedByPlayer);
        this.projectiles.add(projectile);
        addProjectileToGame(projectile);
    }

    public enum Age {
        STONE_AGE,
        CLASSICAL_AGE,
        MEDIEVAL_AGE,
        MODERN_AGE,
        FUTURE_AGE
    }

    public enum GameState {
        PLAYING,
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

    private void updateAge() {
        if(currentAge == Age.FUTURE_AGE){
        return;}
        Age newAge = currentAge;

        if (score >= 80) {
            newAge = Age.FUTURE_AGE;
        } else if (score >= 60) {
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

    private void changeGameElementsForAge() {
        String ageText = "";
        String backgroundImageUrl = "";
        switch (currentAge) {
            case STONE_AGE:
                ageText = "Stone Age";
                backgroundImageUrl = "/images/stoneAgeBackground.png";
                break;
            case CLASSICAL_AGE:
                ageText = "Classical Age";
                backgroundImageUrl = "/images/classicalAgeBackground.png";
                break;
            case MEDIEVAL_AGE:
                ageText = "Medieval Age";
                backgroundImageUrl = "/images/medievalAgeBackground.png";
                break;
            case MODERN_AGE:
                ageText = "Modern Age";
                backgroundImageUrl = "/images/modernAgeBackground.png";
                break;
            case FUTURE_AGE:
                ageText = "Future Age";
                backgroundImageUrl = "/images/futureAgeBackground.png";
                break;
        }
        updateBackgroundImage(backgroundImageUrl);
        String finalAgeText = ageText;
        Platform.runLater(() -> ageDisplayLabel.setText("Current Age: " + finalAgeText));
    }

    private void showScoreboard() {
        List<ScoreEntry> scoreList = ScoreEntry.loadScores();
        Stage scoreboardStage = new Stage();
        scoreboardStage.initModality(Modality.APPLICATION_MODAL);
        scoreboardStage.setTitle("Scoreboard");

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);

        for (ScoreEntry entry : scoreList) {
            Label scoreLabel = new Label(entry.getPlayerName() + ": " + entry.getScore());
            layout.getChildren().add(scoreLabel);
        }

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> scoreboardStage.close());

        layout.getChildren().add(closeButton);

        Scene scene = new Scene(layout, 300, 400);
        scoreboardStage.setScene(scene);
        scoreboardStage.showAndWait();
    }

    private void updateBackgroundImage(String imageUrl) {
        try {
            Image background = new Image(getClass().getResourceAsStream(imageUrl));
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

    private void clearEnemies() {
        for (Enemy enemy : enemies) {
            gamePane.getChildren().remove(enemy.getImageView());
            gamePane.getChildren().remove(enemy.getDebugBoundingBox());
        }
        enemies.clear();
    }

    private boolean isTimeForBoss() {
        // Check if the boss has already been spawned in the current age
        if (bossSpawned) {
            return false; // Boss already spawned, don't spawn again
        }
        // Spawn boss based on score thresholds and current age
        switch (currentAge) {
            case STONE_AGE:
                return score >= 20; // Spawn boss at score 20 in Stone Age
            case CLASSICAL_AGE:
                return score >= 60; // Spawn boss at score 40 in Classical Age
            case MEDIEVAL_AGE:
                return score >= 100; // Spawn boss at score 60 in Medieval Age
            case MODERN_AGE:
                return score >= 140; // Spawn boss at score 80 in Modern Age
            case FUTURE_AGE:
                return score >= 180; // Spawn boss at score 100 in Future Age
            default:
                return false;
        }
    }

    private void transitionToNextAge() {
        clearEnemies();
        updateAge();
        changeGameElementsForAge();
        bossSpawned = false;
    }

    private Age getNextAge() {
        if (currentAge == Age.STONE_AGE) {
            Age nextAge = Age.CLASSICAL_AGE;
            return nextAge;
        }
        else if (currentAge == Age.CLASSICAL_AGE) {
            Age nextAge = Age.MEDIEVAL_AGE;
            return nextAge;
        }
        else if (currentAge == Age.MEDIEVAL_AGE) {
            Age nextAge = Age.MODERN_AGE;
            return nextAge;
        }
        else if (currentAge == Age.MODERN_AGE) {
            Age nextAge = Age.FUTURE_AGE;
            return nextAge;
        }


        return null;
    }
    private Enemy selectBossForCurrentAge() {
        double x, y;
        int edge = (int) (Math.random() * 4); // 0: top, 1: right, 2: bottom, 3: left

        switch (edge) {
            case 0: // top
                x = Math.random() * gamePane.getWidth();
                y = 0;
                break;
            case 1: // right
                x = gamePane.getWidth();
                y = Math.random() * gamePane.getHeight();
                break;
            case 2: // bottom
                x = Math.random() * gamePane.getWidth();
                y = gamePane.getHeight();
                break;
            case 3: // left
                x = 0;
                y = Math.random() * gamePane.getHeight();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + edge);
        }
        switch (currentAge) {
            case STONE_AGE:
                return new BigDino(x,y,4,20, this);
            case CLASSICAL_AGE:
                return new Cerberus(x,y,5,20, this);
            case MEDIEVAL_AGE:
                return new Bunny(x,y,10,20, this);
            case MODERN_AGE:
                return new Tank(x,y,2,40, this);
            case FUTURE_AGE:
                return new BigDino(x,y,2,20, this);
            default:
                throw new IllegalStateException("Unknown Age: " + currentAge);
        }
    }
    public void setBossDefeated(boolean defeated) {
        this.bossDefeated = defeated;
    }
    public enum Difficulty {
        EASY,
        MEDIUM,
        HARD
    }
    public void setCurrentDifficulty(Difficulty difficulty) {
        this.currentDifficulty = difficulty;
    }
}