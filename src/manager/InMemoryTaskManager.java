package manager;

import tasks.Epic;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
public class InMemoryTaskManager implements TaskManager {
    private HashMap<Integer, Task> tasks;
    private HashMap<Integer, Epic> epics;
    private HashMap<Integer, SubTask> subTasks;
    private int idCounter = 0;

    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subTasks = new HashMap<>();

    }

    private int generateNewId() {
        return idCounter++;
    }

    @Override
    public Task createTask(Task task) {
        int id = generateNewId();
        task.setId(id);
        tasks.put(id, task);
        return tasks.get(id);
    }

    @Override
    public Task updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
            return task;
        }
        return null;


    }

    @Override
    public Task deleteTaskById(Integer id) {
        return tasks.remove(id);
    }

    @Override
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public Task getTaskById(Integer id) {
        return tasks.get(id);
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    @Override
    public SubTask createSubTask(SubTask subTask) {
        if (epics.containsKey(subTask.getEpicId())) {
            int id = generateNewId();
            subTask.setId(id);
            subTasks.put(id, subTask);
            Epic epic = epics.get(subTask.getEpicId());
            epic.addSubTaskById(id);
            updateEpicStatus(epic);
            return subTask;
        } else {
            return null;
        }
    }

    @Override
    public SubTask updateSubTask(SubTask subTask) {
        if (subTasks.containsKey(subTask.getId())) {
            subTasks.put(subTask.getId(), subTask);
            updateEpicStatus(epics.get(subTask.getEpicId()));
            return subTask;
        }
        return null;
    }

    @Override
    public SubTask deleteSubTaskById(int id) {
        SubTask subTask = subTasks.remove(id);
        Epic epic = epics.get(subTask.getEpicId());
        epic.deleteSubTaskById(id);
        updateEpic(epic);
        return subTask;
    }

    @Override
    public ArrayList<SubTask> getAllSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    @Override
    public SubTask getSubTaskById(Integer id) {
        return subTasks.get(id);
    }

    @Override
    public void deleteAllSubTasks() {
        subTasks.clear();
        for (Epic epic : epics.values()) {
            epic.deleteAllSubTasks();
            updateEpicStatus(epic);
        }
    }

    @Override
    public Epic createEpic(Epic epic) {
        int id = generateNewId();
        epic.setId(id);
        epics.put(id, epic);
        return epics.get(id);
    }
    @Override
    public Epic updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            Epic existingEpic = epics.get(epic.getId());
            existingEpic.setName(epic.getName());
            existingEpic.setDescription(epic.getDescription());
            return epic;
        } else return null;
    }

    @Override
    public Epic deleteEpicById(Integer id) {
        Epic epic = epics.remove(id);
        for (Integer idOfSubTask : epic.getSubTasksIds()) {
            subTasks.remove(idOfSubTask);
        }
        return epic;
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public Epic getEpicById(Integer id) {
        return epics.get(id);
    }

    @Override
    public void deleteAllEpics() {
        epics.clear();
        subTasks.clear();
    }

    @Override
    public ArrayList<SubTask> getAllSubTasksOfOneEpic(int id) {
        ArrayList<SubTask> listOfSubTasks = new ArrayList<>();
        Epic epic = epics.get(id);
        for (Integer idOfSubTask : epic.getSubTasksIds()) {
            listOfSubTasks.add(subTasks.get(idOfSubTask));
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