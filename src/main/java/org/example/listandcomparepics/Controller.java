package org.example.listandcomparepics;


import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class Controller {

    private DirectoryWatcherService watcherMgr;

    @FXML public Label dirChosen;

    @FXML public ListView<String> fileListView = new ListView<>();
//    @FXML public ListView<File> fileListView = new ListView<>();

    @FXML public Button dirBrowser;

    @FXML public TextField textRegEx;

    @FXML public VBox root;


    private DirectoryWatcherService watcherService;
    private DirectoryListingService listingService;
    private Path currentWatchDir;
    private Stage stage;

    public void initialize() {

        this.watcherService = new DirectoryWatcherService();
        this.listingService = new DirectoryListingService();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void onDirBrowserClick() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Select Directory");

        File selectedDir = dirChooser.showDialog(stage);
        if (selectedDir != null) {
            dirChosen.setText(selectedDir.getAbsolutePath());
            currentWatchDir = selectedDir.toPath();

            /// 1. Refresh List
            refreshListView();

            /// 2. Start watching directory for changes
            try {
                watcherService.startWatching(currentWatchDir, this::refreshListView);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void refreshListView() {
        if (currentWatchDir != null) {
            /// Update the ListView items
            fileListView.setItems(listingService.getDirectoryListing(currentWatchDir));
        }
    }

    private void shutdown() {
        watcherService.stopWatching();
    }
}

//        fileListView.setPlaceholder(new Label("No Directory Selected"));
//    ObservableList<File> fileObservableList = FXCollections.observableArrayList();