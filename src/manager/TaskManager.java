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
    private Integer idCounter = 0;

    public TaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subTasks = new HashMap<>();

    }

    private Integer generateNewId() {
        return idCounter++;
    }

    public Task createTask(Task task) {
        int id = generateNewId();
        tasks.put(id, new Task(task, id));
        return tasks.get(id);
    }

    public Task updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            Task existingTask = tasks.get(task.getId());
            existingTask.setName(task.getName());
            existingTask.setDescription(task.getDescription());
            existingTask.setStatus(task.getStatus());
        }
        return task;

    }

    public Task deleteTaskById(Integer id) {;
        return tasks.remove(id);
    }

    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public Task getTaskById(Integer id) {
        return tasks.get(id);
    }

    public ArrayList<Task> deleteAllTasks() {
        ArrayList<Task> deletedTasks = new ArrayList<>(tasks.values());
        for (Integer id : tasks.keySet()) {
            tasks.remove(id);
        }
        return deletedTasks;
    }

    public SubTask createSubTask(SubTask subTask) {
        if (epics.containsKey(subTask.getEpic_ID())) {
            int id = generateNewId();
            subTasks.put(id, new SubTask(subTask, id));
            Epic epic = epics.get(subTask.getEpic_ID());
            epic.getSubTasksIds().add(id);
            updateEpic(epic);
            return subTasks.get(id);
        } else {
            return null;
        }
    }

    public SubTask updateSubTask(SubTask subTask) {
        if (subTasks.containsKey(subTask.getId())) {
            SubTask existingSubTask = subTasks.get(subTask.getId());
            existingSubTask.setName(subTask.getName());
            existingSubTask.setDescription(subTask.getDescription());
            existingSubTask.setStatus(subTask.getStatus());
            updateEpic(epics.get(subTask.getEpic_ID()));
            return existingSubTask;
        }
        return null;
    }

    public SubTask deleteSubTaskById (Integer id) {
        Epic epic = epics.get(subTasks.get(id).getEpic_ID());
        epic.getSubTasksIds().remove(id);
        updateEpic(epic);
        return subTasks.remove(id);
    }

    public ArrayList<SubTask> getAllSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    public SubTask getSubTaskById(Integer id) {
        return subTasks.get(id);
    }

    public ArrayList<SubTask> deleteAllSubTasks() {
        ArrayList<SubTask> deletedSubTasks = new ArrayList<>(subTasks.values());
        for (Integer id : subTasks.keySet()) {
            Epic epic = epics.get(subTasks.get(id).getEpic_ID());
            epic.getSubTasksIds().remove(id);
            updateEpic(epic);
            subTasks.remove(id);
        }
        return deletedSubTasks;
    }

    public Epic createEpic(Epic epic) {
        int id = generateNewId();
        epics.put(id, new Epic(epic, id));
        return epics.get(id);
    }
    public Epic updateEpic (Epic epic) {
        if (epics.containsKey(epic.getId())) {
            Epic existingEpic = epics.get(epic.getId());
            existingEpic.setName(epic.getName());
            existingEpic.setDescription(epic.getDescription());
            if (epic.getSubTasksIds().isEmpty()) {
                epic.setStatus(Status.NEW);
                return epic;
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
                    return epic;
                }
                if (hasNew && hasDone) {
                    epic.setStatus(Status.IN_PROGRESS);
                    return epic;
                }
            }
            if (hasNew) {
                epic.setStatus(Status.NEW);
            } else if (hasDone) {
                epic.setStatus(Status.DONE);
            }
            return epic;
        } else return null;
    }

    public Epic deleteEpicById(Integer id) {
        Epic epic = epics.get(id);
        for (Integer idOfSubTask : epic.getSubTasksIds()) {
            subTasks.remove(idOfSubTask);
        }
        return epics.remove(id);
    }

    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public Epic getEpicById(Integer id) {
        return epics.get(id);
    }

    public ArrayList<Epic> deleteAllEpics() {
        ArrayList<Epic> deletedEpics = new ArrayList<>(epics.values());
        for (Integer id : epics.keySet()) {
            epics.remove(id);
        }
        for (Integer id : subTasks.keySet()) {
            subTasks.remove(id);
        }
        return deletedEpics;
    }

    public ArrayList<SubTask> getAllSubTasksOfOneEpic(Epic epic) {
        ArrayList<SubTask> listOfSubTasks = new ArrayList<>();
        for (Integer id : epic.getSubTasksIds()) {
            listOfSubTasks.add(subTasks.get(id));
        }
        return listOfSubTasks;
    }
}