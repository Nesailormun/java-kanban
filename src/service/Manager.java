package service;

import java.nio.file.Path;

public class Manager {

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static TaskManager getDefaultFileBackedTaskManager(Path path) {
        return new FileBackedTaskManager(path);
    }
}
