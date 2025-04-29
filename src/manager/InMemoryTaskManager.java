package manager;

import exceptions.TaskOverlapException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import tasks.Epic;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;

public class InMemoryTaskManager implements TaskManager {

    private static final DateTimeFormatter DATE_AND_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm, dd.MM.yyyy");
    protected Map<Integer, Task> tasks;
    protected Map<Integer, Epic> epics;
    protected Map<Integer, SubTask> subTasks;
    protected SortedSet<Task> treeSetByTime;
    protected int idCounter;
    private HistoryManager historyManager;

    public InMemoryTaskManager(HistoryManager historyManager) {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subTasks = new HashMap<>();
        this.historyManager = historyManager;
        idCounter = 0;
        treeSetByTime = new TreeSet<>((Comparator.comparing(Task::getStartTime)));
    }

    protected InMemoryTaskManager(Map<Integer, Task> tasks, Map<Integer, Epic> epics, Map<Integer, SubTask> subTasks,
            int idCounter, HistoryManager historyManager, SortedSet<Task> treeSetByTime) {
        this.tasks = tasks;
        this.epics = epics;
        this.subTasks = subTasks;
        this.idCounter = idCounter;
        this.historyManager = historyManager;
        this.treeSetByTime = treeSetByTime;
    }

    protected static boolean timeIsSet(Task task) {
        return (!((task.getStartTime() == null) && (task.getDuration() == null)));
    }

    private int generateNewId() {
        return idCounter++;
    }

    @Override
    public Task createTask(Task task) {
        int id = generateNewId();
        task.setId(id);
        Task taskToAdd = new Task(task);
        if (timeIsSet(taskToAdd)) {
            try {
            addToOrUpdateTreeSetByTime(taskToAdd);
            } catch (TaskOverlapException exception) {
                System.out.println(exception.getMessage());
                return null;
            }
        }
        tasks.put(id, taskToAdd);
        return task;
    }

