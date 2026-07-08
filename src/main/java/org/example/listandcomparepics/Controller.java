package org.example.listandcomparepics;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Controller implements WatchDirCallBack {

    private DirectoryModel watcherMgr;

    @FXML public Label dirChosen;

    //@FXML public ListView<String> fileListView = new ListView<>();
    @FXML public ListView<File> fileListView = new ListView<>();

    @FXML public Button dirBrowser;

    @FXML public TextField textRegEx;

    @FXML public VBox root;

//    List<String> fileList = new ArrayList<>();
//    String dirPath;
    ObservableList<File> fileObservableList = FXCollections.observableArrayList();

    @FXML public void initialize() {
        fileListView.setPlaceholder(new Label("No Directory Selected"));
    }

    @Override
    @FXML public void onDirBrowserClick(ActionEvent event) throws IOException {
        DirectoryChooser dC = new DirectoryChooser();
        dC.setInitialDirectory(new File(System.getProperty("user.home")));
        dC.setTitle("Select Directory");

        Stage stage = (Stage) dirBrowser.getScene().getWindow();
        File selectedDir = dC.showDialog(stage);

        if (selectedDir != null) {
            String strPath = selectedDir.getAbsolutePath();
            dirChosen.setText(strPath);
            Path dirPath = Paths.get(strPath);

            File[] files = dirPath.toFile().listFiles();

            fileObservableList =
                    FXCollections.observableArrayList(files);

            fileListView.setCellFactory(param -> new ListCell<File>() {
                @Override
                protected void updateItem(File item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else  {
                        setText(item.getName());
                    }
                }
            });
        } else {
            dirChosen.setText("Directory Not Selected");
        }
    }
}

// use Files.walk(...) to include subdirectories
//            try (Stream<Path> paths = Files.list(dirPath)) {
//List<File> files = paths
//        .filter(Files::isRegularFile)
//        .map(Path::toFile)
//        .toList();
//            } catch (IOException e) {
//        e.printStackTrace();
//            }