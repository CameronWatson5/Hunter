/*
This is the main method. The game starts here.
When this file is run, a start game pop up should appear.
This allows the user to select a difficulty and begin the game.
*/

package com.example.hunter;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Objects;

public class GameStart extends Application {
    private GameController gameController;
    // Loads the GUI for the game
    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hunter/game_screen.fxml"));
            Parent root = loader.load();

            this.gameController = loader.getController();

            Scene scene = new Scene(root, 800, 600);
            primaryStage.setScene(scene);

            gameController.setScene(scene);
            scene.setOnKeyPressed(gameController::onKeyPressed);
            scene.setOnKeyReleased(gameController::onKeyReleased);

            showStartupPopup(primaryStage);

            primaryStage.setTitle("Hunter");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // This allows the player to either exit or start the game.
    private void showStartupPopup(Stage primaryStage) {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Hunter");

        popupStage.maximizedProperty().addListener((obs, oldMaximized, newMaximized) -> primaryStage.setMaximized(newMaximized));

        Image bgImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/extra.png")));
        BackgroundImage backgroundImage = new BackgroundImage(bgImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                BackgroundSize.DEFAULT);
        Background background = new Background(backgroundImage);

        Button startButton = new Button("Start Game");
        startButton.setFont(Font.font("Stencil", FontWeight.BOLD, 20));
        startButton.setOnAction(event -> {
            popupStage.close();
            gameController.startGame(); // Start the game logic
            primaryStage.show(); // Show the main game stage when Start is clicked
        });

        Button quitButton = new Button("Quit");
        quitButton.setFont(Font.font("Impact", FontWeight.BOLD, 20));
        quitButton.setOnAction(event -> Platform.exit());


        // Create a ToggleGroup for radio buttons
        ToggleGroup difficultyGroup = new ToggleGroup();

        // Create radio buttons
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

        // Controls Label
        Label controlsLabel = new Label("Controls:\n" +
                "Up arrow / W = Move Up\n" +
                "Left arrow / A = Move Left\n" +
                "Right arrow / D = Move Right\n" +
                "Down arrow / S = Move Down\n" +
                "Space = Melee Attack\n" +
                "Z = Projectile Attack\n" +
                "Q = Pause");
        controlsLabel.setFont(Font.font("Stencil", FontWeight.NORMAL, 14));
        controlsLabel.setTextFill(Color.web("#FFFFFF"));
        controlsLabel.setBackground(new Background(new BackgroundFill(Color.BLACK, new CornerRadii(5.0), Insets.EMPTY)));
        controlsLabel.setPadding(new Insets(10));

        // Layout
        VBox layout = new VBox(20, controlsLabel, startButton, difficultyLayout, quitButton); // Increase spacing between buttons
        layout.setAlignment(Pos.CENTER);
        layout.setBackground(background);

        Scene popupScene = new Scene(layout, 800, 600);
        popupStage.setScene(popupScene);
        popupStage.showAndWait();
    }
    public void onDifficultySelected(GameController.Difficulty selectedDifficulty) {
        gameController.setCurrentDifficulty(selectedDifficulty);
    }
    // The main method which begins the entire program
    public static void main(String[] args) {
            launch(args);
        }
    }
