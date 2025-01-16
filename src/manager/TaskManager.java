package manager;

import tasks.Epic;
import tasks.SubTask;
import tasks.Task;

import java.util.ArrayList;

public interface TaskManager {
    Task createTask(Task task);

    Task updateTask(Task task);

    Task deleteTaskById(Integer id);

    ArrayList<Task> getAllTasks();

    Task getTaskById(Integer id);

    void deleteAllTasks();

    SubTask createSubTask(SubTask subTask);

    SubTask updateSubTask(SubTask subTask);

    SubTask deleteSubTaskById(int id);

    ArrayList<SubTask> getAllSubTasks();

    SubTask getSubTaskById(Integer id);

    void deleteAllSubTasks();

    Epic createEpic(Epic epic);

    Epic updateEpic(Epic epic);

    Epic deleteEpicById(Integer id);

    ArrayList<Epic> getAllEpics();

    Epic getEpicById(Integer id);

    void deleteAllEpics();

    ArrayList<SubTask> getAllSubTasksOfOneEpic(int id);
}
