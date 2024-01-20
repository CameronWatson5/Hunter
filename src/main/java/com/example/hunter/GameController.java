package com.example.hunter;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.event.ActionEvent;
import javafx.scene.input.KeyEvent;

public class GameController {
    @FXML
    private ImageView characterView;

    private Player player;
    public void initialize() {
        Image characterImage = new Image(getClass().getResourceAsStream("/images/character.png"));
        characterView.setImage(characterImage);

        // Set initial position, etc.
        characterView.setX(100); // Example position
        characterView.setY(100);
        player = new Player(characterView);
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

