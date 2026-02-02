package httphandlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import exceptions.ErrorResponse;
import http.handlers.HttpStatus;
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
import tasks.Epic;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;

public class HttpSubTaskHandlerTest extends HttpHandlerTest {

    @Test
    void shouldGetThreeSubTasks() throws IOException, InterruptedException {
        Epic epic0 = new Epic("Epic 0 name", "Epic 0 description");
        SubTask subTask0 = new SubTask("Subtask 0 name", "Subtask 0 Description",
                Status.NEW, Instant.now(), Duration.ofMinutes(5), 0);
        SubTask subTask1 = new SubTask("Subtask 1 name", "Subtask 1 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(10)), Duration.ofMinutes(5), 0);
        SubTask subTask2 = new SubTask("Subtask 2 name", "Subtask 2 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(15)), Duration.ofMinutes(5), 0);
        manager.createEpic(epic0);
        manager.createSubTask(subTask0);
        manager.createSubTask(subTask1);
        manager.createSubTask(subTask2);
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<SubTask> subTasksFromServer = jsonMapper.fromJson(response.body(), new SubTaskListTypeToken().getType());

        assertEquals(HttpStatus.OK.getCode(), response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertNotNull(subTasksFromServer, "Задачи не возвращаются");
        assertEquals(manager.getAllSubTasks(), subTasksFromServer, "Некорректное количество задач");
    }

    @Test
    void shouldGetEmptyListOfSubTasks() throws IOException, InterruptedException {
        Epic epic0 = new Epic("Epic 0 name", "Epic 0 description");
        Task task0 = new Task("Task 0 name", "Task 0 Description",
                Status.NEW, Instant.now(), Duration.ofMinutes(5));
        manager.createEpic(epic0);
        manager.createTask(task0);
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<SubTask> subTasksFromServer = jsonMapper.fromJson(response.body(), new SubTaskListTypeToken().getType());

        assertEquals(HttpStatus.OK.getCode(), response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertEquals(Collections.emptyList(), subTasksFromServer, "Вернулся не пустой список");
        assertEquals(manager.getAllSubTasks(), subTasksFromServer, "Некорректное количество задач на сервере");
    }

    @Test
    void shouldGetSubTaskById() throws IOException, InterruptedException {
        Epic epic0 = new Epic("Epic 0 name", "Epic 0 description");
        SubTask subTask0 = new SubTask("Subtask 0 name", "Subtask 0 Description",
                Status.NEW, Instant.now(), Duration.ofMinutes(5), 0);
        SubTask subTask1 = new SubTask("Subtask 1 name", "Subtask 1 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(10)), Duration.ofMinutes(5), 0);
        SubTask subTask2 = new SubTask("Subtask 2 name", "Subtask 2 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(15)), Duration.ofMinutes(5), 0);
        manager.createEpic(epic0);
        manager.createSubTask(subTask0);
        manager.createSubTask(subTask1);
        manager.createSubTask(subTask2);
        URI url = URI.create("http://localhost:8080/subtasks/2");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        SubTask subTaskFromServer = jsonMapper.fromJson(response.body(), SubTask.class);

        assertEquals(HttpStatus.OK.getCode(), response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertNotNull(subTaskFromServer, "Задача не возвращаются");
        assertEquals(manager.getSubTaskById(2), subTaskFromServer, "Вернулась некорректная задача");
    }

    @Test
    void shouldReturnNotFoundCodeBecauseOfIncorrectId() throws IOException, InterruptedException {
        Epic epic0 = new Epic("Epic 0 name", "Epic 0 description");
        SubTask subTask0 = new SubTask("Subtask 0 name", "Subtask 0 Description",
                Status.NEW, Instant.now(), Duration.ofMinutes(5), 0);
        SubTask subTask1 = new SubTask("Subtask 1 name", "Subtask 1 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(10)), Duration.ofMinutes(5), 0);
        SubTask subTask2 = new SubTask("Subtask 2 name", "Subtask 2 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(15)), Duration.ofMinutes(5), 0);
        manager.createEpic(epic0);
        manager.createSubTask(subTask0);
        manager.createSubTask(subTask1);
        manager.createSubTask(subTask2);
        URI url = URI.create("http://localhost:8080/subtasks/250");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ErrorResponse errorResponse = jsonMapper.fromJson(response.body(), ErrorResponse.class);

        assertEquals(HttpStatus.NOT_FOUND.getCode(), response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertEquals("Подзадача с ID 250 не найдена", errorResponse.getErrorMessage(),
                "Вернулось неккоректное сообщение об ошибке");
        assertEquals("/subtasks/250", errorResponse.getUrlOfRequest(), "Вернулась некорректная ссылка запроса");
    }

    @Test
    void shouldReturnNotFoundCodeBecauseOfNegativeId() throws IOException, InterruptedException {
        Epic epic0 = new Epic("Epic 0 name", "Epic 0 description");
        SubTask subTask0 = new SubTask("Subtask 0 name", "Subtask 0 Description",
                Status.NEW, Instant.now(), Duration.ofMinutes(5), 0);
        SubTask subTask1 = new SubTask("Subtask 1 name", "Subtask 1 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(10)), Duration.ofMinutes(5), 0);
        SubTask subTask2 = new SubTask("Subtask 2 name", "Subtask 2 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(15)), Duration.ofMinutes(5), 0);
        manager.createEpic(epic0);
        manager.createSubTask(subTask0);
        manager.createSubTask(subTask1);
        manager.createSubTask(subTask2);
        URI url = URI.create("http://localhost:8080/subtasks/-666");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ErrorResponse errorResponse = jsonMapper.fromJson(response.body(), ErrorResponse.class);

        assertEquals(HttpStatus.NOT_FOUND.getCode(), response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertEquals("ID не может быть отрицательным", errorResponse.getErrorMessage(),
                "Вернулось неккоректное сообщение об ошибке");
        assertEquals("/subtasks/-666", errorResponse.getUrlOfRequest(), "Вернулась некорректная ссылка запроса");
    }

    @Test
    void shouldReturnNotFoundCodeBecauseOfIdIsNotNumber() throws IOException, InterruptedException {
        Epic epic0 = new Epic("Epic 0 name", "Epic 0 description");
        SubTask subTask0 = new SubTask("Subtask 0 name", "Subtask 0 Description",
                Status.NEW, Instant.now(), Duration.ofMinutes(5), 0);
        SubTask subTask1 = new SubTask("Subtask 1 name", "Subtask 1 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(10)), Duration.ofMinutes(5), 0);
        SubTask subTask2 = new SubTask("Subtask 2 name", "Subtask 2 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(15)), Duration.ofMinutes(5), 0);
        manager.createEpic(epic0);
        manager.createSubTask(subTask0);
        manager.createSubTask(subTask1);
        manager.createSubTask(subTask2);
        URI url = URI.create("http://localhost:8080/subtasks/lol");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ErrorResponse errorResponse = jsonMapper.fromJson(response.body(), ErrorResponse.class);

        assertEquals(HttpStatus.NOT_FOUND.getCode(), response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertEquals("Отправленное ID lol некорреткно", errorResponse.getErrorMessage(),
                "Вернулось неккоректное сообщение об ошибке");
        assertEquals("/subtasks/lol", errorResponse.getUrlOfRequest(), "Вернулась некорректная ссылка запроса");
    }

    @Test
    void shouldReturnNotFoundCodeBecauseUrlIsInvalid() throws IOException, InterruptedException {
        Epic epic0 = new Epic("Epic 0 name", "Epic 0 description");
        SubTask subTask0 = new SubTask("Subtask 0 name", "Subtask 0 Description",
                Status.NEW, Instant.now(), Duration.ofMinutes(5), 0);
        SubTask subTask1 = new SubTask("Subtask 1 name", "Subtask 1 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(10)), Duration.ofMinutes(5), 0);
        SubTask subTask2 = new SubTask("Subtask 2 name", "Subtask 2 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(15)), Duration.ofMinutes(5), 0);
        manager.createEpic(epic0);
        manager.createSubTask(subTask0);
        manager.createSubTask(subTask1);
        manager.createSubTask(subTask2);
        URI url = URI.create("http://localhost:8080/subtasks/yandex/practicum");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ErrorResponse errorResponse = jsonMapper.fromJson(response.body(), ErrorResponse.class);

        assertEquals(HttpStatus.NOT_FOUND.getCode(), response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertEquals("Эндпоинт GET /subtasks/yandex/practicum не найден", errorResponse.getErrorMessage(),
                "Вернулось неккоректное сообщение об ошибке");
        assertEquals("/subtasks/yandex/practicum", errorResponse.getUrlOfRequest(),
                "Вернулась некорректная ссылка запроса");
    }

    @Test
    void shouldAddSubTask() throws IOException, InterruptedException {
        Epic epic0 = new Epic("Epic 0 name", "Epic 0 description");
        SubTask subTask0 = new SubTask("Subtask 0 name", "Subtask 0 Description",
                Status.NEW, Instant.now(), Duration.ofMinutes(5), 0);
        manager.createEpic(epic0);
        String subtaskJson = jsonMapper.toJson(subTask0);
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(HttpStatus.CREATED.getCode(), response.statusCode());

        List<SubTask> subtasksFromManager = manager.getAllSubTasks();

        assertNotNull(subtasksFromManager, "Подзадачи не возвращаются");
        assertEquals(1, subtasksFromManager.size(), "Некорректное количество подзадач");
        assertEquals("Subtask 0 name", subtasksFromManager.get(0).getName(), "Некорректное имя подзадачи");
    }

    @Test
    void shouldUpdateSubTask() throws IOException, InterruptedException {
        Epic epic0 = new Epic("Epic 0 name", "Epic 0 description");
        SubTask subTask0 = new SubTask("Subtask 0 name", "Subtask 0 Description",
                Status.NEW, Instant.now(), Duration.ofMinutes(5), 0);
        SubTask subTask1 = new SubTask("Subtask 1 name", "Subtask 1 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(10)), Duration.ofMinutes(5), 0);
        SubTask subTask2 = new SubTask("Subtask 2 name", "Subtask 2 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(16)), Duration.ofMinutes(5), 0);
        manager.createEpic(epic0);
        manager.createSubTask(subTask0);
        manager.createSubTask(subTask1);
        manager.createSubTask(subTask2);
        SubTask newSubTask1 = new SubTask("New Subtask 1 name", "New Subtask 1 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(10)), Duration.ofMinutes(5), 0);
        newSubTask1.setId(2);
        URI url = URI.create("http://localhost:8080/subtasks/");
        String taskJson = jsonMapper.toJson(newSubTask1);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        SubTask subTaskFromServer = jsonMapper.fromJson(response.body(), SubTask.class);

        assertEquals(HttpStatus.CREATED.getCode(), response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertEquals(newSubTask1, manager.getSubTaskById(2), "Подзадача не обновилась");
        assertEquals(manager.getSubTaskById(2), subTaskFromServer, "Вернулась некорректная подзадача");
    }

    @Test
    void shouldReturnNotAcceptableBecauseOfTaskOverlapCreateSubTask() throws IOException, InterruptedException {
        Epic epic0 = new Epic("Epic 0 name", "Epic 0 description");
        SubTask subTask0 = new SubTask("Subtask 0 name", "Subtask 0 Description",
                Status.NEW, Instant.now(), Duration.ofMinutes(5), 0);
        SubTask subTask1 = new SubTask("Subtask 1 name", "Subtask 1 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(10)), Duration.ofMinutes(5), 0);
        SubTask subTask2 = new SubTask("Subtask 2 name", "Subtask 2 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(7)), Duration.ofMinutes(5), 0);
        manager.createEpic(epic0);
        manager.createSubTask(subTask0);
        manager.createSubTask(subTask1);
        URI url = URI.create("http://localhost:8080/subtasks/");
        String subtaskJson = jsonMapper.toJson(subTask2);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ErrorResponse errorResponse = jsonMapper.fromJson(response.body(), ErrorResponse.class);

        assertEquals(HttpStatus.NOT_ACCEPTABLE.getCode(), response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertEquals("/subtasks/", errorResponse.getUrlOfRequest(),
                "Вернулась некорректная ссылка запроса");
    }

    @Test
    void shouldReturnNotAcceptableBecauseOfTaskOverlapUpdateSubTask() throws IOException, InterruptedException {
        Epic epic0 = new Epic("Epic 0 name", "Epic 0 description");
        SubTask subTask0 = new SubTask("Subtask 0 name", "Subtask 0 Description",
                Status.NEW, Instant.now(), Duration.ofMinutes(5), 0);
        SubTask subTask1 = new SubTask("Subtask 1 name", "Subtask 1 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(10)), Duration.ofMinutes(5), 0);
        SubTask subTask2 = new SubTask("Subtask 2 name", "Subtask 2 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(16)), Duration.ofMinutes(5), 0);
        manager.createEpic(epic0);
        manager.createSubTask(subTask0);
        manager.createSubTask(subTask1);
        manager.createSubTask(subTask2);
        SubTask newSubTask1 = new SubTask("New Subtask 1 name", "New Subtask 1 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(2)), Duration.ofMinutes(5), 0);
        newSubTask1.setId(2);
        URI url = URI.create("http://localhost:8080/subtasks/");
        String taskJson = jsonMapper.toJson(newSubTask1);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ErrorResponse errorResponse = jsonMapper.fromJson(response.body(), ErrorResponse.class);

        assertEquals(HttpStatus.NOT_ACCEPTABLE.getCode(), response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertEquals("/subtasks/", errorResponse.getUrlOfRequest(),
                "Вернулась некорректная ссылка запроса");
    }

    @Test
    void shouldDeleteSubTask() throws IOException, InterruptedException {
        Epic epic0 = new Epic("Epic 0 name", "Epic 0 description");
        SubTask subTask0 = new SubTask("Subtask 0 name", "Subtask 0 Description",
                Status.NEW, Instant.now(), Duration.ofMinutes(5), 0);
        SubTask subTask1 = new SubTask("Subtask 1 name", "Subtask 1 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(10)), Duration.ofMinutes(5), 0);
        SubTask subTask2 = new SubTask("Subtask 2 name", "Subtask 2 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(16)), Duration.ofMinutes(5), 0);
        manager.createEpic(epic0);
        manager.createSubTask(subTask0);
        subTask1 = manager.createSubTask(subTask1);
        manager.createSubTask(subTask2);
        URI url = URI.create("http://localhost:8080/subtasks/2");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        SubTask subTaskFromServer = jsonMapper.fromJson(response.body(), SubTask.class);

        assertEquals(HttpStatus.OK.getCode(), response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertEquals(new ArrayList<>(List.of(subTask0, subTask2)), manager.getAllSubTasks(), "Подзадачи не обновились");
        assertEquals(subTask1, subTaskFromServer, "Вернулась некорректная подзадача");
    }

    @Test
    void shouldReturnNotFoundCodeBecauseUrlIsInvalidDeleteMethod() throws IOException, InterruptedException {
        Epic epic0 = new Epic("Epic 0 name", "Epic 0 description");
        SubTask subTask0 = new SubTask("Subtask 0 name", "Subtask 0 Description",
                Status.NEW, Instant.now(), Duration.ofMinutes(5), 0);
        SubTask subTask1 = new SubTask("Subtask 1 name", "Subtask 1 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(10)), Duration.ofMinutes(5), 0);
        SubTask subTask2 = new SubTask("Subtask 2 name", "Subtask 2 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(16)), Duration.ofMinutes(5), 0);
        manager.createEpic(epic0);
        manager.createSubTask(subTask0);
        manager.createSubTask(subTask1);
        manager.createSubTask(subTask2);
        URI url = URI.create("http://localhost:8080/subtasks/yandex/practicum");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ErrorResponse errorResponse = jsonMapper.fromJson(response.body(), ErrorResponse.class);

        assertEquals(HttpStatus.NOT_FOUND.getCode(), response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertEquals("Эндпоинт DELETE /subtasks/yandex/practicum не найден", errorResponse.getErrorMessage(),
                "Вернулось неккоректное сообщение об ошибке");
        assertEquals("/subtasks/yandex/practicum", errorResponse.getUrlOfRequest(),
                "Вернулась некорректная ссылка запроса");
    }
}
