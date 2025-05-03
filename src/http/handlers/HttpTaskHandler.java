package http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import exceptions.EndpointNotFoundException;
import exceptions.TaskIdIsIncorrectException;
import exceptions.InvalidTaskException;
import exceptions.TaskNotFoundException;
import exceptions.TaskOverlapException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import manager.TaskManager;
import tasks.Task;

public class HttpTaskHandler extends BaseHttpHandler {

    public HttpTaskHandler(TaskManager manager, Gson jsonMapper) {
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

    protected void handleGet(HttpExchange exchange)
            throws IOException, TaskNotFoundException, TaskIdIsIncorrectException, EndpointNotFoundException {
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");
        if (pathParts.length == 2) {
            List<Task> allTasks = manager.getAllTasks();
            String json = jsonMapper.toJson(allTasks);
            sendResponse(exchange, json, 200);
        } else if (pathParts.length == 3) {
            try {
                int id = Integer.parseInt(pathParts[2]);
                if (id < 0) {
                    throw new TaskIdIsIncorrectException("ID не может быть отрицательным");
                } else {
                    Task taskById = manager.getTaskById(id);
                    String json = jsonMapper.toJson(taskById);
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

    protected void handlePost(HttpExchange exchange)
            throws IOException, TaskOverlapException, InvalidTaskException, EndpointNotFoundException {
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");
        if (pathParts.length == 2) {
            byte[] bodyBytes = exchange.getRequestBody().readAllBytes();
            String bodyString = new String(bodyBytes, StandardCharsets.UTF_8);
            Task task = jsonMapper.fromJson(bodyString, Task.class);
            Task postTask;
            if (Objects.isNull(task.getId())) {
                postTask = manager.createTask(task);
            } else {
                postTask = manager.updateTask(task);
            }
            String json = jsonMapper.toJson(postTask);
            sendResponse(exchange, json, 201);
        } else {
            throw new EndpointNotFoundException(
                    String.format("Эндпоинт %s %s не найден", exchange.getRequestMethod(), path));
        }

    }

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
                    Task taskById = manager.deleteTaskById(id);
                    String json = jsonMapper.toJson(taskById);
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
