package http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import exceptions.EndpointNotFoundException;
import exceptions.InvalidTaskException;
import exceptions.TaskIdIsIncorrectException;
import exceptions.TaskNotFoundException;
import exceptions.TaskOverlapException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import manager.TaskManager;
import tasks.Epic;
import tasks.SubTask;

public class HttpEpicHandler extends HttpTaskHandler {

    public HttpEpicHandler(TaskManager manager, Gson jsonMapper) {
        super(manager, jsonMapper);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        try {
            switch (method) {
                case "GET":
                    handleGet(exchange);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange);
                    break;
                default:
                    sendError(exchange, String.format("Обработка метода %s не предусмотрена", method), 405);
            }
        } catch (TaskNotFoundException | TaskIdIsIncorrectException | EndpointNotFoundException exception) {
            sendError(exchange, exception.getMessage(), 404);
        } catch (TaskOverlapException exception) {
            sendError(exchange, exception.getMessage(), 406);
        } catch (Exception exception) {
            sendError(exchange, exception.getMessage(), 500);
        } finally {
            exchange.close();
        }
    }

    @Override
    protected void handleGet(HttpExchange exchange)
            throws IOException, TaskNotFoundException, TaskIdIsIncorrectException, EndpointNotFoundException {
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");
        if (pathParts.length == 2) {
            List<Epic> allEpics = manager.getAllEpics();
            String json = jsonMapper.toJson(allEpics);
            sendResponse(exchange, json, 200);
        } else if (pathParts.length == 3) {
            try {
                int id = Integer.parseInt(pathParts[2]);
                if (id < 0) {
                    throw new TaskIdIsIncorrectException("ID не может быть отрицательным");
                } else {
                    Epic epicById = manager.getEpicById(id);
                    String json = jsonMapper.toJson(epicById);
                    sendResponse(exchange, json, 200);
                }
            } catch (NumberFormatException exception) {
                throw new TaskIdIsIncorrectException(String.format("Отправленное ID %s некорреткно", pathParts[2]));
            }
        } else if ((pathParts.length == 4) && (pathParts[3].equals("subtasks"))) {
            try {
                int id = Integer.parseInt(pathParts[2]);
                if (id < 0) {
                    throw new TaskIdIsIncorrectException("ID не может быть отрицательным");
                } else {
                    List<SubTask> allSubTasksOfEpic = manager.getAllSubTasksOfOneEpic(id);
                    String json = jsonMapper.toJson(allSubTasksOfEpic);
                    sendResponse(exchange, json, 200);
                }
            } catch (NumberFormatException exception) {
                throw new TaskIdIsIncorrectException(String.format("Отправленное ID %s некорреткно", pathParts[2]));
            }
        } else {
            throw new EndpointNotFoundException(
                    String.format("Эндпоинт %s %s не найден", exchange.getRequestMethod(), path));
        }
    }

    @Override
    protected void handlePost(HttpExchange exchange)
            throws IOException, TaskOverlapException, InvalidTaskException, EndpointNotFoundException {
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");
        if (pathParts.length == 2) {
            byte[] bodyBytes = exchange.getRequestBody().readAllBytes();
            String bodyString = new String(bodyBytes, StandardCharsets.UTF_8);
            Epic epic = jsonMapper.fromJson(bodyString, Epic.class);
            Epic postEpic;
            if (Objects.isNull(epic.getId())) {
                postEpic = manager.createEpic(epic);
            } else {
                postEpic = manager.updateEpic(epic);
            }

            String json = jsonMapper.toJson(postEpic);
            sendResponse(exchange, json, 201);
        } else {
            throw new EndpointNotFoundException(
                    String.format("Эндпоинт %s %s не найден", exchange.getRequestMethod(), path));
        }
    }

    @Override
    protected void handleDelete(HttpExchange exchange)
            throws IOException, TaskIdIsIncorrectException, EndpointNotFoundException {
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");
        if (pathParts.length == 3) {
            try {
                int id = Integer.parseInt(pathParts[2]);
                if (id < 0) {
                    throw new TaskIdIsIncorrectException("ID не может быть отрицательным");
                } else {
                    Epic EpicById = manager.deleteEpicById(id);
                    String json = jsonMapper.toJson(EpicById);
                    sendResponse(exchange, json, 200);
                }
            } catch (NumberFormatException exception) {
                throw new TaskIdIsIncorrectException(String.format("Отправленное ID %s некорреткно", pathParts[2]));
            }
        } else {
            throw new EndpointNotFoundException(
                    String.format("Эндпоинт %s %s не найден", exchange.getRequestMethod(), path));
        }

    }
}
