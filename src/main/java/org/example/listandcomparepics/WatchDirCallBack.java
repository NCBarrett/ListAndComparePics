package org.example.listandcomparepics;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

public interface WatchDirCallBack {
    void onFileEvent(WatchEvent.Kind<?> kind, Path filePath);
}
