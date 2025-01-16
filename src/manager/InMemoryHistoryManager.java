package manager;

import tasks.Task;

import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager{
    private ArrayList<Task> history;
    InMemoryHistoryManager() {
        history = new ArrayList<>();
    }
    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        } else if (history.size() < 10) {
            history.add(task);
        } else {
            history.remove(0);
            history.add(task);
        }

    }

    @Override
    public ArrayList<Task> getHistory() {
        return new ArrayList<>(history);
    }
}
