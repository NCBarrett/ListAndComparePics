package org.example.listandcomparepics;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class Controller {

    @FXML public Button dirBrowser;
    @FXML public Button submitBtn;
    @FXML public Button newDirBtn;

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

    @FXML public TextField RegEx;
    @FXML public TextField RegEx2;

    private DirectoryWatcherService watcherService;
    private DirectoryListingService listingService;
    private Path currentWatchDir;
    private Stage stage;
    private String strFileName;

    public void initialize() {

        this.watcherService = new DirectoryWatcherService();
        this.listingService = new DirectoryListingService();

        fileListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue,
                 newValue) -> {
                    if (newValue != null) {
                        loadImage(newValue, imageViewer);
                        strFileName = newValue;
                        choiceGrpContainer.setDisable(false);
                        RadioButton selRadio = (RadioButton)
                                actionChoiceGrp.getSelectedToggle();
                        if (selRadio != null && selRadio.getText().equals("Rename: ")) {
                            fileName.setText(newValue);
                        }
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
//                        System.out.println("radioBtn = " + selText);

                        if (selText.equals("Rename: ")) {
//                            System.out.println("Rename");
                            fileName.setDisable(false);
                            dirName.setDisable(true);
                            fileName.setText(strFileName);
                            newDirBtn.setDisable(true);
                        } else {
//                            System.out.println("Move to");
                            dirName.setDisable(false);
                            fileName.setDisable(true);
                            newDirBtn.setDisable(false);
                        }
                        submitBtn.setDisable(false);
                    }
                }
        );

        choiceGrpContainer.setDisable(true);
        fileName.setDisable(true);
        dirName.setDisable(true);
        submitBtn.setDisable(true);
        newDirBtn.setDisable(true);
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
                    currentWatchDir, textRegEx.getText())); /// left side
            rtFileListView.setItems(listingService.getDirectoryListing( /// right side
                    currentWatchDir, rtTextRegEx.getText(), true));
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
            /// imageViewer.setImage(image) DOES NOT bind the image
            targetView.imageProperty().set(image);
        } catch (Exception e) {
//            System.err.println("Error loading image: " + filename);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Image Load Error");
            alert.setHeaderText("Failed to load image");
            alert.setContentText("Error loading image: " + filename);
            alert.showAndWait();
            targetView.imageProperty().set(null);
        }
    }

    public void submitButton(ActionEvent event) {
        RadioButton selRadio = (RadioButton) actionChoiceGrp.getSelectedToggle();

        if (selRadio.getText().equals("Rename: ")) {
            System.out.println("Rename button selected");
            String oldName = fileListView.getSelectionModel().getSelectedItem();
            Path sourcePath = currentWatchDir.resolve(oldName);
            Path destinationPath = currentWatchDir.resolve(fileName.getText());
//            System.out.println("sourcePath = " + sourcePath +
//                    "; destinationPath = " + destinationPath);

            try {
                // Case-only rename: go through a temp name first - NTFS issue
                if (sourcePath.toString().equalsIgnoreCase(destinationPath.toString())
                        && !sourcePath.toString().equals(destinationPath.toString())) {
                    Path tempPath = currentWatchDir.resolve(oldName + "_tmp_rename");
                    Files.move(sourcePath, tempPath, StandardCopyOption.REPLACE_EXISTING);
                    Files.move(tempPath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                }
//                System.out.println("File renamed successfully");
            } catch (IOException e) {
//                System.err.println("File could not be renamed: " + e.getMessage());
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("File Rename Error");
                alert.setHeaderText("Failed to rename file");
                alert.setContentText("File could not be renamed: " + e.getMessage());
                alert.showAndWait();
            }
            fileName.clear();
        } else {
            System.out.println("Move button selected");
            String name = fileListView.getSelectionModel().getSelectedItem();
            Path sourcePath = currentWatchDir.resolve(name);
            Path targetPath = Path.of(dirName.getText() + name);
            try {
                Files.move(sourcePath, targetPath, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("File Move Error");
                alert.setHeaderText("Failed to move file");
                alert.setContentText("File could not be moved: " + e.getMessage());
                alert.showAndWait();
            }
            dirName.clear();
        }
        actionChoiceGrp.selectToggle(null);
        choiceGrpContainer.setDisable(true);
        newDirBtn.setDisable(true);
        refreshListView();
        imageViewer.setImage(null);
        submitBtn.setDisable(true);
    }

    public void newDirBrowser(ActionEvent event) {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Select destination directory");

        File destDir = dirChooser.showDialog(stage);
        if (destDir != null) {
            dirName.setText(destDir.getAbsolutePath());
        }
    }
}
