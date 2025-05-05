package http.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import http.handlers.BaseHttpHandler;
import http.handlers.HttpEpicHandler;
import http.handlers.HttpHistoryHandler;
import http.handlers.HttpPrioritizedHandler;
import http.handlers.HttpSubTaskHandler;
import http.handlers.HttpTaskHandler;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import manager.Managers;
import manager.TaskManager;
import tasks.Status;
import tasks.Task;

public class HttpTaskServer {

    private static final String HOSTNAME = "localhost";
    private static final int PORT = 8080;
    private final TaskManager manager;
    private Gson jsonMapper;
    private HttpServer httpServer;

    public HttpTaskServer(TaskManager manager) {
        this.manager = manager;
        jsonMapper = new GsonBuilder()
                .registerTypeAdapter(Instant.class, new InstantAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
    }

    public Gson getJsonMapper() {
        return jsonMapper;
    }

    public void setJsonMapper(Gson jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public void start() throws IOException {
        InetSocketAddress address = new InetSocketAddress(HOSTNAME, PORT);
        httpServer = HttpServer.create(address, 0);
        BaseHttpHandler.setUpHandlers(jsonMapper, manager);
        httpServer.createContext("/tasks", new HttpTaskHandler());
        httpServer.createContext("/subtasks", new HttpSubTaskHandler());
        httpServer.createContext("/epics", new HttpEpicHandler());
        httpServer.createContext("/history", new HttpHistoryHandler());
        httpServer.createContext("/prioritized", new HttpPrioritizedHandler());
        httpServer.start();
    }

    public void stop() {
        httpServer.stop(0);
    }

    public static void main(String[] args) throws IOException {
        Path tempFile = Files.createTempFile("data", ".csv");
        TaskManager manager = Managers.getFileBackedTaskManager(tempFile);
        HttpTaskServer server = new HttpTaskServer(manager);
        server.start();

        Task task0 = new Task("Заголовок первого таска", "Описание первого таска", Status.NEW);
        manager.createTask(task0);
        Task task1 = new Task("Заголовок второго таска", "Описание второго таска", Status.NEW);
        manager.createTask(task1);
        Task task2 = new Task("Заголовок третьего таска", "Описание третьего таска", Status.NEW);
        manager.createTask(task2);
        long seconds3 = LocalDateTime.of(2025, 4, 2, 12, 45, 0)
                .atZone(ZoneOffset.UTC)
                .toEpochSecond();
        Instant startTime3 = Instant.ofEpochSecond(seconds3);
        long seconds4 = LocalDateTime.of(2025, 4, 3, 13, 0, 0)
                .atZone(ZoneOffset.UTC)
                .toEpochSecond();
        Instant startTime4 = Instant.ofEpochSecond(seconds4);
        Duration duration = Duration.ofMinutes(15);
        Task task3 = new Task("Заголовок четвёртого таска", "Описание четвёртого таска", Status.NEW, startTime3,
                duration);
        manager.createTask(task3);
        Task task4 = new Task("Заголовок пятого таска", "Описание пятого таска", Status.NEW, startTime4, duration);
        manager.createTask(task4);
    }


}
