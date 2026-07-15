package org.example.listandcomparepics;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DirectoryListingService {

    public ObservableList<String> getDirectoryListing(Path dir, String regex) {
        return getDirectoryListing(dir, regex, false);
    }

    public ObservableList<String> getDirectoryListing(Path dir, String regex,
                                                      boolean naturalSort) {
        ObservableList<String> list = FXCollections.observableArrayList();
        if (dir == null || !Files.isDirectory(dir)) {
            return list;
        }

        Pattern pattern = null;
        if (regex != null && !regex.isBlank()) {
            try {
                pattern = Pattern.compile(regex);
            } catch (PatternSyntaxException e) {
                System.err.println("Invalid regex, showing unfiltered list: " + e.getMessage());
            }
        }
        final Pattern finalPattern = pattern;

        try (Stream<Path> stream = Files.list(dir)) {
            List<String> names = stream
                    .filter(p -> !Files.isDirectory(p))   // files only, no folders
                    .map(p -> p.getFileName().toString())
                    .filter(name -> finalPattern == null || finalPattern.matcher(name).find())
                    .collect(Collectors.toList());

            Comparator<String> comparator;
            if (naturalSort) {
                comparator = (a, b) -> naturalCompare(a, b);
            } else {
                comparator = (a, b) -> a.compareTo(b);
            }
            names.sort(comparator);
//            names.sort(naturalSort ? DirectoryListingService::naturalCompare : String::compareTo);
            list.addAll(names);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    /// 'Human' sorting -- save for later
    private static int naturalCompare(String a, String b) {
        int i = 0, j = 0;
        while (i < a.length() && j < b.length()) {
            char ca = a.charAt(i), cb = b.charAt(j);
            if (Character.isDigit(ca) && Character.isDigit(cb)) {
                int startI = i, startJ = j;
                while (i < a.length() && Character.isDigit(a.charAt(i))) i++;
                while (j < b.length() && Character.isDigit(b.charAt(j))) j++;
                int cmp = new java.math.BigInteger(a.substring(startI, i))
                        .compareTo(new java.math.BigInteger(b.substring(startJ, j)));
                if (cmp != 0) return cmp;
            } else {
                if (ca != cb) return Character.compare(ca, cb);
                i++; j++;
            }
        }
        return (a.length() - i) - (b.length() - j);
    }
}
