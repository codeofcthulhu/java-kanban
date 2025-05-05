package http.handlers;

import static http.handlers.HttpMethod.GET;

import com.sun.net.httpserver.HttpExchange;
import exceptions.EndpointNotFoundException;
import java.io.IOException;
import java.util.List;
import tasks.Task;

public class HttpHistoryHandler extends BaseHttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        HttpMethod method = HttpMethod.fromString(exchange.getRequestMethod());
        try {
            if (method.equals(GET)) {
                handleGet(exchange);
            } else {
                sendError(exchange, String.format("Обработка метода %s не предусмотрена", method),
                        HttpStatus.METHOD_NOT_ALLOWED.getCode());
            }
        } catch (EndpointNotFoundException exception) {
            sendError(exchange, exception.getMessage(), HttpStatus.NOT_FOUND.getCode());
        } catch (Exception exception) {
            sendError(exchange, exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.getCode());
        } finally {
            exchange.close();
        }
    }

    protected void handleGet(HttpExchange exchange)
            throws IOException, EndpointNotFoundException {
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");
        if (pathParts.length == 2) {
            List<Task> history = manager.getHistory();
            String json = jsonMapper.toJson(history);
            sendResponse(exchange, json, HttpStatus.OK.getCode());
        } else {
            throw new EndpointNotFoundException(
                    String.format("Эндпоинт %s %s не найден", exchange.getRequestMethod(), path));
        }
    }
}
