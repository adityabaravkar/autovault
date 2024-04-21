package com.autovault;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {
    private static final Logger logger = LogManager.getLogger(Main.class);

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/main-view.fxml")));
            Scene scene = new Scene(root);
            primaryStage.setTitle("Autovault");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            logger.error("Failed to load main view", e);
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
