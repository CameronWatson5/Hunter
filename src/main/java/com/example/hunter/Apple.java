package com.example.hunter;

import javafx.animation.PauseTransition;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class Apple {
    private double x, y;
    private Rectangle boundingBox;
    private ImageView imageView;
    private Pane gamePane;

    public Apple(double x, double y, Pane gamePane) {
        this.x = x;
        this.y = y;
        this.gamePane = gamePane; // Initialize gamePane

        this.boundingBox = new Rectangle(x, y, 75, 75);

        Image image = new Image(getClass().getResourceAsStream("/images/Apple.png"));
        this.imageView = new ImageView(image);

        this.imageView.setFitWidth(75);
        this.imageView.setFitHeight(75);

        // Set the position of the ImageView
        this.imageView.setX(x);
        this.imageView.setY(y);

        scheduleRemoval(); // Schedule the removal
    }

    public ImageView getImageView() {
        return imageView;
    }

    public Rectangle getBoundingBox() {
        return boundingBox;
    }

    private void scheduleRemoval() {
        PauseTransition delay = new PauseTransition(Duration.seconds(5));
        delay.setOnFinished(event -> gamePane.getChildren().remove(imageView));
        delay.play();
    }

    public boolean checkCollision(Player player) {
        Rectangle2D playerBoundingBox = player.getBoundingBox();
        Rectangle2D appleBoundingBox = new Rectangle2D(
                boundingBox.getX(),
                boundingBox.getY(),
                boundingBox.getWidth(),
                boundingBox.getHeight()
        );

        return appleBoundingBox.intersects(playerBoundingBox);
    }
}

