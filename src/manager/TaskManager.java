package manager;

import tasks.Epic;
import tasks.SubTask;
import tasks.Task;
import java.util.List;

public interface TaskManager {
    Task createTask(Task task);

    Task updateTask(Task task);

    Task deleteTaskById(Integer id);

    List<Task> getAllTasks();

    Task getTaskById(Integer id);

    void deleteAllTasks();

    SubTask createSubTask(SubTask subTask);

    SubTask updateSubTask(SubTask subTask);

    SubTask deleteSubTaskById(int id);

    List<SubTask> getAllSubTasks();

    SubTask getSubTaskById(Integer id);

    void deleteAllSubTasks();

    Epic createEpic(Epic epic);

    Epic updateEpic(Epic epic);

    Epic deleteEpicById(Integer id);

    List<Epic> getAllEpics();

    Epic getEpicById(Integer id);

    void deleteAllEpics();

    List<SubTask> getAllSubTasksOfOneEpic(int id);

    List<Task> getHistory();
}
