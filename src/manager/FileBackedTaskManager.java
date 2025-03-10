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
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private Path data;
    private static final String DATA_HEAD = "id,type,name,status,description,epic";

    public FileBackedTaskManager(HistoryManager historyManager, Path data) {
        super(historyManager);
        this.data = data;
    }

    public static FileBackedTaskManager loadFromFile(Path file) {
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(new InMemoryHistoryManager(), file);
        int lastId = 0;
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            List<String> allLines = Files.readAllLines(file);
            allLines.removeFirst();
            for (String line : allLines) {
                Task task = fromString(line);
                if (task != null) {
                    if (lastId < task.getId()) lastId = task.getId();
                    switch (task) {
                        case Epic e -> {
                            int id = e.getId();
                            epics.put(id, e);
                        }
                        case SubTask s -> {
                            int id = s.getId();
                            subTasks.put(id, s);
                            Epic epic = epics.get(s.getEpicId());
                            epic.addSubTaskById(id);
                            updateEpicStatus(epic);
                        }
                        case Task t -> {
                            int id = t.getId();
                            tasks.put(id, t);
                        }
                    }
                }
            }
        } catch (IOException exception) {
            String errorMessage = "Ошибка чтения файла: " + exception.getMessage();
            System.out.println(errorMessage);
            throw new ManagerFileInitializationException(errorMessage);
        }
        idCounter = lastId;
        return fileBackedTaskManager;
    }

    private static Task fromString(String line) {
        String[] words = line.split(",");
        Task taskToReturn = null;
        try {
            taskToReturn = switch (TasksTypes.valueOf(words[1])) {
                case TasksTypes.TASK -> {
                    try {
                        Task task = new Task(words[2], words[4], Status.valueOf(words[3]));
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
                        Task epic = new Epic(words[2], words[4]);
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
                        Task subTask = new SubTask(words[2], words[4], Status.valueOf(words[3]), Integer.parseInt(words[5]));
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
            }
            for (Epic epic : allEpics) {
                writer.write(epic.toString());
            }
            for (SubTask subTask : allSubtasks) {
                writer.write(subTask.toString());
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
        return(t);
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
