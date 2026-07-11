package org.example.listandcomparepics;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DirectoryListingService {

    public ObservableList<String> getDirectoryListing(Path dir, String regex) {
        ObservableList<String> list = FXCollections.observableArrayList();
        if (dir == null || !Files.isDirectory(dir)) {
            return list;
        }

        // Add RegEx filter here
        Pattern pattern = null;
        if (regex != null && !regex.isBlank()) {
            try {
                pattern = Pattern.compile(regex);
            } catch (PatternSyntaxException e) {
                System.err.println("Invalid regex, showing unfiltered list: " +
                        e.getMessage());
            }
        }
        final Pattern finalPattern = pattern;

        /// Try-with-resources safely closes the operating system stream
        try (Stream<Path> stream = Files.list(dir)) {
            list.addAll(stream
                    .map(path -> path.getFileName().toString())
                    .filter(name -> finalPattern == null ||
                            finalPattern.matcher(name).find())
                    .sorted()
                    .toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}
