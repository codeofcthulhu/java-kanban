package manager;

import tasks.Task;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager{
    private List<Task> history;
    private static final int HISTORY_SIZE = 10;

    public InMemoryHistoryManager() {
        history = new LinkedList<>();
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        } else if (history.size() < HISTORY_SIZE) {
            history.add(task);
        } else {
            history.removeFirst();
            history.add(task);
        }

    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }
}
