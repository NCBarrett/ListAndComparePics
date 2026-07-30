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
import java.util.stream.Stream;

public class Controller {

    @FXML public Button dirBrowser;
    @FXML public Button submitBtn;

    @FXML public ComboBox<String> GirlID;
    @FXML public ComboBox<String> EyeColor;
    @FXML public ComboBox<String> HairColor;
    @FXML public ComboBox<String> BraSize;
    @FXML public ComboBox<String> ClothesType;
    
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
    public ComboBox ClothesStyle;
    public ComboBox TopPattern;
    public ComboBox BottomPattern;
    public ComboBox MainOnlyColor;
    public ComboBox MainColor2;
    public ComboBox BottomOnlyColor;
    public ComboBox MainColor3;
    public ComboBox MainColor4;
    public ComboBox MainColor5;


    private DirectoryWatcherService watcherService;
    private DirectoryListingService listingService;

    private Path currentWatchDir;

    private Stage stage;

    // Cycle protection flag: blocks the ComboBox listener from firing
    // when changes are made automatically via code rather than manually
    private boolean isAutoUpdating = false;

    public void initialize() {
        this.watcherService = new DirectoryWatcherService();
        this.listingService = new DirectoryListingService();

        // Left List Selection Listener
        fileListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue,
                 newValue) -> {
                    if (newValue != null) {
                        System.out.println("Updating left imageViewer");
                        loadImage(newValue, imageViewer);

                        isAutoUpdating = true;  // Activate cycle protection shield
                        extractID(newValue);
                        isAutoUpdating = false; // Deactivate shield
                    }
                }
        );

        // Right List Selection Listener
        rtFileListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue,
                 newValue) -> {
                    if (newValue != null) {
                        System.out.println("Updating right imageViewer");
                    }
                }
        );

        // GirlID ComboBox Listener
        GirlID.valueProperty().addListener((
                observable, oldValue, newValue) -> {
            if (isAutoUpdating) {
                return; // Stop the feedback refresh loop here
            }
            if (newValue != null && !newValue.isBlank()) {
                String selectedId = newValue.trim();
                // Dynamically route text to drive your right historical filtering
                rtTextRegEx.setText("^Girl " + selectedId);
            }
        });

        // Search text listeners triggering reactive list filters
        textRegEx.textProperty().addListener((observable, oldValue, newValue) -> {
            refreshListView();
        });

        rtTextRegEx.textProperty().addListener((observable, oldValue, newValue) -> {
            refreshListView();
        });

        rtFileListView.setCellFactory(param ->
                new ImageThumbListCell(currentWatchDir));
    }

    private String extractID(String newValue) {
        // Compile your core convention pattern. Skips 'Animated ' optionally.
        Pattern pattern = Pattern.compile("^(?:Animated )?Girl (\\d+)");
        Matcher matcher = pattern.matcher(newValue);
        String idNumber = "";

        if (matcher.find()) {
            idNumber = matcher.group(1); // Safely isolate the string of digits
            GirlID.setValue(idNumber);   // Programmatically snap the ComboBox value to match
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

            refreshListView(); // Initial listing load

            try {
                // Attach the background thread callback watcher system
                watcherService.startWatching(currentWatchDir, this::refreshListView);
            } catch (IOException e) {
                e.printStackTrace();
            }
            stage.sizeToScene();
        }
    }

    private void refreshListView() {
        if (currentWatchDir == null) return;

        // --- LEFT PANE PROCESSING (Worklist / Filtered Source files) ---
//        String leftRaw = (textRegEx.getText() != null) ? textRegEx.getText().trim() : "";
        String leftRaw;
        if (textRegEx.getText() != null) {
            leftRaw = textRegEx.getText().trim();
        } else {
            leftRaw = "";
        }

        if (!leftRaw.isEmpty()) {
            if (!leftRaw.startsWith("^")) leftRaw = "^" + leftRaw;
            if (!leftRaw.endsWith("$")) leftRaw = leftRaw + "$";
        }

        /// Fetch left items
        var leftItems = listingService.getDirectoryListing(currentWatchDir, leftRaw);

        /// Universal Filter: Completely remove any file starting with "animated"
        leftItems.removeIf(filename -> filename.toLowerCase().startsWith(
                "animated"));

        leftItems.sort((file1, file2) ->
                DirectoryListingService.naturalCompare(file1, file2));

        fileListView.setItems(leftItems);

        // --- RIGHT PANE PROCESSING (Clean Targets / Historical Baseline) ---
//        String rightRaw = (rtTextRegEx.getText() != null) ? rtTextRegEx.getText().trim() : "";
        String rightRaw;
        if (rtTextRegEx.getText() != null) {
            rightRaw = rtTextRegEx.getText().trim();
        } else {
            rightRaw = "";
        }

        if (!rightRaw.isEmpty()) {
            if (!rightRaw.startsWith("^")) rightRaw = "^" + rightRaw;
            if (!rightRaw.endsWith("$")) rightRaw = rightRaw + "$";
        }

        var rtItems = listingService.getDirectoryListing(currentWatchDir, rightRaw,
                true);

        /// Custom Two-Tier Sort: Bubble "Animated" files up, then default to natural
        /// compare
        rtItems.removeIf(filename -> filename.toLowerCase().startsWith("animated"));

        rtItems.sort((file1, file2) ->
            DirectoryListingService.naturalCompare(file1, file2));

        rtFileListView.setItems(rtItems);
//        if (!rtItems.isEmpty()) {
//            rtFileListView.scrollTo(rtItems.size());
//        }
    }

    private void loadImage(String filename, ImageView targetView) {
        File imageFile = new File(currentWatchDir.toString(), filename);
        System.out.println("In loadImage: Path = " + imageFile.getAbsolutePath());
        try {
            Image image = new Image(imageFile.toURI().toString());
            targetView.imageProperty().set(image);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Image Load Error");
            alert.setHeaderText("Failed to load image");
            alert.setContentText("Error loading image: " + filename);
            alert.showAndWait();
            targetView.imageProperty().set(null);
        }
    }

    public void submitButton(ActionEvent event) {
        String oldName = fileListView.getSelectionModel().getSelectedItem();
        if (oldName == null) return;

        Path sourcePath = currentWatchDir.resolve(oldName);
        String targetBaseName = endFilePath.getText().trim();
        if (targetBaseName.isEmpty()) return;

        String ext = oldName.contains(".") ? oldName.substring(oldName.lastIndexOf(".")) : ".jpg";
        String resolvedName = findNextAvailableName(targetBaseName, ext);
        Path destinationPath = currentWatchDir.resolve(resolvedName);

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("File Will Be Renamed");
        confirm.setHeaderText("File '" + oldName + "' will be renamed to '" + resolvedName + "'.");
        confirm.setContentText("Do you want to continue?");

        var result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Direct file rename execution block
                Files.move(sourcePath, destinationPath, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("File Rename Error");
                alert.setHeaderText("Failed to rename file");
                alert.setContentText("File could not be renamed: " + e.getMessage());
                alert.showAndWait();
            }
            refreshListView();
            imageViewer.setImage(null);
        }
    }

    private String findNextAvailableName(String baseNameWithoutExt, String extension) {
        Pattern seriesPattern = Pattern.compile("^" + Pattern.quote(baseNameWithoutExt) + " \\((\\d+)\\)");
        int highestSeriesNum = 0;
        boolean absoluteBaseExists = false;

        try (Stream<Path> stream = Files.list(currentWatchDir)) {
            for (Path path : stream.toList()) {
                String existingName = path.getFileName().toString();

                if (existingName.equalsIgnoreCase(baseNameWithoutExt + extension)) {
                    absoluteBaseExists = true;
                }

                Matcher m = seriesPattern.matcher(existingName);
                if (m.find()) {
                    int num = Integer.parseInt(m.group(1));
                    if (num > highestSeriesNum) {
                        highestSeriesNum = num;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not scan directory for sequence tracking: " + e.getMessage());
            return baseNameWithoutExt + extension;
        }

        if (!absoluteBaseExists && highestSeriesNum == 0) {
            return baseNameWithoutExt + extension;
        } else {
            int nextNum = Math.max(1, highestSeriesNum + 1);
            return baseNameWithoutExt + " (" + nextNum + ")" + extension;
        }
    }

    public void shutdown() {
        watcherService.stopWatching();
    }
}
