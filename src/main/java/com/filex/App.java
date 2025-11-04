package com.filex;

import java.io.IOException;

import database.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        DatabaseManager.initialize();
        Parent root = FXMLLoader.load(getClass().getResource("/ui/MainUI.fxml"));
        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setTitle("FileX - An Offline File Converter");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}