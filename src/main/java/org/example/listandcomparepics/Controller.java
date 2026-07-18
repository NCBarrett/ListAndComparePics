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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    @FXML public TextField endFilePath;

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
                            dirName.clear();
                            fileName.setText(strFileName);
                            newDirBtn.setDisable(true);
                        } else {
//                            System.out.println("Move to");
                            dirName.setDisable(false);
                            fileName.setDisable(true);
                            fileName.clear();
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

            var rtItems = listingService.getDirectoryListing(
                    currentWatchDir, rtTextRegEx.getText(), true);
            rtFileListView.setItems(rtItems);

            if (!rtItems.isEmpty()) {
                rtFileListView.scrollTo(rtItems.size());
            }
        }
    }

    public void shutdown() {
        watcherService.stopWatching();
    }

    private void loadImage(String filename, ImageView targetView) {
        /// Suggested by IDE
        File imageFile = new File(currentWatchDir.toString(), filename);
        System.out.println("In loadImage: Path = " + imageFile.getAbsolutePath());

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
            System.out.println("sourcePath = " + sourcePath +
                    "; destinationPath = " + destinationPath);

            /// Checking case-insensitive rename: are both .equalsIgnoreCase
            /// and .equals true?
            System.out.println("sourcePath.toString().equalsIgnoreCase(\n" +
                    "destinationPath.toString() = " + sourcePath.toString()
                            .equalsIgnoreCase(destinationPath.toString()) +
                    "; !sourcePath.toString().equals(destinationPath.toString() = "+
                    !sourcePath.toString().equals(destinationPath.toString()));

            boolean isCaseOnlyRename = sourcePath.toString().equalsIgnoreCase(
                        destinationPath.toString())
                    && !sourcePath.toString().equals(destinationPath.toString());

            System.out.println("isCaseOnlyRename = " + isCaseOnlyRename);
            // + "; Files.exists(destinationPath) = " + Files.exists(destinationPath)

            /// Because of findNextAvailableName, don't need to worry about
            /// file name collisions

            String resolvedName = findNextAvailableName(fileName.getText());
            destinationPath = currentWatchDir.resolve(resolvedName);
            System.out.println("resolvedName = " + resolvedName);
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("File Will Be Renamed");
            confirm.setHeaderText("File '" + oldName + "' will be renamed to '" +
                    resolvedName + "'.");
            confirm.setContentText("Do you want to continue?");

            var result = confirm.showAndWait();
            if (result.isEmpty()) {
                fileName.clear();
            } else {
                /// Collision check: auto-resolve to the lowest available number,
                /// unless it's just a case fix
                try {
                    /// Case-only rename: go through a temp name first -
                    /// NTFS issue
                    if (isCaseOnlyRename) {
                        Path tempPath = currentWatchDir.resolve(oldName +
                                "_tmp_rename");
                        Files.move(sourcePath, tempPath,
                                StandardCopyOption.ATOMIC_MOVE);
                        Files.move(tempPath, destinationPath,
                                StandardCopyOption.ATOMIC_MOVE);
                    } else {
                        Files.move(sourcePath, destinationPath,
                                StandardCopyOption.ATOMIC_MOVE);
                    }
//                System.out.println("File renamed successfully");
                } catch (IOException e) {
//                System.err.println("File could not be renamed: " + e.getMessage());
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("File Rename Error");
                    alert.setHeaderText("Failed to rename file");
                    alert.setContentText("File could not be renamed: " +
                            e.getMessage());
                    alert.showAndWait();
                }
                fileName.clear();
            }
        } else {
            System.out.println("Move button selected");
            String name = fileListView.getSelectionModel().getSelectedItem();
            Path sourcePath = currentWatchDir.resolve(name);
            Path targetPath = Path.of(dirName.getText()).resolve(name);
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

    private String findNextAvailableName(String desiredName) {
        // Split into: everything before the first number, the number itself,
        // everything after
        // Add a *third* group in regex for detecting series numbers
        Pattern p = Pattern.compile("^(.*?)(\\d+)?(.*)$");
        Matcher m = p.matcher(desiredName);

        if (!m.matches()) {
            return desiredName; // no number found in the name — can't auto-resolve,
                                // leave as-is
        }

        String prefix = m.group(1);
        String rtInfix = m.group(3);
        String suffix = m.group(4);

        int n = 1;
        while (true) {
            String candidate = prefix + n + rtInfix + suffix;
            if (!Files.exists(currentWatchDir.resolve(candidate))) {
                return candidate;
            }
            n++;
        }
    }
}
