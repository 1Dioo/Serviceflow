package com.serviceflow;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
            App.class.getResource("/com/serviceflow/view/MainView.fxml")
        );

        Parent root = loader.load();

        Scene scene = new Scene(root, 1100, 700);

        stage.setTitle("Serviceflow");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}