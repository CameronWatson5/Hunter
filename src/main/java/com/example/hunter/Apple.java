/*
This is the Apple class, it is randomly dropped when an enemy object dies.
If the player collides with the apple, then the player gains 10 health points.
*/

package com.example.hunter;

import javafx.animation.PauseTransition;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.Objects;

public class Apple {
    private final double x;
    private final double y;
    private final Rectangle boundingBox; // used for collision
    private final ImageView imageView;
    private final Pane gamePane;

    public Apple(double x, double y, Pane gamePane) {
        this.x = x;
        this.y = y;
        this.gamePane = gamePane; // Initialize gamePane

        // create bounding box
        this.boundingBox = new Rectangle(x, y, 75, 75);

        // load image
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/Apple.png")));
        this.imageView = new ImageView(image);

        this.imageView.setFitWidth(75);
        this.imageView.setFitHeight(75);

        // match sprite with object's position
        this.imageView.setX(x);
        this.imageView.setY(y);

        scheduleRemoval(); // Gets removed after 5 seconds
    }
    // returns visual representation of apple
    public ImageView getImageView() {
        return imageView;
    }
    // used for collisions detection
    public Rectangle getBoundingBox() {
        return boundingBox;
    }
    // This removes the apple after 5 seconds.
    private void scheduleRemoval() {
        PauseTransition delay = new PauseTransition(Duration.seconds(5));
        delay.setOnFinished(event -> gamePane.getChildren().remove(imageView));
        delay.play();
    }
    // Checks if the player's bounding box intersects with the apple's bounding box.
    // If it does, the player receives 10 health points.
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


