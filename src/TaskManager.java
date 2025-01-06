import java.util.HashMap;
public class TaskManager {
    HashMap<Integer, Task> tasks;
    HashMap<Integer, Epic> epics;
    HashMap<Integer, SubTask> subTasks;

    public TaskManager(HashMap<Integer, Task> tasks, HashMap<Integer, Epic> epics, HashMap<Integer, SubTask> subTasks) {
        this.tasks = tasks;
        this.epics = epics;
        this.subTasks = subTasks;
    }

    public TaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subTasks = new HashMap<>();

    }
}
