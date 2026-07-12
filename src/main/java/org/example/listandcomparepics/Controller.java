package org.example.listandcomparepics;


import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public class Controller {

    @FXML public Button dirBrowser;
    @FXML public Button submitBtn;

    @FXML public HBox renameBtnBox;
    @FXML public HBox moveBtnBox;

    @FXML public ImageView imageViewer;
    @FXML public ImageView rtImageViewer;

    @FXML public Label dirChosen;

    @FXML public ListView<String> fileListView = new ListView<>();
    @FXML public ListView<String> rtFileListView = new ListView<>();

    @FXML public RadioButton renameBtn = new RadioButton("Rename: ");
    @FXML public RadioButton moveBtn = new RadioButton("Move: ");

    @FXML public StackPane imageContainer;
    @FXML public StackPane rtImageContainer;

    @FXML public TextField textRegEx;
    @FXML public TextField fileName;
    @FXML public TextField dirName;
    @FXML public TextField rtTextRegEx;

    @FXML public ToggleGroup actionChoiceGrp = new ToggleGroup();

    @FXML public VBox leftPane;
    @FXML public VBox choiceGrpContainer;
    @FXML public VBox rightPane;
    @FXML public VBox root;
    @FXML public VBox dirPane;

    private DirectoryWatcherService watcherService;
    private DirectoryListingService listingService;
    private Path currentWatchDir;
    private Stage stage;

    public void initialize() {

        this.watcherService = new DirectoryWatcherService();
        this.listingService = new DirectoryListingService();

        fileListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue,
                 newValue) -> {
                    if (newValue != null) {
                        loadImage(newValue, imageViewer);
                        fileName.setText(newValue);
                        choiceGrpContainer.setDisable(false);
                    } else  {
                        choiceGrpContainer.setDisable(true);
                    }
        });

        rtFileListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue,
                 newValue) -> {
                    if (newValue != null) {
                        loadImage(newValue, rtImageViewer);
                    }
                });

        textRegEx.textProperty().addListener((
                observable, oldValue,
                newValue) -> {refreshListView();
        });

        rtTextRegEx.textProperty().addListener((
                observable, oldValue,
                newValue) -> { refreshListView();
        });

        renameBtn.setToggleGroup(actionChoiceGrp);
        moveBtn.setToggleGroup(actionChoiceGrp);

        actionChoiceGrp.selectedToggleProperty().addListener(
                (observable, oldToggle,
                 newToggle) -> {
                    if (newToggle != null) {
                        RadioButton selRadio =  (RadioButton) newToggle;
                        String selText = selRadio.getText();
                        System.out.println("radioBtn = " + selText);

                        if (selText.equals("Rename: ")) {
                            System.out.println("Rename");
                            fileName.setDisable(false);
                            dirName.setDisable(true);
                        } else {
                            System.out.println("Move to");
                            dirName.setDisable(false);
                            fileName.setDisable(true);
                        }
                        submitBtn.setDisable(false);
                    }
                }
        );

        choiceGrpContainer.setDisable(true);
        fileName.setDisable(true);
        dirName.setDisable(true);
        submitBtn.setDisable(true);
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

            stage.sizeToScene();
        }
    }

    private void refreshListView() {
        if (currentWatchDir != null) {
            /// Update the ListView items
            fileListView.setItems(listingService.getDirectoryListing(
                    currentWatchDir, textRegEx.getText()));
            rtFileListView.setItems(listingService.getDirectoryListing(
                    currentWatchDir, rtTextRegEx.getText()
            ));
//            resizeListViewToContent();
        }
    }

    public void shutdown() {
        watcherService.stopWatching();
    }

    private void loadImage(String filename, ImageView targetView) {
        /// Suggested by IDE
        File imageFile = new File(currentWatchDir.toString(), filename);
        System.out.println("Path = " + imageFile.getAbsolutePath());

        try {
            /// Load the image securely using getResourceAsStream
            Image image = new Image(imageFile.toURI().toString());
            /// imageViewer.setImage(image); DOES NOT bind the image
            targetView.imageProperty().set(image);
        } catch (Exception e) {
            System.err.println("Error loading image: " + filename);
            targetView.imageProperty().set(null);
        }
    }
}
