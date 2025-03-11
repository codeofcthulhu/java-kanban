package manager;

import exceptions.ManagerFileInitializationException;
import exceptions.ManagerSaveException;
import tasks.Epic;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private Path data;
    private static final String DATA_HEAD = "id,type,name,status,description,epic\n";

    public FileBackedTaskManager(HistoryManager historyManager, Path data) {
        super(historyManager);
        this.data = data;
    }

    public FileBackedTaskManager(Map<Integer, Task> tasks, Map<Integer, Epic> epics, Map<Integer, SubTask> subTasks, int idCounter, HistoryManager historyManager, Path data) {
        super(tasks, epics, subTasks, idCounter, historyManager);
        this.data = data;
    }

    public static void main(String[] args) throws IOException {
        Path tempFile = Files.createTempFile("data", ".csv");
        System.out.println("Временный файл создан: " + tempFile);
        TaskManager taskManager = Managers.getFileBackedTaskManager(tempFile);

        Task task0 = new Task("Заголовок первого таска", "Описание первого таска", Status.NEW);
        Task task1 = new Task("Заголовок второго таска", "Описание второго таска", Status.NEW);
        Epic epic0 = new Epic("Заголовок первого эпика", "Описание первого эпика");
        Epic epic1 = new Epic("Заголовок второго эпика", "Описание второго эпика");
        SubTask subTask0 = new SubTask("Заголовок первого сабтаска", "Описание первого сабтаска", Status.IN_PROGRESS, 2);
        SubTask subTask1 = new SubTask("Заголовок второго сабтаска", "Описание второго сабтаска", Status.NEW, 3);
        Task createdTask0 = taskManager.createTask(task0);
        Task createdTask1 = taskManager.createTask(task1);
        taskManager.createEpic(epic0);
        taskManager.createEpic(epic1);
        SubTask createdSubTask4 = taskManager.createSubTask(subTask1);
        SubTask createdSubtask5 = taskManager.createSubTask(subTask0);
        Epic createdEpic2 = taskManager.getEpicById(2);
        Epic createdEpic3 = taskManager.getEpicById(3);
        taskManager = FileBackedTaskManager.loadFromFile(tempFile);
        Task taskFromFile0 = taskManager.getTaskById(0);
        Task taskFromFile1 = taskManager.getTaskById(1);
        Epic epicFromFile2 = taskManager.getEpicById(2);
        Epic epicFromFile3 = taskManager.getEpicById(3);
        SubTask subTaskFromFile4 = taskManager.getSubTaskById(4);
        SubTask subTaskFromFile5 = taskManager.getSubTaskById(5);

        System.out.println(createdTask0.toString().equals(taskFromFile0.toString()));
        System.out.println(createdTask1.toString().equals(taskFromFile1.toString()));
        System.out.println(createdEpic2.toString().equals(epicFromFile2.toString()));
        System.out.println(createdEpic3.toString().equals(epicFromFile3.toString()));
        System.out.println(createdSubTask4.toString().equals(subTaskFromFile4.toString()));
        System.out.println(createdSubtask5.toString().equals(createdSubtask5.toString()));

    }

    public static FileBackedTaskManager loadFromFile(Path file) {
        int lastId = 0;
        Map<Integer, Task> tasksMap = new HashMap<>();
        Map<Integer, Epic> epicsMap = new HashMap<>();
        Map<Integer, SubTask> subTasksMap = new HashMap<>();
        try {
            List<String> allLines = Files.readAllLines(file);
            if (allLines.isEmpty()) {
                System.out.println("Файл пуст");
            } else {
                allLines.removeFirst();
                for (String line : allLines) {
                    Task task = fromString(line);
                    if (task != null) {
                        if (lastId < task.getId()) lastId = task.getId();
                        switch (task) {
                            case Epic e -> {
                                int id = e.getId();
                                epicsMap.put(id, e);
                            }
                            case SubTask s -> {
                                int id = s.getId();
                                subTasksMap.put(id, s);
                                Epic epic = epicsMap.get(s.getEpicId());
                                epic.addSubTaskById(id);
                            }
                            case Task t -> {
                                int id = t.getId();
                                tasksMap.put(id, t);
                            }
                        }
                    }
                }
            }
        } catch (IOException exception) {
            String errorMessage = "Ошибка чтения файла: " + exception.getMessage();
            System.out.println(errorMessage);
            throw new ManagerFileInitializationException(errorMessage);
        }
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(tasksMap, epicsMap, subTasksMap, lastId,
                Managers.getDefaultHistory(), file);
        return fileBackedTaskManager;
    }

    private static Task fromString(String line) {
        String[] words = line.split(",");
        Task taskToReturn = null;
        try {
            String taskType = words[1];
            taskToReturn = switch (TasksTypes.valueOf(taskType)) {
                case TasksTypes.TASK -> {
                    try {
                        String taskName = words[2];
                        String taskDescription = words[4];
                        Status taskStatus = Status.valueOf(words[3]);
                        Task task = new Task(taskName, taskDescription, taskStatus);
                        int id = Integer.parseInt(words[0]);
                        task.setId(id);
                        yield task;
                    } catch (NumberFormatException e) {
                        System.out.println("Ошибка: id в файле не является числом.");
                        yield null;
                    } catch (IllegalArgumentException e) {
                        System.out.println("Ошибка: некорректное значение статуса задания в файле");
                        yield null;
                    }
                }
                case TasksTypes.EPIC -> {
                    try {
                        String epicName = words[2];
                        Status epicStatus = Status.valueOf(words[3]);
                        String epicDescription = words[4];
                        Task epic = new Epic(epicName, epicDescription, epicStatus);
                        int id = Integer.parseInt(words[0]);
                        epic.setId(id);
                        yield epic;
                    } catch (NumberFormatException e) {
                        System.out.println("Ошибка: id в файле не является числом.");
                        yield null;
                    }
                }
                case TasksTypes.SUBTASK -> {
                    try {
                        String subTaskName = words[2];
                        String subTaskDescription = words[4];
                        Status subTaskStatus = Status.valueOf(words[3]);
                        int subTaskEpicId = Integer.parseInt(words[5]);
                        Task subTask = new SubTask(subTaskName, subTaskDescription, subTaskStatus, subTaskEpicId);
                        int id = Integer.parseInt(words[0]);
                        subTask.setId(id);
                        yield subTask;
                    } catch (NumberFormatException e) {
                        System.out.println("Ошибка: id в файле не является числом.");
                        yield null;
                    } catch (IllegalArgumentException e) {
                        System.out.println("Ошибка: некорректное значение статуса задания в файле");
                        yield null;
                    }
                }
            };
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: некорректное значение типа задания в файле");
        }
        return taskToReturn;
    }

    private void save() {
        List<Task> allTasks = getAllTasks();
        List<Epic> allEpics = getAllEpics();
        List<SubTask> allSubtasks = getAllSubTasks();
        try (BufferedWriter writer = Files.newBufferedWriter(data, StandardCharsets.UTF_8)) {
            writer.write(DATA_HEAD);
            for (Task task : allTasks) {
                writer.write(task.toString());
                writer.newLine();
            }
            for (Epic epic : allEpics) {
                writer.write(epic.toString());
                writer.newLine();
            }
            for (SubTask subTask : allSubtasks) {
                writer.write(subTask.toString());
                writer.newLine();
            }
        } catch (IOException exception) {
            String errorMessage = "Ошибка сохранения в файл: " + exception.getMessage();
            System.out.println(errorMessage);
            throw new ManagerSaveException(errorMessage);
        }

    }

    @Override
    public Task createTask(Task task) {
        Task t = super.createTask(task);
        save();
        return t;
    }

    @Override
    public Task updateTask(Task task) {
        Task t = super.updateTask(task);
        save();
        return (t);
    }

    @Override
    public Task deleteTaskById(Integer id) {
        Task t = super.deleteTaskById(id);
        save();
        return t;
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public SubTask createSubTask(SubTask subTask) {
        SubTask s = super.createSubTask(subTask);
        save();
        return s;
    }

    @Override
    public SubTask updateSubTask(SubTask subTask) {
        SubTask s = super.updateSubTask(subTask);
        save();
        return s;
    }

    @Override
    public SubTask deleteSubTaskById(int id) {
        SubTask s = super.deleteSubTaskById(id);
        save();
        return s;
    }

    @Override
    public void deleteAllSubTasks() {
        super.deleteAllSubTasks();
        save();
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic e = super.createEpic(epic);
        save();
        return e;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        Epic e = super.updateEpic(epic);
        save();
        return e;
    }

    @Override
    public Epic deleteEpicById(Integer id) {
        Epic e = super.deleteEpicById(id);
        save();
        return e;
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }
}
