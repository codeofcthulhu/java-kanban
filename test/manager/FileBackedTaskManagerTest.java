package manager;

import static org.junit.jupiter.api.Assertions.assertThrows;

import exceptions.InvalidTaskException;
import exceptions.TaskOverlapException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;

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
        allExpectedLines.add("id,type,name,status,description,startTime,endTime,duration,epic");
        allExpectedLines.add("0,TASK,Заголовок первого таска,NEW,Описание первого таска,null,,null,");
        allExpectedLines.add("1,EPIC,Заголовок первого эпика,DONE,Описание первого эпика,null,null,null,");
        allExpectedLines.add("2,SUBTASK,Заголовок первого сабтаска,DONE,Описание первого сабтаска,null,,null,1");

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

    @Test
    void shouldReturnEmptyListOfSortedByTimeTasksLoadedFromFile() {
        Task task0 = new Task("Заголовок первого таска", "Описание первого таска", Status.NEW);
        taskManager.createTask(task0);
        Task task1 = new Task("Заголовок второго таска", "Описание второго таска", Status.NEW);
        taskManager.createTask(task1);
        Task task2 = new Task("Заголовок третьего таска", "Описание третьего таска", Status.NEW);
        taskManager.createTask(task2);

        taskManager = FileBackedTaskManager.loadFromFile(tempFile);
        List<Task> listOfSortedByTimeTasks = taskManager.getPrioritizedTasks();

        Assertions.assertTrue(listOfSortedByTimeTasks.isEmpty());
    }

    @Test
    void taskManagerWithThreeTaskFromFileShouldReturnOnlyOneTaskWithNotNullStartTime() {
        long seconds0 = LocalDateTime.of(2023, 3, 15, 13, 40, 0)
                .atZone(ZoneOffset.UTC)
                .toEpochSecond();
        Instant startTime0 = Instant.ofEpochSecond(seconds0);
        Duration duration = Duration.ofMinutes(15);
        Task task0 = new Task("Заголовок первого таска", "Описание первого таска", Status.NEW);
        taskManager.createTask(task0);
        Task task1 = new Task("Заголовок второго таска", "Описание второго таска", Status.NEW, startTime0, duration);
        taskManager.createTask(task1);
        Task task2 = new Task("Заголовок третьего таска", "Описание третьего таска", Status.NEW);
        taskManager.createTask(task2);
        ArrayList<Task> expectedListOfSortedByTimeTasks = new ArrayList<>(List.of(task1));

        taskManager = FileBackedTaskManager.loadFromFile(tempFile);
        List<Task> listOfSortedByTimeTasks = taskManager.getPrioritizedTasks();

        Assertions.assertEquals(expectedListOfSortedByTimeTasks, listOfSortedByTimeTasks);
    }

    @Test
    void shouldRestoreFiveTasksAndFiveSubtasksPrioritizedByTimeFromFile() {
        long seconds0 = LocalDateTime.of(2023, 3, 15, 13, 40, 0)
                .atZone(ZoneOffset.UTC)
                .toEpochSecond();
        Instant startTime0 = Instant.ofEpochSecond(seconds0);
        long seconds1 = LocalDateTime.of(2023, 7, 14, 21, 0, 0)
                .atZone(ZoneOffset.UTC)
                .toEpochSecond();
        Instant startTime1 = Instant.ofEpochSecond(seconds1);
        long seconds2 = LocalDateTime.of(2023, 1, 14, 11, 35, 0)
                .atZone(ZoneOffset.UTC)
                .toEpochSecond();
        Instant startTime2 = Instant.ofEpochSecond(seconds2);
        long seconds3 = LocalDateTime.of(2024, 2, 28, 16, 5, 0)
                .atZone(ZoneOffset.UTC)
                .toEpochSecond();
        Instant startTime3 = Instant.ofEpochSecond(seconds3);
        long seconds4 = LocalDateTime.of(2023, 8, 15, 0, 30, 0)
                .atZone(ZoneOffset.UTC)
                .toEpochSecond();
        Instant startTime4 = Instant.ofEpochSecond(seconds4);
        long seconds5 = LocalDateTime.of(2023, 11, 15, 6, 15, 0)
                .atZone(ZoneOffset.UTC)
                .toEpochSecond();
        Instant startTime5 = Instant.ofEpochSecond(seconds5);
        long seconds6 = LocalDateTime.of(2023, 4, 14, 18, 25, 0)
                .atZone(ZoneOffset.UTC)
                .toEpochSecond();
        Instant startTime6 = Instant.ofEpochSecond(seconds6);
        long seconds7 = LocalDateTime.of(2023, 10, 15, 3, 10, 0)
                .atZone(ZoneOffset.UTC)
                .toEpochSecond();
        Instant startTime7 = Instant.ofEpochSecond(seconds7);
        long seconds8 = LocalDateTime.of(2023, 12, 15, 8, 55, 0)
                .atZone(ZoneOffset.UTC)
                .toEpochSecond();
        Instant startTime8 = Instant.ofEpochSecond(seconds8);
        long seconds9 = LocalDateTime.of(2022, 12, 30, 9, 0, 0)
                .atZone(ZoneOffset.UTC)
                .toEpochSecond();
        Instant startTime9 = Instant.ofEpochSecond(seconds9);
        Duration duration = Duration.ofMinutes(15);
        Task task0 = new Task("Заголовок первого таска", "Описание первого таска", Status.NEW, startTime0, duration);
        taskManager.createTask(task0);
        Task task1 = new Task("Заголовок второго таска", "Описание второго таска", Status.NEW, startTime1, duration);
        taskManager.createTask(task1);
        Task task2 = new Task("Заголовок третьего таска", "Описание третьего таска", Status.NEW, startTime2, duration);
        taskManager.createTask(task2);
        Task task3 = new Task("Заголовок четвёртого таска", "Описание четвёртого таска", Status.NEW, startTime3,
                duration);
        taskManager.createTask(task3);
        Task task4 = new Task("Заголовок пятого таска", "Описание пятого таска", Status.NEW, startTime4, duration);
        taskManager.createTask(task4);
        Epic epic0 = new Epic("Заголовок первого эпика", "Описание первого эпика");
        taskManager.createEpic(epic0);
        SubTask subTask0 = new SubTask("Заголовок первого сабтаска", "Описание первого сабтаска", Status.NEW,
                startTime5, duration, 5);
        taskManager.createSubTask(subTask0);
        SubTask subTask1 = new SubTask("Заголовок второго сабтаска", "Описание второго сабтаска", Status.NEW,
                startTime6, duration, 5);
        taskManager.createSubTask(subTask1);
        SubTask subTask2 = new SubTask("Заголовок третьего сабтаска", "Описание третьего сабтаска", Status.NEW,
                startTime7, duration, 5);
        taskManager.createSubTask(subTask2);
        SubTask subTask3 = new SubTask("Заголовок четвёртого сабтаска", "Описание четвёртого сабтаска", Status.NEW,
                startTime8, duration, 5);
        taskManager.createSubTask(subTask3);
        SubTask subTask4 = new SubTask("Заголовок пятого сабтаска", "Описание пятого сабтаска", Status.NEW, startTime9,
                duration, 5);
        taskManager.createSubTask(subTask4);
        ArrayList<Task> expectedListOfSortedByTimeTasks = new ArrayList<>();
        expectedListOfSortedByTimeTasks.add(subTask4);
        expectedListOfSortedByTimeTasks.add(task2);
        expectedListOfSortedByTimeTasks.add(task0);
        expectedListOfSortedByTimeTasks.add(subTask1);
        expectedListOfSortedByTimeTasks.add(task1);
        expectedListOfSortedByTimeTasks.add(task4);
        expectedListOfSortedByTimeTasks.add(subTask2);
        expectedListOfSortedByTimeTasks.add(subTask0);
        expectedListOfSortedByTimeTasks.add(subTask3);
        expectedListOfSortedByTimeTasks.add(task3);

        taskManager = FileBackedTaskManager.loadFromFile(tempFile);
        List<Task> listOfSortedByTimeTasks = taskManager.getPrioritizedTasks();

        Assertions.assertEquals(expectedListOfSortedByTimeTasks, listOfSortedByTimeTasks);
    }

    @Test
    void shouldThrowInvalidTaskExceptionBecauseOfTaskWithNullAlsoThisTaskShouldNotBeSavedInFile() {
        Task task0 = new Task("Заголовок первого таска", "Описание первого таска", Status.NEW);
        taskManager.createTask(task0);
        Task task1 = new Task("Заголовок второго таска", "Описание второго таска", Status.NEW);
        taskManager.createTask(task1);
        Task task2 = new Task(null, null, null);
        List<Task> expectedList = new ArrayList<>(List.of(task0, task1));
        assertThrows(InvalidTaskException.class, () -> {
                    taskManager.createTask(task2);
                },
                "Пустые поля в переданном в менеджер таске вызывают исключение");

        taskManager = FileBackedTaskManager.loadFromFile(tempFile);
        List<Task> tasksFromFile = taskManager.getAllTasks();
        Assertions.assertEquals(expectedList, tasksFromFile);
    }
}
