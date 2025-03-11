package manager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    Path tempFile;

    @Override
    protected FileBackedTaskManager createTaskManager() {
        try {
            tempFile = Files.createTempFile("data", ".csv");
            return Managers.getFileBackedTaskManager(tempFile);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать временный файл", e);
        }
    }

    @BeforeEach
    public void setUp() throws IOException {
        tempFile = Files.createTempFile("data", ".csv");
        System.out.println("Временный файл создан: " + tempFile);
        taskManager = Managers.getFileBackedTaskManager(tempFile);
    }

    @AfterEach
    public void deleteTempFile() throws IOException {
        Files.delete(tempFile);
        System.out.println("Временный файл удалён: " + tempFile);
    }

    @Test
    void shouldSaveEmptyFileAndLoadItAfter() {
        TaskManager taskManagerNew = FileBackedTaskManager.loadFromFile(tempFile);

        List<Task> allTasks = taskManagerNew.getAllTasks();
        List<SubTask> allSubTasks = taskManagerNew.getAllSubTasks();
        List<Epic> allEpics = taskManagerNew.getAllEpics();

        Assertions.assertTrue(allTasks.isEmpty());
        Assertions.assertTrue(allSubTasks.isEmpty());
        Assertions.assertTrue(allEpics.isEmpty());
    }

    @Test
    void shouldSaveFileWithOneTaskOneEpicAndOneSubTask() throws IOException {
        Task task0 = new Task("Заголовок первого таска", "Описание первого таска", Status.NEW);
        Epic epic0 = new Epic("Заголовок первого эпика", "Описание первого эпика");
        SubTask subTask0 = new SubTask("Заголовок первого сабтаска", "Описание первого сабтаска", Status.DONE, 1);
        taskManager.createTask(task0);
        taskManager.createEpic(epic0);
        taskManager.createSubTask(subTask0);

        List<String> allLines = Files.readAllLines(tempFile);
        List<String> allExpectedLines = new ArrayList<>(4);
        allExpectedLines.add("id,type,name,status,description,epic");
        allExpectedLines.add("0,TASK,Заголовок первого таска,NEW,Описание первого таска,");
        allExpectedLines.add("1,EPIC,Заголовок первого эпика,DONE,Описание первого эпика,");
        allExpectedLines.add("2,SUBTASK,Заголовок первого сабтаска,DONE,Описание первого сабтаска,1");

        Assertions.assertTrue(allLines.equals(allExpectedLines));
    }

    @Test
    void shouldRestoreOneTaskOneEpicAndOneSubTaskFromFile() {
        Task task0 = new Task("Заголовок первого таска", "Описание первого таска", Status.NEW);
        Epic epic0 = new Epic("Заголовок первого эпика", "Описание первого эпика");
        SubTask subTask0 = new SubTask("Заголовок первого сабтаска", "Описание первого сабтаска", Status.DONE, 1);
        taskManager.createTask(task0);
        taskManager.createEpic(epic0);
        taskManager.createSubTask(subTask0);
        Task task = taskManager.getTaskById(0);
        Epic epic = taskManager.getEpicById(1);
        SubTask subTask = taskManager.getSubTaskById(2);

        taskManager = FileBackedTaskManager.loadFromFile(tempFile);

        Assertions.assertTrue(taskManager.getTaskById(0).equals(task));
        Assertions.assertTrue(taskManager.getEpicById(1).equals(epic));
        Assertions.assertTrue(taskManager.getSubTaskById(2).equals(subTask));
        Assertions.assertTrue(taskManager.getTaskById(0).getName().equals(task.getName()));
        Assertions.assertTrue(taskManager.getEpicById(1).getDescription().equals(epic.getDescription()));
        Assertions.assertTrue(taskManager.getSubTaskById(2).getEpicId().equals(subTask.getEpicId()));
    }
}
