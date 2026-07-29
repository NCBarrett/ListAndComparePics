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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller {

    @FXML public Button dirBrowser;
    @FXML public Button submitBtn;

    @FXML public ComboBox GirlID;

    @FXML public HBox dirPane;

    @FXML public ImageView imageViewer;

    @FXML public Label dirChosen;

    @FXML public ListView<String> fileListView = new ListView<>();
    @FXML public ListView<String> rtFileListView = new ListView<>();

    @FXML public StackPane imageContainer;

    @FXML public TextField textRegEx;
    @FXML public TextField rtTextRegEx;

    @FXML public VBox leftPane;
    @FXML public VBox rightPane;
    @FXML public VBox root;

    @FXML public TextField RegEx;
    @FXML public TextField RegEx2;
    @FXML public TextField endFilePath;

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
                        System.out.println("Updating left imageViewer");
                        loadImage(newValue, imageViewer);
                        extractID(newValue);
                    }
        });

        rtFileListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue,
                 newValue) -> {
                    if (newValue != null) {
                        System.out.println("Updating right imageViewer");
//                        loadImage(newValue, rtImageViewer);
                    }
                });

        GirlID.valueProperty().addListener((
                observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.toString().isBlank()) {
                String selectedId = newValue.toString().trim();
            }
        })

        textRegEx.textProperty().addListener((
                observable, oldValue,
                newValue) -> {refreshListView();
        });

        rtTextRegEx.textProperty().addListener((
                observable, oldValue,
                newValue) -> { refreshListView();
        });

    }

    private String extractID(String newValue) {
        /// 1. Compile the naming convention pattern matching "Girl [number]"
        /// (?:Animated )? gracefully skips the optional 'Animated ' prefix
        Pattern pattern = Pattern.compile("^(?:Animated )?Girl (\\d+)");
        Matcher matcher = pattern.matcher(newValue);

        String idNumber = "";

        /// 2. Extract the number group if found
        if (matcher.find()) {
            idNumber = matcher.group(1); /// Extract the digits

            /// 3. Programmatically set the display value of your Combobox
            GirlID.setValue(idNumber);
        }

        return idNumber;
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

            /// --- LEFT PANE PROCESSING (ANOMALIES / BREAKING CODES) ---
            /// Get user regex, trim, and wrap with strict anchors if they
            /// aren't explicitly typed
            String leftRaw = "";
            if (textRegEx.getText() != null) {
                leftRaw = textRegEx.getText().trim();
            } else {
                leftRaw = "";
            }

            /// Wrap with strict ^ and $ string anchors if they aren't
            /// explicitly typed
            if (!leftRaw.isEmpty()) {
                if (!leftRaw.startsWith("^")) {
                    leftRaw = "^" + leftRaw;
                }
                if (!leftRaw.endsWith("$")) {
                    leftRaw = leftRaw + "$";
                }
            }

            /// Update the ListView items
            /// Pass the strict pattern to your left list view
            fileListView.setItems(listingService.getDirectoryListing(
                    currentWatchDir, leftRaw));

            // =================================================================
            // 2. RIGHT PANE PROCESSING (CLEAN TARGET CONVENTION)
            // =================================================================
            String rightRaw = "";
            if (rtTextRegEx.getText() != null) {
                rightRaw = endFilePath.getText().trim();
            } else {
                rightRaw = "";
            }

            if (!rightRaw.isEmpty()) {
                if (!rightRaw.startsWith("^")) {
                    rightRaw = "^" + rightRaw;
                }
                if (!rightRaw.endsWith("$")) {
                    rightRaw = rightRaw + "$";
                }
            }

            /// Fetch the baseline items matching your strict naming pattern
            var rtItems = listingService.getDirectoryListing(
                    currentWatchDir, rightRaw, true);

            /// Custom Two-Tier Sort: Bubble "Animated" files to the top,
            /// then sort naturally
            rtItems.sort((file1, file2) -> {
                boolean f1Anim = file1.toLowerCase().startsWith("animated");
                boolean f2Anim = file2.toLowerCase().startsWith("animated");

                // Tier 1 logic: One is animated, one is not
                if (f1Anim && !f2Anim) {
                    return -1; // Force file1 up to the top
                }
                if (!f1Anim && f2Anim) {
                    return 1;  // Force file2 up to the top
                }

                // Tier 2 logic: Both are animated OR both are normal (fallback to your natural compare)
                return DirectoryListingService.naturalCompare(file1, file2);
            });

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

    }

}
