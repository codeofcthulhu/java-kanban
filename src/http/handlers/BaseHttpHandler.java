package http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.ErrorResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import manager.TaskManager;

public abstract class BaseHttpHandler implements HttpHandler {

    protected TaskManager manager;
    protected Gson jsonMapper;

    public BaseHttpHandler(TaskManager manager, Gson jsonMapper) {
        this.manager = manager;
        this.jsonMapper = jsonMapper;
    }

    protected void sendResponse(HttpExchange exchange, String json, int code) throws IOException {
        byte[] response = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(code, response.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    protected void sendError(HttpExchange exchange, String message, int code) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(exchange.getRequestURI().getPath(), message, code
        );
        String json = jsonMapper.toJson(errorResponse);
        sendResponse(exchange, json, code);
    }
}
