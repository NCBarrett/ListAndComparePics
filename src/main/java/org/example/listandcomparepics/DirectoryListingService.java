package org.example.listandcomparepics;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DirectoryListingService {

    public ObservableList<String> getDirectoryListing(Path dir) {
        ObservableList<String> list = FXCollections.observableArrayList();
        if (dir == null || !Files.isDirectory(dir)) {
            return list;
        }

        /// Try-with-resources safely closes the operating system stream
        try (Stream<Path> stream = Files.list(dir)) {
            list.addAll(stream
                    .map(path -> path.getFileName().toString())
                    .sorted()
                    .toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}
