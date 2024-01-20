package com.example.hunter;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GameStart extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hunter/game_screen.fxml"));
        Parent root = loader.load();

        GameController gameController = loader.getController();
        // Additional setup for gameController can be done here, if needed

        Scene scene = new Scene(root, 800, 600); // Set appropriate size for your game window
        primaryStage.setScene(scene);
        primaryStage.setTitle("Hunter");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
