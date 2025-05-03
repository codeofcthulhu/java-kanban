package httphandlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import exceptions.ErrorResponse;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import tasks.Status;
import tasks.Task;

public class HttpTaskHandlerTest extends HttpHandlerTest {

    @Test
    void shouldGetThreeTasks() throws IOException, InterruptedException {
        Task task0 = new Task("Task 0 name", "Task 0 Description",
                Status.NEW, Instant.now(), Duration.ofMinutes(5));
        Task task1 = new Task("Task 1 name", "Task 1 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(10)), Duration.ofMinutes(5));
        Task task2 = new Task("Task 2 name", "Task 2 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(15)), Duration.ofMinutes(5));
        manager.createTask(task0);
        manager.createTask(task1);
        manager.createTask(task2);
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Task> tasksFromServer = jsonMapper.fromJson(response.body(), new TaskListTypeToken().getType());

        assertEquals(200, response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertNotNull(tasksFromServer, "Задачи не возвращаются");
        assertEquals(manager.getAllTasks(), tasksFromServer, "Некорректное количество задач");
    }

    @Test
    void shouldGetEmptyListOfTasks() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Task> tasksFromServer = jsonMapper.fromJson(response.body(), new TaskListTypeToken().getType());

        assertEquals(200, response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertEquals(manager.getAllTasks(), tasksFromServer, "Некорректное количество задач");
    }

    @Test
    void shouldGetTaskById() throws IOException, InterruptedException {
        Task task0 = new Task("Task 0 name", "Task 0 Description",
                Status.NEW, Instant.now(), Duration.ofMinutes(5));
        Task task1 = new Task("Task 1 name", "Task 1 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(10)), Duration.ofMinutes(5));
        Task task2 = new Task("Task 2 name", "Task 2 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(15)), Duration.ofMinutes(5));
        manager.createTask(task0);
        manager.createTask(task1);
        manager.createTask(task2);
        URI url = URI.create("http://localhost:8080/tasks/1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Task taskFromServer = jsonMapper.fromJson(response.body(), Task.class);

        assertEquals(200, response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertNotNull(taskFromServer, "Задача не вернулась");
        assertEquals(task1, taskFromServer, "Вернулась некорректная задача");
    }

    @Test
    void shouldReturnNotFoundCodeBecauseOfIncorrectId() throws IOException, InterruptedException {
        Task task0 = new Task("Task 0 name", "Task 0 Description",
                Status.NEW, Instant.now(), Duration.ofMinutes(5));
        Task task1 = new Task("Task 1 name", "Task 1 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(10)), Duration.ofMinutes(5));
        Task task2 = new Task("Task 2 name", "Task 2 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(15)), Duration.ofMinutes(5));
        manager.createTask(task0);
        manager.createTask(task1);
        manager.createTask(task2);
        URI url = URI.create("http://localhost:8080/tasks/250");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ErrorResponse errorResponse = jsonMapper.fromJson(response.body(), ErrorResponse.class);

        assertEquals(404, response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertEquals("Задача с ID 250 не найдена", errorResponse.getErrorMessage(),
                "Вернулось неккоректное сообщение об ошибке");
        assertEquals("/tasks/250", errorResponse.getUrlOfRequest(), "Вернулась некорректная ссылка запроса");
    }

    @Test
    void shouldReturnNotFoundCodeBecauseOfNegativeId() throws IOException, InterruptedException {
        Task task0 = new Task("Task 0 name", "Task 0 Description",
                Status.NEW, Instant.now(), Duration.ofMinutes(5));
        Task task1 = new Task("Task 1 name", "Task 1 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(10)), Duration.ofMinutes(5));
        Task task2 = new Task("Task 2 name", "Task 2 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(15)), Duration.ofMinutes(5));
        manager.createTask(task0);
        manager.createTask(task1);
        manager.createTask(task2);
        URI url = URI.create("http://localhost:8080/tasks/-2");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ErrorResponse errorResponse = jsonMapper.fromJson(response.body(), ErrorResponse.class);

        assertEquals(404, response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertEquals("ID не может быть отрицательным", errorResponse.getErrorMessage(),
                "Вернулось неккоректное сообщение об ошибке");
        assertEquals("/tasks/-2", errorResponse.getUrlOfRequest(), "Вернулась некорректная ссылка запроса");
    }

    @Test
    void shouldReturnNotFoundCodeBecauseOfIdIsNotNumber() throws IOException, InterruptedException {
        Task task0 = new Task("Task 0 name", "Task 0 Description",
                Status.NEW, Instant.now(), Duration.ofMinutes(5));
        Task task1 = new Task("Task 1 name", "Task 1 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(10)), Duration.ofMinutes(5));
        Task task2 = new Task("Task 2 name", "Task 2 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(15)), Duration.ofMinutes(5));
        manager.createTask(task0);
        manager.createTask(task1);
        manager.createTask(task2);
        URI url = URI.create("http://localhost:8080/tasks/lol");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ErrorResponse errorResponse = jsonMapper.fromJson(response.body(), ErrorResponse.class);

        assertEquals(404, response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertEquals("Отправленное ID lol некорреткно", errorResponse.getErrorMessage(),
                "Вернулось неккоректное сообщение об ошибке");
        assertEquals("/tasks/lol", errorResponse.getUrlOfRequest(), "Вернулась некорректная ссылка запроса");
    }

    @Test
    void shouldReturnNotFoundCodeBecauseUrlIsInvalid() throws IOException, InterruptedException {
        Task task0 = new Task("Task 0 name", "Task 0 Description",
                Status.NEW, Instant.now(), Duration.ofMinutes(5));
        Task task1 = new Task("Task 1 name", "Task 1 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(10)), Duration.ofMinutes(5));
        Task task2 = new Task("Task 2 name", "Task 2 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(15)), Duration.ofMinutes(5));
        manager.createTask(task0);
        manager.createTask(task1);
        manager.createTask(task2);
        URI url = URI.create("http://localhost:8080/tasks/yandex/practicum");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ErrorResponse errorResponse = jsonMapper.fromJson(response.body(), ErrorResponse.class);

        assertEquals(404, response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertEquals("Эндпоинт GET /tasks/yandex/practicum не найден", errorResponse.getErrorMessage(),
                "Вернулось неккоректное сообщение об ошибке");
        assertEquals("/tasks/yandex/practicum", errorResponse.getUrlOfRequest(),
                "Вернулась некорректная ссылка запроса");
    }

    @Test
    void shouldAddTask() throws IOException, InterruptedException {
        Task task = new Task("Task name", "Task Description",
                Status.NEW, Instant.now(), Duration.ofMinutes(5));
        String taskJson = jsonMapper.toJson(task);
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Task> tasksFromManager = manager.getAllTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Task name", tasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    void shouldUpdateTask() throws IOException, InterruptedException {
        Task task0 = new Task("Task 0 name", "Task 0 Description",
                Status.NEW, Instant.now(), Duration.ofMinutes(5));
        Task task1 = new Task("Task 1 name", "Task 1 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(10)), Duration.ofMinutes(5));
        Task task2 = new Task("Task 2 name", "Task 2 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(16)), Duration.ofMinutes(5));
        manager.createTask(task0);
        manager.createTask(task1);
        manager.createTask(task2);
        Task newTask1 = new Task("New Task 1 name", "New Task 1 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(10)), Duration.ofMinutes(5));
        newTask1.setId(1);
        URI url = URI.create("http://localhost:8080/tasks/");
        String taskJson = jsonMapper.toJson(newTask1);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Task taskFromServer = jsonMapper.fromJson(response.body(), Task.class);

        assertEquals(201, response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertEquals(newTask1, manager.getTaskById(1), "Задача не обновилась");
        assertEquals(manager.getTaskById(1), taskFromServer, "Вернулась некорректная задача");
    }

    @Test
    void shouldReturnNotAcceptableBecauseOfTaskOverlapCreateTask() throws IOException, InterruptedException {
        Task task0 = new Task("Task 0 name", "Task 0 Description",
                Status.NEW, Instant.now(), Duration.ofMinutes(5));
        Task task1 = new Task("Task 1 name", "Task 1 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(10)), Duration.ofMinutes(5));
        Task task2 = new Task("Task 2 name", "Task 2 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(12)), Duration.ofMinutes(5));
        manager.createTask(task0);
        manager.createTask(task1);
        URI url = URI.create("http://localhost:8080/tasks/");
        String taskJson = jsonMapper.toJson(task2);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ErrorResponse errorResponse = jsonMapper.fromJson(response.body(), ErrorResponse.class);

        assertEquals(406, response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertEquals("/tasks/", errorResponse.getUrlOfRequest(),
                "Вернулась некорректная ссылка запроса");
    }

    @Test
    void shouldReturnNotAcceptableBecauseOfTaskOverlapUpdateTask() throws IOException, InterruptedException {
        Task task0 = new Task("Task 0 name", "Task 0 Description",
                Status.NEW, Instant.now(), Duration.ofMinutes(5));
        Task task1 = new Task("Task 1 name", "Task 1 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(10)), Duration.ofMinutes(5));
        Task task2 = new Task("Task 2 name", "Task 2 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(15)), Duration.ofMinutes(5));
        manager.createTask(task0);
        manager.createTask(task1);
        manager.createTask(task2);
        Task newTask2 = new Task("New Task 2 name", "New Task 2 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(7)), Duration.ofMinutes(5));
        newTask2.setId(2);
        URI url = URI.create("http://localhost:8080/tasks/");
        String taskJson = jsonMapper.toJson(newTask2);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ErrorResponse errorResponse = jsonMapper.fromJson(response.body(), ErrorResponse.class);

        assertEquals(406, response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertEquals("/tasks/", errorResponse.getUrlOfRequest(),
                "Вернулась некорректная ссылка запроса");
    }

    @Test
    void shouldDeleteTask() throws IOException, InterruptedException {
        Task task0 = new Task("Task 0 name", "Task 0 Description",
                Status.NEW, Instant.now(), Duration.ofMinutes(5));
        Task task1 = new Task("Task 1 name", "Task 1 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(10)), Duration.ofMinutes(5));
        Task task2 = new Task("Task 2 name", "Task 2 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(15)), Duration.ofMinutes(5));
        manager.createTask(task0);
        task1 = manager.createTask(task1);
        manager.createTask(task2);
        URI url = URI.create("http://localhost:8080/tasks/1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Task taskFromServer = jsonMapper.fromJson(response.body(), Task.class);

        assertEquals(200, response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertEquals(new ArrayList<>(List.of(task0, task2)), manager.getAllTasks(), "Задачи не обновились");
        assertEquals(task1, taskFromServer, "Вернулась некорректная задача");
    }

    @Test
    void shouldReturnNotFoundCodeBecauseUrlIsInvalidDeleteMethod() throws IOException, InterruptedException {
        Task task0 = new Task("Task 0 name", "Task 0 Description",
                Status.NEW, Instant.now(), Duration.ofMinutes(5));
        Task task1 = new Task("Task 1 name", "Task 1 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(10)), Duration.ofMinutes(5));
        Task task2 = new Task("Task 2 name", "Task 2 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(15)), Duration.ofMinutes(5));
        manager.createTask(task0);
        manager.createTask(task1);
        manager.createTask(task2);
        URI url = URI.create("http://localhost:8080/tasks/yandex/practicum");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ErrorResponse errorResponse = jsonMapper.fromJson(response.body(), ErrorResponse.class);

        assertEquals(404, response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertEquals("Эндпоинт DELETE /tasks/yandex/practicum не найден", errorResponse.getErrorMessage(),
                "Вернулось неккоректное сообщение об ошибке");
        assertEquals("/tasks/yandex/practicum", errorResponse.getUrlOfRequest(),
                "Вернулась некорректная ссылка запроса");
    }
}
