package manager;

import tasks.Epic;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
public class TaskManager {
    private HashMap<Integer, Task> tasks;
    private HashMap<Integer, Epic> epics;
    private HashMap<Integer, SubTask> subTasks;
    private int idCounter = 0;

    public TaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subTasks = new HashMap<>();

    }

    private int generateNewId() {
        return idCounter++;
    }

    public Task createTask(Task task) {
        int id = generateNewId();
        task.setId(id);
        tasks.put(id, task);
        return tasks.get(id);
    }

    public Task updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
            return task;
        }
        return null;


    }

    public Task deleteTaskById(Integer id) {
        return tasks.remove(id);
    }

    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public Task getTaskById(Integer id) {
        return tasks.get(id);
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public SubTask createSubTask(SubTask subTask) {
        if (epics.containsKey(subTask.getEpicId())) {
            int id = generateNewId();
            subTask.setId(id);
            subTasks.put(id, subTask);
            Epic epic = epics.get(subTask.getEpicId());
            epic.addSubTaskById(id);
            updateEpicStatus(epic);
            return subTasks.get(id);
        } else {
            return null;
        }
    }

    public SubTask updateSubTask(SubTask subTask) {
        if (subTasks.containsKey(subTask.getId())) {
            subTasks.put(subTask.getId(), subTask);
            updateEpicStatus(epics.get(subTask.getEpicId()));
            return subTask;
        }
        return null;
    }

    public SubTask deleteSubTaskById (Integer id) {
        SubTask subTask = subTasks.remove(id);
        Epic epic = epics.get(subTask.getEpicId());
        epic.deleteSubTaskById(id);
        updateEpic(epic);
        return subTask;
    }

    public ArrayList<SubTask> getAllSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    public SubTask getSubTaskById(Integer id) {
        return subTasks.get(id);
    }

    public void deleteAllSubTasks() {
        subTasks.clear();
        for (Epic epic : epics.values()) {
            epic.deleteAllSubTasks();
            updateEpicStatus(epic);
        }
    }

    public Epic createEpic(Epic epic) {
        int id = generateNewId();
        epic.setId(id);
        epics.put(id, epic);
        return epics.get(id);
    }
    public Epic updateEpic (Epic epic) {
        if (epics.containsKey(epic.getId())) {
            Epic existingEpic = epics.get(epic.getId());
            existingEpic.setName(epic.getName());
            existingEpic.setDescription(epic.getDescription());
            return epic;
        } else return null;
    }

    public Epic deleteEpicById(Integer id) {
        Epic epic = epics.remove(id);
        for (Integer idOfSubTask : epic.getSubTasksIds()) {
            subTasks.remove(idOfSubTask);
        }
        return epic;
    }

    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public Epic getEpicById(Integer id) {
        return epics.get(id);
    }

    public void deleteAllEpics() {
        epics.clear();
        subTasks.clear();
    }

    public ArrayList<SubTask> getAllSubTasksOfOneEpic(Epic epic) {
        ArrayList<SubTask> listOfSubTasks = new ArrayList<>();
        for (Integer id : epic.getSubTasksIds()) {
            listOfSubTasks.add(subTasks.get(id));
        }
        return listOfSubTasks;
    }

    private void updateEpicStatus(Epic epic) {
        if (epic.getSubTasksIds().isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }
        boolean hasNew = false;
        boolean hasDone = false;
        for (Integer idOfSubTusk : epic.getSubTasksIds()) {
            Status statusOfSubTusk = subTasks.get(idOfSubTusk).getStatus();
            if (statusOfSubTusk == Status.NEW) {
                hasNew = true;
            } else if (statusOfSubTusk == Status.DONE) {
                hasDone = true;
            } else {
                epic.setStatus(Status.IN_PROGRESS);
                return;
            }
            if (hasNew && hasDone) {
                epic.setStatus(Status.IN_PROGRESS);
                return;
            }
        }
        if (hasNew) {
            epic.setStatus(Status.NEW);
        } else if (hasDone) {
            epic.setStatus(Status.DONE);
        }
    }
}