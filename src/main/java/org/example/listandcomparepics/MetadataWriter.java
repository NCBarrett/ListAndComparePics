package org.example.listandcomparepics;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MetadataWriter {

    private static final String EXIFTOOL_PATH = "exiftool";

    /**
     * Writes metadata tags directly to a local JPEG file for Windows Explorer & OneDrive.
     * Keeps the file extension intact while embedding metadata.
     */
    public boolean applyTagsToLocalFile(File imageFile, List<String> tags) {
        if (tags == null || tags.isEmpty() || !imageFile.exists()) {
            return false;
        }

        List<String> command = new ArrayList<>();
        command.add(EXIFTOOL_PATH);
        command.add("-overwrite_original");

        // Semi-colon separated string for native Windows Details Pane indexing
        String xpKeywordsString = String.join("; ", tags);
        command.add("-XPKeywords=" + xpKeywordsString);

        // Standard XMP Subject array entries for OneDrive cloud side search
        for (String tag : tags) {
            command.add("-Subject=" + tag);
        }

        command.add(imageFile.getAbsolutePath());

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Clear the buffer to prevent process hangs on local SSD
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                while (reader.readLine() != null) {
                    // Consuming stream
                }
            }

            return process.waitFor() == 0;

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
