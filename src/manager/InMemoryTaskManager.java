package manager;

import tasks.Epic;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {
    private Map<Integer, Task> tasks;
    private Map<Integer, Epic> epics;
    private Map<Integer, SubTask> subTasks;
    private HistoryManager historyManager;
    private int idCounter = 0;

    public InMemoryTaskManager(HistoryManager historyManager) {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subTasks = new HashMap<>();
        this.historyManager = historyManager;
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
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public Task getTaskById(Integer id) {
        Task task = tasks.get(id);
        if (task != null) {
            Task historyTask = new Task(task.getName(), task.getDescription(), task.getStatus());
            historyTask.setId(id);
            historyManager.add(historyTask);
        }
        return task;
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
    public List<SubTask> getAllSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    @Override
    public SubTask getSubTaskById(Integer id) {
        SubTask subTask = subTasks.get(id);
        if (subTask != null) {
            Task historyTask = new SubTask(subTask.getName(), subTask.getDescription(), subTask.getStatus(), subTask.getEpicId());
            historyTask.setId(id);
            historyManager.add(historyTask);
        }
        return subTask;
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
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public Epic getEpicById(Integer id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            Epic historyTask = new Epic(epic.getName(), epic.getDescription());
            historyTask.setId(id);
            historyTask.setSubTasksIds(epic.getSubTasksIds());
            historyManager.add(historyTask);
        }
        return epic;
    }

    @Override
    public void deleteAllEpics() {
        epics.clear();
        subTasks.clear();
    }

    @Override
    public List<SubTask> getAllSubTasksOfOneEpic(int id) {
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
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}