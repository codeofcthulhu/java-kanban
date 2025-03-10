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
    protected static Map<Integer, Task> tasks;
    protected static Map<Integer, Epic> epics;
    protected static Map<Integer, SubTask> subTasks;
    protected static int idCounter;
    private HistoryManager historyManager;

    public InMemoryTaskManager(HistoryManager historyManager) {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subTasks = new HashMap<>();
        this.historyManager = historyManager;
        idCounter = 0;
    }

    private int generateNewId() {
        return idCounter++;
    }

    @Override
    public Task createTask(Task task) {
        int id = generateNewId();
        task.setId(id);
        Task taskToAdd = new Task(task);
        tasks.put(id, taskToAdd);
        return task;
    }

    @Override
    public Task updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            Task taskToAdd = new Task(task);
            tasks.put(task.getId(), taskToAdd);
            return task;
        }
        return null;
    }

    @Override
    public Task deleteTaskById(Integer id) {
        historyManager.remove(id);
        return tasks.remove(id);
    }

    @Override
    public List<Task> getAllTasks() {
        List<Task> tasksList = new ArrayList<>(tasks.values());
        List<Task> tasksListToReturn = new ArrayList<>(tasksList.size());
        for (Task task : tasksList) {
            Task taskToReturn = new Task(task);
            tasksListToReturn.add(taskToReturn);
        }
        return tasksListToReturn;
    }

    @Override
    public Task getTaskById(Integer id) {
        Task task = tasks.get(id);
        if (task != null) {
            Task historyTask = new Task(task);
            historyManager.add(historyTask);
            Task taskToReturn = new Task(task);
            return taskToReturn;
        }

        return null;
    }

    @Override
    public void deleteAllTasks() {
        for (Task task : tasks.values()) {
            historyManager.remove(task.getId());
        }
        tasks.clear();
    }

    @Override
    public SubTask createSubTask(SubTask subTask) {
        if (epics.containsKey(subTask.getEpicId())) {
            int id = generateNewId();
            subTask.setId(id);
            SubTask subTaskToAdd = new SubTask(subTask);
            subTasks.put(id, subTaskToAdd);
            Epic epic = epics.get(subTaskToAdd.getEpicId());
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
            SubTask subTaskToAdd = new SubTask(subTask);
            subTasks.put(subTaskToAdd.getId(), subTaskToAdd);
            updateEpicStatus(epics.get(subTaskToAdd.getEpicId()));
            return subTask;
        }
        return null;
    }

    @Override
    public SubTask deleteSubTaskById(int id) {
        SubTask subTask = subTasks.remove(id);
        historyManager.remove(id);
        Epic epic = epics.get(subTask.getEpicId());
        epic.deleteSubTaskById(id);
        updateEpic(epic);
        return subTask;
    }

    @Override
    public List<SubTask> getAllSubTasks() {
        List<SubTask> subTasksList = new ArrayList<>(subTasks.values());
        List<SubTask> subTasksListToReturn = new ArrayList<>(subTasksList.size());
        for (SubTask subTask : subTasksList) {
            SubTask subTaskToReturn = new SubTask(subTask);
            subTasksListToReturn.add(subTaskToReturn);
        }
        return subTasksListToReturn;
    }

    @Override
    public SubTask getSubTaskById(Integer id) {
        SubTask subTask = subTasks.get(id);
        if (subTask != null) {
            Task historyTask = new SubTask(subTask);
            historyManager.add(historyTask);
            SubTask subTaskToReturn = new SubTask(subTask);
            return subTaskToReturn;
        }
        return null;
    }

    @Override
    public void deleteAllSubTasks() {
        for (SubTask subTask : subTasks.values()) {
            historyManager.remove(subTask.getId());
        }
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
        Epic epicToAdd = new Epic(epic);
        epics.put(id, epicToAdd);
        return epic;
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
        historyManager.remove(epic.getId());
        for (Integer idOfSubTask : epic.getSubTasksIds()) {
            subTasks.remove(idOfSubTask);
            historyManager.remove(idOfSubTask);
        }
        return epic;
    }

    @Override
    public List<Epic> getAllEpics() {
        List<Epic> epicsList = new ArrayList<>(epics.values());
        List<Epic> epicsListToReturn = new ArrayList<>(epicsList.size());
        for (Epic epic : epicsList) {
            Epic epicToReturn = new Epic(epic);
            epicsListToReturn.add(epicToReturn);
        }
        return epicsListToReturn;
    }

    @Override
    public Epic getEpicById(Integer id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            Epic historyTask = new Epic(epic);
            historyManager.add(historyTask);
            Epic epicToReturn = new Epic(epic);
            return epicToReturn;
        }
        return null;
    }

    @Override
    public void deleteAllEpics() {
        for (Epic epic : epics.values()) {
            historyManager.remove(epic.getId());
        }
        for (SubTask subTask : subTasks.values()) {
            historyManager.remove(subTask.getId());
        }
        epics.clear();
        subTasks.clear();
    }

    @Override
    public List<SubTask> getAllSubTasksOfOneEpic(int id) {
        List<SubTask> listOfSubTasks = new ArrayList<>();
        Epic epic = epics.get(id);
        for (Integer idOfSubTask : epic.getSubTasksIds()) {
            listOfSubTasks.add(new SubTask(subTasks.get(idOfSubTask)));
        }
        return listOfSubTasks;
    }

    protected static void updateEpicStatus(Epic epic) {
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