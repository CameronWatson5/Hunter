package com.example.hunter;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.event.ActionEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ArrayList;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;

public class GameController {
    @FXML
    private ProgressBar healthBar;
    @FXML
    private Label healthLabel;

    @FXML
    private ImageView characterView;
    private ArrayList<Enemy> enemies;
    private AnimationTimer gameLoop;
    @FXML
    private Pane gamePane;
    private long lastSpawnTime = 0;
    private long spawnInterval = 2000; // spawn an enemy every 5000 milliseconds (5 seconds)
    private Player player;
    private ArrayList<Projectile> projectiles = new ArrayList<>();



    public ArrayList<Enemy> getEnemies() {
        return this.enemies;
    }
    private void spawnEnemy() {
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

        Enemy enemy = new Enemy(x, y, 1, 2);
        if (!gamePane.getChildren().contains(enemy.getImageView())) {
            gamePane.getChildren().add(enemy.getImageView());
        }
        addEnemy(enemy);
    }

    public void addEnemy(Enemy enemy) {
        enemies.add(enemy);
    }

    public void removeEnemy(Enemy enemy) {
        enemies.remove(enemy);
        gamePane.getChildren().remove(enemy.getImageView()); // Also remove the ImageView from the pane
    }
    public void initialize() {
        try {
            URL imageUrl = getClass().getResource("/images/staticBackground.png");
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

        enemies = new ArrayList<>();
        player = new Player(characterView, "/images/character1.png", this);
        Platform.runLater(() -> gamePane.requestFocus());
        gamePane.setOnKeyPressed(this::onKeyPressed);
        // Set initial position, etc.
        updateHealthBar(); // Initial health bar update
        characterView.setX(100); // Example position
        characterView.setY(100);
        setupGameLoop();
    }

    public void createProjectile(double x, double y, double directionX, double directionY, double speed) {
        Projectile projectile = new Projectile(x, y, directionX, directionY, speed);
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
                if (now - lastSpawnTime >= spawnInterval * 1_000_000) { // convert to nanoseconds
                    spawnEnemy();
                    lastSpawnTime = now;
                }
                updateGame();
            }
        };
        gameLoop.start();
    }

    private void updateGame() {
        // Temporary list to hold enemies that need to be removed
        ArrayList<Enemy> toRemove = new ArrayList<>();

        // Iterate over enemies to update them and check for collisions
        for (Enemy enemy : enemies) {
            enemy.update(player);

            // Check for removal
            if (enemy.shouldBeRemoved()) {
                toRemove.add(enemy);
            } else {
                // Perform collision check
                checkCollisionWithPlayer(enemy);
            }
        }

        // Remove the enemies that are marked for removal
        for (Enemy enemy : toRemove) {
            removeEnemy(enemy);
        }

        // Handle projectiles
        ArrayList<Projectile> projectilesToRemove = new ArrayList<>();

        for (Projectile projectile : projectiles) {
            //System.out.println("Updating projectile - X: " + projectile.getX() + ", Y: " + projectile.getY());
            projectile.update();



        // Check for collisions with enemies
            for (Enemy enemy : enemies) {
                if (projectile.getBoundingBox().intersects(enemy.getBoundingBox())) {
                    enemy.receiveDamage(projectile.getDamage());
                    projectilesToRemove.add(projectile);
                    break; // Assuming one projectile can only hit one enemy
                }
            }

            // Check if projectile is out of bounds and mark it for removal
            if (isOutOfBounds(projectile)) {
                projectilesToRemove.add(projectile);
            }
        }

        // Remove projectiles
        projectiles.removeAll(projectilesToRemove);
        projectilesToRemove.forEach(projectile -> gamePane.getChildren().remove(projectile.getView()));
    }


    private void checkCollisionWithPlayer(Enemy enemy) {
        if (player.getBoundingBox().intersects(enemy.getBoundingBox())) {
            player.receiveDamage(10);
        }
    }
    public void setPlayer(Player player) {
        this.player = player;
    }

    // Method to handle keyboard input
    public void onKeyPressed(KeyEvent keyEvent) {
        if (player == null) return;

        switch (keyEvent.getCode()) {
            case UP:    player.move(0, -10); break; // Move up
            case DOWN:  player.move(0, 10); break; // Move down
            case LEFT:  player.move(-10, 0); break; // Move left
            case RIGHT: player.move(10, 0); break; // Move right
            case SPACE: player.attack(); break; // Attack
            case Z: player.fireProjectile(); break;
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

    public void handleButtonClick(ActionEvent actionEvent) {
    }
    private boolean isOutOfBounds(Projectile projectile) {
        // Implement logic to determine if projectile is out of the visible area
        // For example:
        double x = projectile.getX();
        double y = projectile.getY();
        return x < 0 || x > gamePane.getWidth() || y < 0 || y > gamePane.getHeight();
    }


    // Add other game-related methods here
}

