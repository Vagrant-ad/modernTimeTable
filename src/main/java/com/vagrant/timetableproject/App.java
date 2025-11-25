package com.vagrant.timetableproject;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    private static Scene scene;
     @Override
    public void start(Stage stage) throws IOException{
         FXMLLoader fxmlLoader = new FXMLLoader(
                 App.class.getResource("/com/vagrant/timetableproject/view.fxml")
         );
         Parent root = fxmlLoader.load();
         scene = new Scene(root,1100,850);
        stage.setTitle("Modern Timetable");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(700);
        stage.show();
     }
    public static void main(String[] args) {
         launch(args);
    }
}
