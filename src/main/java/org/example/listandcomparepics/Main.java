package org.example.listandcomparepics;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                Main.class.getResource("mainView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        Controller controller = fxmlLoader.getController();
        controller.setStage(stage);

        stage.setTitle("List And Compare Pics");
        stage.setScene(scene);

        stage.setOnCloseRequest((WindowEvent event) -> {
            controller.shutdown();});

        stage.show();
    }
}