    @Override
    public Task updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            Task taskToAdd = new Task(task);
            if (timeIsSet(taskToAdd)) {
                try {
                    addToOrUpdateTreeSetByTime(taskToAdd);
                } catch (TaskOverlapException exception) {
                    System.out.println(exception.getMessage());
                    return null;
                }
            }
            tasks.put(task.getId(), taskToAdd);
            return task;
        }
        return null;
    }

    @Override
    public Task deleteTaskById(Integer id) {
        historyManager.remove(id);
        if (timeIsSet(tasks.get(id))) {
            deleteFromTreeSetByTime(tasks.get(id));
        }
        return tasks.remove(id);
    }

    @Override
    public List<Task> getAllTasks() {
        return tasks.values().stream().map(Task::new).collect(Collectors.toCollection(ArrayList::new));
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
        tasks.values().stream().peek(task -> historyManager.remove(task.getId())).filter(InMemoryTaskManager::timeIsSet)
                .forEach(this::deleteFromTreeSetByTime);

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
            if (timeIsSet(subTaskToAdd)) {
                try {
                    addToOrUpdateTreeSetByTime(subTaskToAdd);
                } catch (TaskOverlapException exception) {
                    System.out.println(exception.getMessage());
                    return null;
                }
                updateEpicTime(epic);
            }
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
            Epic epic = epics.get(subTaskToAdd.getEpicId());
            updateEpicStatus(epic);
            if (timeIsSet(subTaskToAdd)) {
                try {
                    addToOrUpdateTreeSetByTime(subTaskToAdd);
                } catch (TaskOverlapException exception) {
                    System.out.println(exception.getMessage());
                    return null;
                }
                updateEpicTime(epic);
            }
            return subTask;
        }
        return null;
    }

    @Override
    public SubTask deleteSubTaskById(int id) {
        if (subTasks.containsKey(id)) {
            SubTask subTask = subTasks.remove(id);
            historyManager.remove(id);
            Epic epic = epics.get(subTask.getEpicId());
            epic.deleteSubTaskById(id);
            updateEpic(epic);
            if (timeIsSet(subTask)) {
                deleteFromTreeSetByTime(subTask);
                updateEpicTime(epic);
            }
            return subTask;
        } else {
            return null;
        }
    }

    @Override
    public List<SubTask> getAllSubTasks() {
        return subTasks.values().stream().map(SubTask::new).collect(Collectors.toCollection(ArrayList::new));
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
        subTasks.values().stream().peek(subTask -> historyManager.remove(subTask.getId()))
                .filter(InMemoryTaskManager::timeIsSet)
                .forEach(this::deleteFromTreeSetByTime);
        subTasks.clear();
        epics.values().stream().peek(epic -> {
            epic.deleteAllSubTasks();
            updateEpicStatus(epic);
        }).filter(InMemoryTaskManager::timeIsSet).forEach(this::updateEpicTime);
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
        } else {
            return null;
        }
    }

    @Override
    public Epic deleteEpicById(Integer id) {
        Epic epic = epics.remove(id);
        historyManager.remove(epic.getId());
        epic.getSubTasksIds().stream().peek(idOfSubTask -> historyManager.remove(idOfSubTask))
                .map(idOfSubTask -> subTasks.remove(idOfSubTask)).filter(InMemoryTaskManager::timeIsSet)
                .forEach(this::deleteFromTreeSetByTime);
        return epic;
    }

    @Override
    public List<Epic> getAllEpics() {
        return epics.values().stream().map(Epic::new).collect(Collectors.toCollection(ArrayList::new));
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
        epics.keySet().stream().forEach(historyManager::remove);
        subTasks.values().stream().peek(subTask -> historyManager.remove(subTask.getId()))
                .filter(InMemoryTaskManager::timeIsSet)
                .forEach(this::deleteFromTreeSetByTime);
        epics.clear();
        subTasks.clear();
    }

    @Override
    public List<SubTask> getAllSubTasksOfOneEpic(int id) {
        return epics.get(id).getSubTasksIds().stream().map(subTaskId -> subTasks.get(subTaskId))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(treeSetByTime);
    }

    protected void updateEpicStatus(Epic epic) {
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

    protected void updateEpicTime(Epic epic) {
        ArrayList<Integer> subTasksIds = epic.getSubTasksIds();
        if (subTasksIds.isEmpty()) {
            epic.setStartTime(null);
            epic.setDuration(null);
            epic.setEndTime(null);
        } else {
            record EpicTime(Instant startTime, Instant endTime, Duration duration) {

            }

            EpicTime epicTime = subTasksIds.stream().map(id -> subTasks.get(id))
                    .collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
                        Instant startTime = list.stream().map(Task::getStartTime).filter(Objects::nonNull)
                                .min(Instant::compareTo).orElse(null);
                        Instant endTime = list.stream().map(Task::getEndTime).filter(Objects::nonNull)
                                .max(Instant::compareTo).orElse(null);
                        Duration duration = list.stream().map(Task::getDuration).filter(Objects::nonNull)
                                .reduce(Duration::plus).orElse(null);

                        return new EpicTime(startTime, endTime, duration);
                    }));
            epic.setStartTime(epicTime.startTime);
            epic.setDuration(epicTime.duration);
            epic.setEndTime(epicTime.endTime);
        }
    }

    protected void addToOrUpdateTreeSetByTime(Task task) throws TaskOverlapException {
        if (tasks.containsKey(task.getId())) {
            deleteFromTreeSetByTime(tasks.get(task.getId()));
        }
        List<Task> tasksWithIntersection = getPrioritizedTasks().stream()
                .filter(taskFromTree -> hasIntersections(taskFromTree, task)).toList();
        if (!tasksWithIntersection.isEmpty()) {
            String message = buildTaskOverlapMessage(task, tasksWithIntersection);
            throw new TaskOverlapException(message);
        } else {
            treeSetByTime.add(task);
        }
    }

    private String buildTaskOverlapMessage(Task task, List<Task> tasksWithIntersection) {
        LocalDateTime taskDateTime = LocalDateTime.ofInstant(task.getStartTime(), ZoneOffset.UTC);
        long taskDuration = task.getDuration().toMinutes();
        StringBuilder message = new StringBuilder(String.format(
                "Указанная задача: \"%s\" с \n" + "датой начала: %s\n" + "продолжительностью в минутах: %d\n",
                task.getName(), taskDateTime.format(DATE_AND_TIME_FORMATTER), taskDuration));
        if (tasksWithIntersection.size() == 1) {
            message.append("пересекается с одной из уже добавленных раннее задач:\n");
        } else {
            message.append("пересекается с несколькими из уже добавленных раннее задач:\n");
        }
        for (Task taskWithIntersection : tasksWithIntersection) {
            LocalDateTime taskWithIntersectionDateTime = LocalDateTime.ofInstant(
                    taskWithIntersection.getStartTime(), ZoneOffset.UTC);
            long taskWithIntersectionDuration = taskWithIntersection.getDuration().toMinutes();
            String taskWithIntersectionName = taskWithIntersection.getName();
            message.append(String.format("\"%s\"\n" + "дата начала: %s\n" + "продолжительность в минутах: %d\n",
                    taskWithIntersectionName, taskWithIntersectionDateTime.format(DATE_AND_TIME_FORMATTER),
                    taskWithIntersectionDuration));
        }
        message.append("Даннай задача не будет добавлена в менеджер задач.");
        return message.toString();
    }

    protected void deleteFromTreeSetByTime(Task task) {
        treeSetByTime.remove(task);
    }

    private boolean hasIntersections(Task task1, Task task2) {
        long startFirst = task1.getStartTime().toEpochMilli();
        long endFirst = task1.getEndTime().toEpochMilli();
        long startSecond = task2.getStartTime().toEpochMilli();
        long endSecond = task2.getEndTime().toEpochMilli();
        boolean oneInsideOther = (startFirst <= startSecond && endFirst >= endSecond) || (startSecond <= startFirst
                && endSecond >= endFirst);
        boolean intersection = (endFirst > startSecond && endFirst < endSecond) || (startFirst > startSecond
                && startFirst < endSecond);
        return (oneInsideOther || intersection);
    }
}