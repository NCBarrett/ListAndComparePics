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
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.MicrosoftTagConstants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Controller {

    @FXML public Button dirBrowser;
    @FXML public Button submitBtn;

    @FXML public CheckBox BtmColor3ChBox;
    @FXML public CheckBox BtmColor4ChBox;
    @FXML public CheckBox BtmColor5ChBox;
    @FXML public CheckBox TopColor3ChBox;
    @FXML public CheckBox TopColor4ChBox;
    @FXML public CheckBox TopColor5ChBox;    
    
    @FXML public ComboBox<String> GirlID;
    @FXML public ComboBox<String> EyeColor;
    @FXML public ComboBox<String> HairColor;
    @FXML public ComboBox<String> BraSize;
    @FXML public ComboBox<String> ClothesType;
    @FXML public ComboBox<String> ClothesStyle;
    @FXML public ComboBox<String> TopPatternType;
    @FXML public ComboBox<String> MainOnlyColor;
    @FXML public ComboBox<String> MainColor2;
    @FXML public ComboBox<String> MainColor3;
    @FXML public ComboBox<String> MainColor4;
    @FXML public ComboBox<String> MainColor5;
    @FXML public ComboBox<String> BottomPatternType;
    @FXML public ComboBox<String> BottomPatternSubType;
    @FXML public ComboBox<String> BottomOnlyColor;
    @FXML public ComboBox<String> BottomColor2;
    @FXML public ComboBox<String> BottomColor3;
    @FXML public ComboBox<String> BottomColor5;
    @FXML public ComboBox<String> BottomColor4;
    @FXML public ComboBox<String> Scene;
    @FXML public ComboBox<String> TopPatternSubType;

    @FXML public HBox dirPane;

    @FXML public ImageView imageViewer;

    @FXML public Label dirChosen;

    @FXML public ListView<String> fileListView = new ListView<>();
    @FXML public ListView<String> rtFileListView = new ListView<>();

    @FXML public StackPane imageContainer;

    @FXML public TextField textRegEx;
    @FXML public TextField rtTextRegEx;
    @FXML public TextField RegEx;
    @FXML public TextField RegEx2;
    @FXML public TextField tagString;

    @FXML public ToggleGroup profileModeGroup;
    @FXML public RadioButton useExistingProfileRadio;
    @FXML public RadioButton newProfileRadio;

    @FXML public VBox leftPane;
    @FXML public VBox rightPane;
    @FXML public VBox root;

    @FXML public TitledPane BottomsPanel;

    private DirectoryWatcherService watcherService;
    private DirectoryListingService listingService;

    private Path currentWatchDir;

    // Additional storage mappings for your new layout categories
    private final Path styleFile = Path.of("saved_clothes_styles.txt");
    private final Path patternCatFile = Path.of("saved_pattern_categories.txt");
    private final Path patternSubFile = Path.of("saved_pattern_subcategories.txt");
    private final Path colorsFile = Path.of("saved_discovered_colors.txt");

    private Stage stage;

    // Cycle protection flag: blocks the ComboBox listener from firing
    // when changes are made automatically via code rather than manually
    private boolean isAutoUpdating = false;

    public void initialize() {
        this.watcherService = new DirectoryWatcherService();
        this.listingService = new DirectoryListingService();

        // Build in a 'RESET' sequence

        /// =================================================================
        /// INITIALIZE STANDARD DROPDOWN OPTIONS
        /// =================================================================

        // Load saved custom entries from disk if you have previously discovered any
        loadSavedTags(styleFile, ClothesStyle);
        loadSavedTags(patternCatFile, TopPatternType);
        loadSavedTags(patternCatFile, BottomPatternType);
        loadSavedTags(patternSubFile, TopPatternSubType);

        // Shared color dictionary loader (all color boxes pull from the same
        // persistent file)
        loadSavedTags(colorsFile, MainOnlyColor);
        loadSavedTags(colorsFile, MainColor2);
        loadSavedTags(colorsFile, MainColor3);
        loadSavedTags(colorsFile, MainColor4);
        loadSavedTags(colorsFile, MainColor5);

        loadSavedTags(colorsFile, BottomOnlyColor);
        loadSavedTags(colorsFile, BottomColor2);
        loadSavedTags(colorsFile, BottomColor3);
        loadSavedTags(colorsFile, BottomColor4);
        loadSavedTags(colorsFile, BottomColor5);

        EyeColor.getItems().setAll("Blue", "Black", "Brown", "Green", "Can't See");
        HairColor.getItems().setAll("Red", "Blond", "Brown", "Black");
        BraSize.getItems().setAll("Medium", "Large", "Extra Large");

        /// 1. Clothes Types (Ordered by frequency of occurrence)
        if (ClothesType.getItems().isEmpty()) {
            ClothesType.getItems().setAll("Bikini", "Pants & Shirt", "Lingerie",
                    "Other street clothes");
        }

        // 2. Clothes Styles
        if (ClothesStyle.getItems().isEmpty()) {
            ClothesStyle.getItems().setAll("Top and Bottom Match",
                    "Top compliments Bottom", "Top Only");
        }

        // 3. Pattern Category Divisions
        if (TopPatternType.getItems().isEmpty()) {
            TopPatternType.getItems().setAll("Main with Compliments", "Simple");
        }

        if (BottomPatternType.getItems().isEmpty()) {
            BottomPatternType.getItems().setAll("Main with Compliments", "Simple");
        }
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
                        updateTagPreviewString();
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

        // Listen for when you switch the profile mode radio buttons
        useExistingProfileRadio.toggleGroupProperty().get().selectedToggleProperty()
                .addListener((observable, oldToggle,
                              newToggle) -> {
            if (newToggle == newProfileRadio) {
                System.out.println("New Profile radio button selected: Clearing all inputs.");

                // Activate the cycle protection shield so clearing dropdown values
                // doesn't accidentally trigger other automatic list refreshes
                isAutoUpdating = true;

                // --- Clear Front Third (Physical Traits) ---
                EyeColor.setValue("");
                HairColor.setValue("");
                BraSize.setValue("");

                // --- Clear Middle Third (Clothing Architecture) ---
                ClothesType.setValue("");
                ClothesStyle.setValue("");

                TopPatternType.setValue("");
                TopPatternSubType.setValue("");

                MainOnlyColor.setValue("");
                MainColor2.setValue("");
                MainColor3.setValue("");
                MainColor4.setValue("");
                MainColor5.setValue("");

                BottomPatternType.setValue("");
                BottomOnlyColor.setValue("");
                BottomColor2.setValue("");
                BottomColor3.setValue("");
                BottomColor4.setValue("");
                BottomColor5.setValue("");

                // --- Clear Back Third (Scene Configuration) ---
                Scene.setValue("");

                // Uncheck all the extra color expansion check boxes
                TopColor3ChBox.setSelected(false);
                TopColor4ChBox.setSelected(false);
                TopColor5ChBox.setSelected(false);
                BtmColor3ChBox.setSelected(false);
                BtmColor4ChBox.setSelected(false);
                BtmColor5ChBox.setSelected(false);

                // Deactivate the shield and manually force your tag preview bar to update
                isAutoUpdating = false;
                updateTagPreviewString();
            }
        });


        // GirlID ComboBox Listener
        GirlID.valueProperty().addListener((
                observable, oldValue, newValue) ->
        {
            if (isAutoUpdating) {
                return; // Stop the feedback refresh loop here
            }
            if (newValue != null && !newValue.isBlank()) {
                String selectedId = newValue.trim();

                // 1. Instantly filter the right-pane historical view using your word
                // boundary fix
                rtTextRegEx.setText("^Girl " + selectedId + "\\b");
                refreshListView();

                // 2. Explicitly check the Radio Button Toggle before auto-populating
                if (useExistingProfileRadio.isSelected()) {
                    isAutoUpdating = true; // Turn on shield to safely update traits
                    lookupAndPopulateSubjectTraits(selectedId); // (Your metadata scanner
                                                                // function)
                    isAutoUpdating = false; // Turn off shield
                    System.out.println(
                            "Profile toggle active: Scanning history for traits.");
                } else {
                    // If "New Profile" is selected, safely wipe fields so you can input
                    // fresh details
                    isAutoUpdating = true;
                    EyeColor.setValue("");
                    HairColor.setValue("");
                    BraSize.setValue("");
                    ClothesType.setValue("");
                    ClothesStyle.setValue("");
                    TopPatternType.setValue("");
                    TopPatternSubType.setValue("");
                    MainOnlyColor.setValue("");
                    MainColor2.setValue("");
                    MainColor3.setValue("");
                    MainColor4.setValue("");
                    MainColor5.setValue("");
                    BottomPatternType.setValue("");
                    BottomOnlyColor.setValue("");
                    BottomColor2.setValue("");
                    BottomColor3.setValue("");
                    BottomColor4.setValue("");
                    BottomColor5.setValue("");
                    Scene.setValue("");

                    isAutoUpdating = false;
                    System.out.println(
                            "Manual override active: Fields cleared for raw configuration.");
                }
            }

            updateTagPreviewString();
        });

        HairColor.valueProperty().addListener((observable,
                                               oldValue, newValue) -> {
            updateTagPreviewString();
        });

        EyeColor.valueProperty().addListener((observable,
                                              oldValue, newValue) -> {
            updateTagPreviewString();
        });

        BraSize.valueProperty().addListener((observable,
                                             oldValue, newValue) -> {
            updateTagPreviewString();
        });

        Scene.valueProperty().addListener((observable,
                                           oldValue, newValue) -> {
            updateTagPreviewString();
        });

        // Listen for changes on the parent pattern category dropdown
        TopPatternType.valueProperty().addListener((observable,
                                                    oldValue, newValue) -> {
            // If the change is triggered programmatically by our auto-updater flag,
            // skip it
            if (isAutoUpdating) {
                return;
            }

            // Pass the newly selected category name to our filter procedure
            updatePatternSubcategories(newValue);
        });

        // Search text listeners triggering reactive list filters
        textRegEx.textProperty().addListener((observable,
                                              oldValue, newValue) -> {
            refreshListView();
        });

        rtTextRegEx.textProperty().addListener((observable,
                                                oldValue, newValue) -> {
            refreshListView();
        });

        rtFileListView.setCellFactory(param ->
                new ImageThumbListCell(currentWatchDir));

        MainColor3.disableProperty().bind(TopColor3ChBox.selectedProperty().not());
        MainColor4.disableProperty().bind(TopColor4ChBox.selectedProperty().not());
        MainColor5.disableProperty().bind(TopColor5ChBox.selectedProperty().not());
        BottomColor3.disableProperty().bind(BtmColor3ChBox.selectedProperty().not());
        BottomColor4.disableProperty().bind(BtmColor4ChBox.selectedProperty().not());
        BottomColor5.disableProperty().bind(BtmColor5ChBox.selectedProperty().not());

        BottomsPanel.setCollapsible(true);
        BottomsPanel.setExpanded(false);
    }

    private void loadSavedTags(Path file, ComboBox<String> comboBox) {
        // 1. Verify if the target configuration file exists on your disk
        if (Files.exists(file)) {
            try (Stream<String> lines = Files.lines(file)) {
                // Read lines, strip out extra empty spacing, and collect valid entries
                var items = lines.map(String::trim)
                        .filter(line -> !line.isEmpty())
                        .toList();

                // Push the collected historical entries into the dropdown list container
                comboBox.getItems().setAll(items);
                System.out.println("Successfully loaded tags from file: " +
                        file.getFileName());
            } catch (IOException e) {
                System.err.println("Could not read tag entries from " + file + ": " +
                        e.getMessage());
            }
        } else {
            // 2. Fallback initialization defaults if the text files do not exist yet
            if (comboBox == BraSize) {
                BraSize.getItems().setAll("M", "L", "XL", "XXL");
            }
            if (comboBox == ClothesType) {
                ClothesType.getItems().setAll("Bikini", "Pants & Shirt", "Lingerie",
                        "Other street clothes");
            }
            if (comboBox == ClothesStyle) {
                ClothesStyle.getItems().setAll("Top and Bottom Match",
                        "Top compliments Bottom", "Top Only");
            }
            if (comboBox == TopPatternType) {
                BottomPatternType.getItems().setAll("Main with Compliments", "Simple");
            }
        }
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
            if (!rightRaw.startsWith("^")) rightRaw = "^Girl " + rightRaw;

//            if (!rightRaw.endsWith("$")) rightRaw = rightRaw + "$";
        }

        var rtItems = listingService.getDirectoryListing(currentWatchDir, rightRaw,
                true);

        /// Custom Two-Tier Sort: Bubble "Animated" files up, then default to natural
        /// compare
        rtItems.removeIf(filename -> filename.toLowerCase().startsWith("animated"));

        rtItems.sort((file1, file2) ->
            DirectoryListingService.naturalCompare(file1, file2));

        rtFileListView.setItems(rtItems);

        /// needed only when we were adding files to the list
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

    }

    private void updatePatternSubcategories(String parentCategory) {
        // 1. Clear out any old subcategory options currently in the child box
        TopPatternSubType.getItems().clear();
        TopPatternSubType.setValue(null); // Reset the current display selection

        if (parentCategory != null) {
            String cleanCategory = parentCategory.trim();

            // 2. Check if the user selected "Main with Compliments"
            if (cleanCategory.equals("Main with Compliments")) {
                TopPatternSubType.getItems().setAll(
                        "Leopard skin",
                        "Polka dot",
                        "Stripes",
                        "Floral",
                        "Other"
                );
                System.out.println("Loaded subcategories for Main with Compliments");
            }

            // 3. Check if the user selected "Simple"
            if (cleanCategory.equals("Simple")) {
                TopPatternSubType.getItems().setAll(
                        "Full color",
                        "Equal stripes"
                );
                System.out.println("Loaded subcategories for Simple");
            }
        }
    }

    private void updateTagPreviewString() {
        StringBuilder tags = new StringBuilder();

        /// 1. Core Subject ID
        String id = GirlID.getValue();
        System.out.println("Girl ID: " + id + ".");
        if (id != null) {
            id = id.trim();
            if (!id.isEmpty()) {
                tags.append(id);

                /// 2. Eye Color
                String eyes = EyeColor.getValue();
                if (eyes != null) {
                    eyes = eyes.trim();
                    if (!eyes.isEmpty()) {
                        tags.append(";").append(eyes).append(" eyes");
                    }
                }

                /// 3. Hair Color
                String hair = HairColor.getValue();
                if (hair != null) {
                    eyes = eyes.trim();
                    if (!eyes.isEmpty()) {
                        tags.append(";").append(hair).append(" hair");
                    }
                }

                // 4. Bra Size
                String size = BraSize.getValue();
                if (size != null) {
                    size = size.trim();
                    if (!size.isEmpty()) {
                        tags.append(";").append(size).append(" breasts");
                    }
                }

                tags.append(";;;");

                /// 5. Scene Type (The last tag in your convention layout)
                String sceneText = Scene.getValue();
                if (sceneText != null) {
                    sceneText = sceneText.trim();
                    if (!sceneText.isEmpty()) {
                        tags.append(";").append(sceneText);
                    }
                }

                System.out.println(tags.toString());
                // Update the long preview TextField at the bottom of your window
                tagString.setText(tags.toString());
            }
        }
    }

    private void lookupAndPopulateSubjectTraits(String subjectId) {
        if (currentWatchDir == null) {
            return;
        }

        boolean foundProfile = false;

        // Scan the right-side historical files already loaded in your reference list
        for (String historicalFilename : rtFileListView.getItems()) {
            if (!foundProfile) {
                File imageFile = new File(currentWatchDir.toString(), historicalFilename);

                try {
                    ImageMetadata metadata = Imaging.getMetadata(imageFile);

                    if (metadata instanceof JpegImageMetadata jpegMetadata) {
                        // Extract the semicolon-separated Windows XP Keywords tag string
                        String[] keywords = jpegMetadata.findExifValueWithExactMatch(
                                MicrosoftTagConstants.EXIF_TAG_XPKEYWORDS
                        ).getStringValue().split(";");

                        // Verify that the file actually contains a valid tag structure
                        if (keywords.length >= 4) {
                            String embeddedId = keywords[0];

                            // If the metadata ID matches our current subject, copy the
                            // traits!
                            if (embeddedId.equals(subjectId)) {
                                String hair = keywords[1].trim();
                                String eyes = keywords[2].trim();
                                String size = keywords[3].trim();

                                // Update your UI dropdown menus programmatically
                                HairColor.setValue(hair);
                                EyeColor.setValue(eyes);
                                BraSize.setValue(size);

                                foundProfile = true;
                                System.out.println("Profile parsed from historical file: " +
                                        historicalFilename);
                            }
                        }
                    }
                } catch (Exception ignored) {
                    // Skip un-tagged or unreadable files and keep looking
                }
            }
        }

        // FALLBACK RESET: If no historical file has been tagged yet, clear traits for
        // manual entry
        if (!foundProfile) {
            HairColor.setValue("");
            EyeColor.setValue("");
            BraSize.setValue("");
            System.out.println("No historical metadata profile found. Fields cleared for manual setup.");
        }
    }

    public void shutdown() {
        watcherService.stopWatching();
    }
}

