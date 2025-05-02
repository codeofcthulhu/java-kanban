package http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import exceptions.EndpointNotFoundException;
import java.io.IOException;
import java.util.List;
import manager.TaskManager;
import tasks.Task;

public class HttpPrioritizedHandler extends BaseHttpHandler {

    public HttpPrioritizedHandler(TaskManager manager, Gson jsonMapper) {
        super(manager, jsonMapper);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        try {
            if (method.equals("GET")) {
                handleGet(exchange);
            } else {
                sendError(exchange, String.format("Обработка метода %s не предусмотрена", method), 405);
            }
        } catch (EndpointNotFoundException exception) {
            sendError(exchange, exception.getMessage(), 404);
        } catch (Exception exception) {
            sendError(exchange, exception.getMessage(), 500);
        } finally {
            exchange.close();
        }
    }

    protected void handleGet(HttpExchange exchange)
            throws IOException, EndpointNotFoundException {
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");
        if (pathParts.length == 2) {
            List<Task> listOfPrioritizedTasks = manager.getPrioritizedTasks();
            String json = jsonMapper.toJson(listOfPrioritizedTasks);
            sendResponse(exchange, json, 200);
        } else {
            throw new EndpointNotFoundException(
                    String.format("Эндпоинт %s %s не найден", exchange.getRequestMethod(), path));
        }
    }
}
