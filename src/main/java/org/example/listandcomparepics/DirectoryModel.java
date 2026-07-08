package org.example.listandcomparepics;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.StandardWatchEventKinds;
import static java.nio.file.StandardWatchEventKinds.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;

public class DirectoryModel {
//    private final ObservableList<File> fileList;
//    private Thread thread;
    private WatchService watcherService;
    private Thread watchThread;
    private boolean isRunning = false;
    private final WatchDirCallBack callback;

    public DirectoryModel(WatchDirCallBack callback) {
        this.callback = callback;
    }

    public void startWatching(Path dirPath) throws IOException {
        stopWatching(); //Ensures previous watchers are stopped

        watcherService = FileSystems.getDefault().newWatchService();
        dirPath.register(watcherService, ENTRY_CREATE, ENTRY_DELETE,
                ENTRY_MODIFY);

        isRunning = true;

        watchThread = new Thread(() -> {
            while (isRunning) {
                WatchKey key;
                try {
                    //Blocks until an event occurs
                    key = watcherService.take();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == OVERFLOW) {
                        continue;
                    }

                    // Intellij flags because of a casting problem
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path filePath = ev.context();
                    Path fullPath = dirPath.resolve(filePath);

                    // Pass events back to the UI via callback
                    callback.onFileEvent(kind, fullPath);
                }

                boolean valid = key.reset();
                if (!valid) {
                    break; // Directory is no longer accessible
                }
            }
        });
        watchThread.setDaemon(true);
        watchThread.start();
    }

    public void stopWatching() throws IOException {
        isRunning = false;
        if (watchThread != null) {
            watchThread.interrupt();
        }
        if (watcherService != null) {
            watcherService.close();
        }
    }
}

//        if (isRunning) {
//        return;
//        }
//isRunning = true;

//    Path dirToWatch;
//    WatchService ws = FileSystems.getDefault().newWatchService();
//
//    public DirectoryModel(File dirPath) throws IOException {
//        this.fileList = FXCollections.observableArrayList();
//        dirToWatch = Paths.get(dirPath.getAbsolutePath());
//    }
//
//    public ObservableList<File> getFileList() {
//        return fileList;
//    }
//
//    public void stopWatching() throws IOException {
//        isRunning = false;
//

//public void loadDirectory(File directory) {
//    // Stop any currently running watcher
//    if (watchService != null) {
//        try {
//            watchService.close();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    fileList.clear();
//    fileList.addAll(directory.listFiles());
//
//    // Start watching the newly selected directory
//    watchService = new WatchService() {
//        @Override
//        public void close() throws IOException {
//
//        }
//
//        @Override
//        public WatchKey poll() {
//            return null;
//        }
//
//        @Override
//        public WatchKey poll(long timeout, TimeUnit unit) throws InterruptedException {
//            return null;
//        }
//
//        @Override
//        public WatchKey take() throws InterruptedException {
//            return null;
//        }
//    };
//}