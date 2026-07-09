package org.example.listandcomparepics;

import java.io.IOException;
import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;

import javafx.application.Platform;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DirectoryWatcherService {

    private WatchService watchService;
    private ExecutorService executorService;
    private Runnable onDirectoryChanged;

    public void startWatching(Path dir, Runnable onDirectoryChanged)
            throws IOException {
        stopWatching(); /// Ensures previous watchers are stopped

        this.onDirectoryChanged = onDirectoryChanged;
        this.watchService = FileSystems.getDefault().newWatchService();
        dir.register(watchService,
                ENTRY_CREATE,
                ENTRY_DELETE,
                ENTRY_MODIFY
                );

        this.executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            try {
                while (true) {
                    /// block until even occurs
                    WatchKey key = watchService.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.kind() == OVERFLOW) {
                            continue;
                        }
                        /// Trigger the callback on the FX Application thread
                        Platform.runLater(this.onDirectoryChanged);
                    }
                    if (!key.reset()) {
                        break;
                    }
                }
            } catch (InterruptedException |
                        ClosedWatchServiceException ignored) {
                /// Thread interrupted or watcher closed
            }
        });
    }

    public void stopWatching() {
        if (executorService != null) {
            executorService.shutdownNow();
        }
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}