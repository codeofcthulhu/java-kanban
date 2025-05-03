package httphandlers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;

public class HttpPrioritizedHandlerTest extends HttpHandlerTest {

    @Test
    void shouldReturnPrioritizedList() throws IOException, InterruptedException {
        Task task0 = new Task("Task 0 name", "Task 0 Description", Status.NEW, Instant.now(), Duration.ofMinutes(5));
        Task task1 = new Task("Task 1 name", "Task 1 Description", Status.NEW,
                Instant.now().plus(Duration.ofMinutes(50)), Duration.ofMinutes(5));
        Task task2 = new Task("Task 2 name", "Task 2 Description", Status.NEW,
                Instant.now().plus(Duration.ofMinutes(15)), Duration.ofMinutes(5));
        Epic epic0 = new Epic("Epic 0 name", "Epic 0 description");
        SubTask subTask0 = new SubTask("Subtask 0 name", "Subtask 0 Description", Status.NEW,
                Instant.now().plus(Duration.ofMinutes(25)), Duration.ofMinutes(5), 0);
        SubTask subTask1 = new SubTask("Subtask 1 name", "Subtask 1 Description", Status.NEW,
                Instant.now().plus(Duration.ofMinutes(160)), Duration.ofMinutes(5), 0);
        SubTask subTask2 = new SubTask("Subtask 2 name", "Subtask 2 Description", Status.NEW,
                Instant.now().plus(Duration.ofMinutes(100)), Duration.ofMinutes(5), 0);
        manager.createEpic(epic0);
        manager.createTask(task0);
        manager.createTask(task1);
        manager.createTask(task2);
        manager.createSubTask(subTask0);
        manager.createSubTask(subTask1);
        manager.createSubTask(subTask2);
        List<Task> expectedListOfPrioritizedTasks = List.of(task0, task2, subTask0, task1, subTask2, subTask1);
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Task> prioritizedListFromServer = jsonMapper.fromJson(response.body(), new TaskListTypeToken().getType());

        assertEquals(200, response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertEquals(manager.getPrioritizedTasks(), prioritizedListFromServer,
                "Некорректная приоритизированный список");
        assertEquals(expectedListOfPrioritizedTasks, prioritizedListFromServer,
                "Некорректная приоритизированный список");
    }

    @Test
    void shouldReturnPrioritizedListWithOneElement() throws IOException, InterruptedException {
        Task task0 = new Task("Task 0 name", "Task 0 Description", Status.NEW);
        Task task1 = new Task("Task 1 name", "Task 1 Description", Status.NEW);
        Task task2 = new Task("Task 2 name", "Task 2 Description", Status.NEW);
        Epic epic0 = new Epic("Epic 0 name", "Epic 0 description");
        SubTask subTask0 = new SubTask("Subtask 0 name", "Subtask 0 Description", Status.NEW, 0);
        SubTask subTask1 = new SubTask("Subtask 1 name", "Subtask 1 Description", Status.NEW,
                Instant.now().plus(Duration.ofMinutes(160)), Duration.ofMinutes(5), 0);
        SubTask subTask2 = new SubTask("Subtask 2 name", "Subtask 2 Description", Status.NEW, 0);
        manager.createEpic(epic0);
        manager.createTask(task0);
        manager.createTask(task1);
        manager.createTask(task2);
        manager.createSubTask(subTask0);
        manager.createSubTask(subTask1);
        manager.createSubTask(subTask2);
        List<Task> expectedListOfPrioritizedTasks = List.of(subTask1);
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Task> prioritizedListFromServer = jsonMapper.fromJson(response.body(), new TaskListTypeToken().getType());

        assertEquals(200, response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertEquals(manager.getPrioritizedTasks(), prioritizedListFromServer,
                "Некорректная приоритизированный список");
        assertEquals(expectedListOfPrioritizedTasks, prioritizedListFromServer,
                "Некорректная приоритизированный список");
    }

    @Test
    void shouldReturnEmptyPrioritizedList() throws IOException, InterruptedException {
        Task task0 = new Task("Task 0 name", "Task 0 Description", Status.NEW);
        Task task1 = new Task("Task 1 name", "Task 1 Description", Status.NEW);
        Task task2 = new Task("Task 2 name", "Task 2 Description", Status.NEW);
        Epic epic0 = new Epic("Epic 0 name", "Epic 0 description");
        SubTask subTask0 = new SubTask("Subtask 0 name", "Subtask 0 Description", Status.NEW, 0);
        SubTask subTask1 = new SubTask("Subtask 1 name", "Subtask 1 Description", Status.NEW, 0);
        SubTask subTask2 = new SubTask("Subtask 2 name", "Subtask 2 Description", Status.NEW, 0);
        manager.createEpic(epic0);
        manager.createTask(task0);
        manager.createTask(task1);
        manager.createTask(task2);
        manager.createSubTask(subTask0);
        manager.createSubTask(subTask1);
        manager.createSubTask(subTask2);
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Task> prioritizedListFromServer = jsonMapper.fromJson(response.body(), new TaskListTypeToken().getType());

        assertEquals(200, response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertEquals(manager.getPrioritizedTasks(), prioritizedListFromServer,
                "Некорректная приоритизированный список");
        assertEquals(Collections.emptyList(), prioritizedListFromServer, "Некорректная приоритизированный список");
        assertEquals(1, prioritizedListFromServer.size(), "Некорректный размер приотизированного списка");
    }
}
