package com.example.hunter;
import javafx.scene.image.ImageView;


public class Player {
    private ImageView characterView;

    public Player(ImageView characterView) {
        this.characterView = characterView;
    }

    public void move(int dx, int dy) {
        characterView.setX(characterView.getX() + dx);
        characterView.setY(characterView.getY() + dy);
    }
    // Other player methods...
}
