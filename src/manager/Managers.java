package manager;

import java.nio.file.Path;

public class Managers {
    private Managers() {
    }

    public static TaskManager getFileBackedTaskManager(Path data) {
        return new FileBackedTaskManager(getDefaultHistory(), data);
    }

    public static TaskManager getDefault() {
        return new InMemoryTaskManager(getDefaultHistory());
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
