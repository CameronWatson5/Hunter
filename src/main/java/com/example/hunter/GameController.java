package com.example.hunter;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.event.ActionEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;

public class GameController {
    @FXML
    private ImageView characterView;
    private AnimationTimer gameLoop;
    @FXML
    private Pane gamePane;
    private Player player;
    public void initialize() {
        player = new Player(characterView, "/images/character1.png");
        Platform.runLater(() -> gamePane.requestFocus());
        gamePane.setOnKeyPressed(this::onKeyPressed);
        // Set initial position, etc.
        characterView.setX(100); // Example position
        characterView.setY(100);
        setupGameLoop();
    }

    private void setupGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateGame();
            }
        };
        gameLoop.start();
    }

    private void updateGame() {
        // Update game state, check for collisions, etc.
        // This method is called in each frame

        // Example: update player position
        // player.update();
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
        }
    }

    public void handleButtonClick(ActionEvent actionEvent) {
    }

    // Add other game-related methods here
}

