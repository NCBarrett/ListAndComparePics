package org.example.listandcomparepics;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;

public class HelloController {

    @FXML public Label dirChosen;

    @FXML public ListView<String> fileList;

    @FXML public Button dirBrowser;

    @FXML public TextField textRegEx;

    @FXML public void onDirBrowserClick(ActionEvent event) throws IOException {
        DirectoryChooser dC = new DirectoryChooser();
        dC.setInitialDirectory(new File(System.getProperty("user.home")));
        dC.setTitle("Select Directory");

        Stage stage = (Stage) dirBrowser.getScene().getWindow();
        
        File file = fc.showOpenDialog(stage);

        if (file != null) {

        }
    }
}
