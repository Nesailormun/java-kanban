package server;

import adapters.DateTimeAdapter;
import adapters.DurationAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import enums.TaskStatus;
import model.Epic;
import model.Subtask;
import model.Task;
import server.handlers.*;
import service.Manager;
import service.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {


    private static final int PORT = 8080;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private static HttpServer httpServer;
    private static TaskManager manager;
    private static Gson gson;

    public HttpTaskServer(TaskManager manager, Gson gson) throws IOException {
        HttpTaskServer.manager = manager;
        HttpTaskServer.gson = gson;
        httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);

        httpServer.createContext("/tasks", new TaskHandler(manager, gson));
        httpServer.createContext("/subtasks", new SubtaskHandler(manager, gson));
        httpServer.createContext("/epics", new EpicHandler(manager, gson));
        httpServer.createContext("/history", new HistoryHandler(manager, gson));
        httpServer.createContext("/prioritized", new PrioritizedHandler(manager, gson));
    }

    public static void main(String[] args) throws InterruptedException, IOException {

        HttpTaskServer httpTaskServer = new HttpTaskServer(Manager.getDefault(), HttpTaskServer.getGson());
        LocalDateTime start = LocalDateTime.of(2025, 2, 7, 10, 0);
        Duration duration = Duration.ofMinutes(30);

        manager.createTask(new Task("TASK1", "SOMETASK", TaskStatus.DONE, start, duration));
        manager.createTask(new Task("TASK2", "SOMETASK", TaskStatus.DONE,
                start.plusMinutes(30), duration));
        manager.createTask(new Task("TASK3", "SOMETASK", TaskStatus.DONE,
                start.plusMinutes(60), duration));
        manager.createTask(new Task("TASK4", "SOMETASK", TaskStatus.DONE));

        manager.createEpic(new Epic(5, "EPIC1", "SOMETODO"));
        manager.createEpic(new Epic(6, "EPIC2", "SOMETODO"));

        manager.createSubtask(new Subtask("SUBTASK1", "SOMETODO", TaskStatus.IN_PROGRESS, 5,
                start.minusMinutes(120), duration));
        manager.createSubtask(new Subtask("SUBTASK2", "SOMETODO", TaskStatus.IN_PROGRESS, 5,
                start.minusMinutes(60), duration));
        manager.createSubtask(new Subtask("SUBTASK3", "SOMETODO", TaskStatus.IN_PROGRESS, 6,
                start.minusMinutes(30), duration));

        HttpTaskServer.start();
        System.out.println("HTTP-сервер запущен на " + PORT + " порту!");
    }


    public static void start() {
        httpServer.start();
    }

    public static void stop() {
        httpServer.stop(0);
    }

    public static Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new DateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter());
        return gsonBuilder.create();
    }
}
