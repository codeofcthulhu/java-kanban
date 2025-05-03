package httphandlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import tasks.Epic;
import tasks.Status;
import tasks.SubTask;

public class HttpEpicHandlerTest extends HttpHandlerTest {

    @Test
    void shouldGetThreeEpics() throws IOException, InterruptedException {
        Epic epic0 = new Epic("Epic 0 name", "Epic 0 description");
        Epic epic1 = new Epic("Epic 1 name", "Epic 1 description");
        Epic epic2 = new Epic("Epic 2 name", "Epic 2 description");
        manager.createEpic(epic0);
        manager.createEpic(epic1);
        manager.createEpic(epic2);
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Epic> epicsFromServer = jsonMapper.fromJson(response.body(), new EpicListTypeToken().getType());

        assertEquals(200, response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertNotNull(epicsFromServer, "Эпики не возвращаются");
        assertEquals(manager.getAllEpics(), epicsFromServer, "Некорректное количество эпиков");
    }

    @Test
    void shouldGetEmptyListOfEpics() throws IOException, InterruptedException {
        Epic epic0 = new Epic("Epic 0 name", "Epic 0 description");
        Epic epic1 = new Epic("Epic 1 name", "Epic 1 description");
        Epic epic2 = new Epic("Epic 2 name", "Epic 2 description");
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Epic> epicsFromServer = jsonMapper.fromJson(response.body(), new EpicListTypeToken().getType());

        assertEquals(200, response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertEquals(Collections.emptyList(), epicsFromServer, "Вернулся не пустой список");
        assertEquals(manager.getAllEpics(), epicsFromServer, "Некорректное количество задач на сервере");
    }

    @Test
    void shouldGetEpicById() throws IOException, InterruptedException {
        Epic epic0 = new Epic("Epic 0 name", "Epic 0 description");
        Epic epic1 = new Epic("Epic 1 name", "Epic 1 description");
        Epic epic2 = new Epic("Epic 2 name", "Epic 2 description");
        manager.createEpic(epic0);
        manager.createEpic(epic1);
        manager.createEpic(epic2);
        URI url = URI.create("http://localhost:8080/epics/2");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Epic epicFromServer = jsonMapper.fromJson(response.body(), Epic.class);

        assertEquals(200, response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertNotNull(epicFromServer, "Задача не возвращаются");
        assertEquals(manager.getEpicById(2), epicFromServer, "Вернулась некорректная задача");
    }

    @Test
    void shouldReturnNotFoundCodeBecauseOfIncorrectId() throws IOException, InterruptedException {
        Epic epic0 = new Epic("Epic 0 name", "Epic 0 description");
        Epic epic1 = new Epic("Epic 1 name", "Epic 1 description");
        Epic epic2 = new Epic("Epic 2 name", "Epic 2 description");
        manager.createEpic(epic0);
        manager.createEpic(epic1);
        manager.createEpic(epic2);
        URI url = URI.create("http://localhost:8080/epics/250");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ErrorResponse errorResponse = jsonMapper.fromJson(response.body(), ErrorResponse.class);

        assertEquals(404, response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertEquals("Эпик с ID 250 не найден", errorResponse.getErrorMessage(),
                "Вернулось неккоректное сообщение об ошибке");
        assertEquals("/epics/250", errorResponse.getUrlOfRequest(), "Вернулась некорректная ссылка запроса");
    }

    @Test
    void shouldReturnNotFoundCodeBecauseOfNegativeId() throws IOException, InterruptedException {
        Epic epic0 = new Epic("Epic 0 name", "Epic 0 description");
        Epic epic1 = new Epic("Epic 1 name", "Epic 1 description");
        Epic epic2 = new Epic("Epic 2 name", "Epic 2 description");
        manager.createEpic(epic0);
        manager.createEpic(epic1);
        manager.createEpic(epic2);
        URI url = URI.create("http://localhost:8080/epics/-666");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ErrorResponse errorResponse = jsonMapper.fromJson(response.body(), ErrorResponse.class);

        assertEquals(404, response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertEquals("ID не может быть отрицательным", errorResponse.getErrorMessage(),
                "Вернулось неккоректное сообщение об ошибке");
        assertEquals("/epics/-666", errorResponse.getUrlOfRequest(), "Вернулась некорректная ссылка запроса");
    }

    @Test
    void shouldReturnNotFoundCodeBecauseOfIdIsNotNumber() throws IOException, InterruptedException {
        Epic epic0 = new Epic("Epic 0 name", "Epic 0 description");
        Epic epic1 = new Epic("Epic 1 name", "Epic 1 description");
        Epic epic2 = new Epic("Epic 2 name", "Epic 2 description");
        manager.createEpic(epic0);
        manager.createEpic(epic1);
        manager.createEpic(epic2);
        URI url = URI.create("http://localhost:8080/epics/lol");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ErrorResponse errorResponse = jsonMapper.fromJson(response.body(), ErrorResponse.class);

        assertEquals(404, response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertEquals("Отправленное ID lol некорреткно", errorResponse.getErrorMessage(),
                "Вернулось неккоректное сообщение об ошибке");
        assertEquals("/epics/lol", errorResponse.getUrlOfRequest(), "Вернулась некорректная ссылка запроса");
    }

    @Test
    void shouldReturnNotFoundCodeBecauseUrlIsInvalid() throws IOException, InterruptedException {
        Epic epic0 = new Epic("Epic 0 name", "Epic 0 description");
        Epic epic1 = new Epic("Epic 1 name", "Epic 1 description");
        Epic epic2 = new Epic("Epic 2 name", "Epic 2 description");
        manager.createEpic(epic0);
        manager.createEpic(epic1);
        manager.createEpic(epic2);
        URI url = URI.create("http://localhost:8080/epics/yandex/practicum");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ErrorResponse errorResponse = jsonMapper.fromJson(response.body(), ErrorResponse.class);

        assertEquals(404, response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertEquals("Эндпоинт GET /epics/yandex/practicum не найден", errorResponse.getErrorMessage(),
                "Вернулось неккоректное сообщение об ошибке");
        assertEquals("/epics/yandex/practicum", errorResponse.getUrlOfRequest(),
                "Вернулась некорректная ссылка запроса");
    }

    @Test
    void shouldGetThreeSubTasksOfOneEpic() throws IOException, InterruptedException {
        Epic epic0 = new Epic("Epic 0 name", "Epic 0 description");
        Epic epic1 = new Epic("Epic 1 name", "Epic 1 description");
        Epic epic2 = new Epic("Epic 2 name", "Epic 2 description");
        SubTask subTask0 = new SubTask("Subtask 0 name", "Subtask 0 Description",
                Status.NEW, Instant.now(), Duration.ofMinutes(5), 2);
        SubTask subTask1 = new SubTask("Subtask 1 name", "Subtask 1 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(10)), Duration.ofMinutes(5), 2);
        SubTask subTask2 = new SubTask("Subtask 2 name", "Subtask 2 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(15)), Duration.ofMinutes(5), 2);
        manager.createEpic(epic0);
        manager.createEpic(epic1);
        manager.createEpic(epic2);
        manager.createSubTask(subTask0);
        manager.createSubTask(subTask1);
        manager.createSubTask(subTask2);
        URI url = URI.create("http://localhost:8080/epics/2/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<SubTask> subTasksFromServer = jsonMapper.fromJson(response.body(), new SubTaskListTypeToken().getType());

        assertEquals(200, response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertNotNull(subTasksFromServer, "Подзадачи не возвращаются");
        assertEquals(manager.getAllSubTasksOfOneEpic(2), subTasksFromServer, "Некорректное количество подзадач");
    }

    @Test
    void shouldGetEmptyListOfSubTasksOfOneEpic() throws IOException, InterruptedException {
        Epic epic0 = new Epic("Epic 0 name", "Epic 0 description");
        Epic epic1 = new Epic("Epic 1 name", "Epic 1 description");
        Epic epic2 = new Epic("Epic 2 name", "Epic 2 description");
        SubTask subTask0 = new SubTask("Subtask 0 name", "Subtask 0 Description",
                Status.NEW, Instant.now(), Duration.ofMinutes(5), 2);
        SubTask subTask1 = new SubTask("Subtask 1 name", "Subtask 1 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(10)), Duration.ofMinutes(5), 2);
        SubTask subTask2 = new SubTask("Subtask 2 name", "Subtask 2 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(15)), Duration.ofMinutes(5), 2);
        manager.createEpic(epic0);
        manager.createEpic(epic1);
        manager.createEpic(epic2);
        manager.createSubTask(subTask0);
        manager.createSubTask(subTask1);
        manager.createSubTask(subTask2);
        URI url = URI.create("http://localhost:8080/epics/1/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<SubTask> subTasksFromServer = jsonMapper.fromJson(response.body(), new SubTaskListTypeToken().getType());

        assertEquals(200, response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertTrue(subTasksFromServer.isEmpty(), "Вернулся не пустой список подзадач");
        assertEquals(manager.getAllSubTasksOfOneEpic(1), subTasksFromServer, "Некорректное количество подзадач");
    }

    @Test
    void shouldReturnNotFoundResponseBecauseOfNotExistEpicInRequestBody() throws IOException, InterruptedException {
        Epic epic0 = new Epic("Epic 0 name", "Epic 0 description");
        Epic epic1 = new Epic("Epic 1 name", "Epic 1 description");
        Epic epic2 = new Epic("Epic 2 name", "Epic 2 description");
        SubTask subTask0 = new SubTask("Subtask 0 name", "Subtask 0 Description",
                Status.NEW, Instant.now(), Duration.ofMinutes(5), 2);
        SubTask subTask1 = new SubTask("Subtask 1 name", "Subtask 1 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(10)), Duration.ofMinutes(5), 2);
        SubTask subTask2 = new SubTask("Subtask 2 name", "Subtask 2 Description",
                Status.NEW, Instant.now().plus(Duration.ofMinutes(15)), Duration.ofMinutes(5), 2);
        manager.createEpic(epic0);
        manager.createEpic(epic1);
        manager.createEpic(epic2);
        manager.createSubTask(subTask0);
        manager.createSubTask(subTask1);
        manager.createSubTask(subTask2);
        URI url = URI.create("http://localhost:8080/epics/3/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ErrorResponse errorResponse = jsonMapper.fromJson(response.body(), ErrorResponse.class);

        assertEquals(404, response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertEquals("Эпик с ID 3 не найден", errorResponse.getErrorMessage(),
                "Вернулось неккоректное сообщение об ошибке");
        assertEquals("/epics/3/subtasks", errorResponse.getUrlOfRequest(), "Вернулась некорректная ссылка запроса");
    }

    @Test
    void shouldAddEpic() throws IOException, InterruptedException {
        Epic epic0 = new Epic("Epic 0 name", "Epic 0 description");
        Epic epic1 = new Epic("Epic 1 name", "Epic 1 description");
        Epic epic2 = new Epic("Epic 2 name", "Epic 2 description");
        manager.createEpic(epic0);
        manager.createEpic(epic1);
        String epicJson = jsonMapper.toJson(epic2);
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Epic> EpicsFromServer = manager.getAllEpics();

        assertEquals(201, response.statusCode());
        assertNotNull(EpicsFromServer, "Эпики не возвращаются");
        assertEquals(3, EpicsFromServer.size(), "Некорректное количество эпиков'");
        assertEquals("Epic 2 name", EpicsFromServer.get(2).getName(), "Некорректное имя добавленного эпика");
    }

    @Test
    void shouldUpdateEpic() throws IOException, InterruptedException {
        Epic epic0 = new Epic("Epic 0 name", "Epic 0 description");
        Epic epic1 = new Epic("Epic 1 name", "Epic 1 description");
        Epic epic2 = new Epic("Epic 2 name", "Epic 2 description");
        manager.createEpic(epic0);
        manager.createEpic(epic1);
        manager.createEpic(epic2);
        Epic newEpic1 = new Epic("New epic 1 name", "New epic 1 description");
        newEpic1.setId(1);
        URI url = URI.create("http://localhost:8080/epics/");
        String taskJson = jsonMapper.toJson(newEpic1);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Epic epic = jsonMapper.fromJson(response.body(), Epic.class);

        assertEquals(201, response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertEquals(newEpic1, manager.getEpicById(1), "Эпик не обновился");
        assertEquals(manager.getEpicById(1), epic, "Вернулся некорректный эпик");
    }

    @Test
    void shouldDeleteEpic() throws IOException, InterruptedException {
        Epic epic0 = new Epic("Epic 0 name", "Epic 0 description");
        Epic epic1 = new Epic("Epic 1 name", "Epic 1 description");
        Epic epic2 = new Epic("Epic 2 name", "Epic 2 description");
        manager.createEpic(epic0);
        epic1 = manager.createEpic(epic1);
        manager.createEpic(epic2);
        URI url = URI.create("http://localhost:8080/epics/1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Epic epicFromResponse = jsonMapper.fromJson(response.body(), Epic.class);

        assertEquals(200, response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertEquals(new ArrayList<>(List.of(epic0, epic2)), manager.getAllEpics(), "Эпики не обновились");
        assertEquals(epic1, epicFromResponse, "Вернулся некорректный эпик");
    }

    @Test
    void shouldReturnNotFoundCodeBecauseUrlIsInvalidDeleteMethod() throws IOException, InterruptedException {
        Epic epic0 = new Epic("Epic 0 name", "Epic 0 description");
        Epic epic1 = new Epic("Epic 1 name", "Epic 1 description");
        Epic epic2 = new Epic("Epic 2 name", "Epic 2 description");
        manager.createEpic(epic0);
        manager.createEpic(epic1);
        manager.createEpic(epic2);
        URI url = URI.create("http://localhost:8080/epics/yandex/practicum");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ErrorResponse errorResponse = jsonMapper.fromJson(response.body(), ErrorResponse.class);

        assertEquals(404, response.statusCode(), "Вернулся некорректный код ответа сервера");
        assertEquals("Эндпоинт DELETE /epics/yandex/practicum не найден", errorResponse.getErrorMessage(),
                "Вернулось неккоректное сообщение об ошибке");
        assertEquals("/epics/yandex/practicum", errorResponse.getUrlOfRequest(),
                "Вернулась некорректная ссылка запроса");
    }
}
