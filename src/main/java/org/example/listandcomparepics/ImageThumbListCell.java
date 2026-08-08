package org.example.listandcomparepics;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.io.File;
import java.nio.file.Path;

public class ImageThumbListCell extends ListCell<String> {
    private final HBox container = new HBox(10);
    private final ImageView thumbView = new ImageView();
    private final Label nameLabel = new Label();
    private final Path directoryProvider;

    public ImageThumbListCell(Path directoryProvider) {
        this.directoryProvider = directoryProvider;

        /// Configure thumbnail layout constraints once
        thumbView.setFitHeight(100);
        thumbView.setFitWidth(100);
        thumbView.setPreserveRatio(true);

        container.setAlignment(Pos.CENTER_LEFT);
        container.getChildren().addAll(thumbView, nameLabel);
    }

    @Override
    protected void updateItem(String filename, boolean empty) {
        super.updateItem(filename, empty);

        if (empty || filename == null || directoryProvider == null) {
            setGraphic(null);
            setText(null);
        } else {
            nameLabel.setText(filename);

            /// Resolve file location using the currently watched directory
            File imageFile = new File(directoryProvider.toString(), filename);

            try {
                /// Width=60, Height=60, preserveRatio=true, smooth=true,
                /// backgroundLoading=true
                Image thumbnail = new Image(imageFile.toURI().toString(), 60,
                        60, true, true,
                        true);
                thumbView.setImage(thumbnail);
                setGraphic(container);
            } catch (Exception e) {
                /// Fallback for broken or unreadable files
                thumbView.setImage(null);
                setGraphic(container);
            }
        }
    }
}
