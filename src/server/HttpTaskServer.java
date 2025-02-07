package server;

import adapters.DateTimeAdapter;
import adapters.DurationAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import enums.TaskStatus;
import exceptions.NotFoundException;
import model.Task;
import server.handlers.*;
import service.InMemoryTaskManager;
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
    private TaskManager manager;

    public HttpTaskServer(TaskManager manager) {
        this.manager = manager;
    }

    public static void main(String[] args) {
        try {
            TaskManager manager = Manager.getDefault();
            LocalDateTime start = LocalDateTime.of(2025, 2, 7, 10, 0);
            Duration duration = Duration.ofMinutes(30);

            manager.createTask(new Task("TASK1", "SOMETASK", TaskStatus.DONE, start, duration));
            manager.createTask(new Task("TASK2", "SOMETASK", TaskStatus.DONE,
                    start.plusMinutes(30), duration));
            manager.createTask(new Task("TASK3", "SOMETASK", TaskStatus.DONE,
                    start.plusMinutes(60), duration));


            httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);

            httpServer.createContext("/tasks", new TaskHandler(manager));
            httpServer.createContext("/subtasks", new SubtaskHandler(manager));
            httpServer.createContext("/epics", new EpicHandler(manager));
            httpServer.createContext("/history", new HistoryHandler(manager));
            httpServer.createContext("/prioritized", new PrioritizedHandler(manager));

            start();

            System.out.println("HTTP-сервер запущен на " + PORT + " порту!");

        } catch (IOException exception) {
            System.out.println("Проблема ввода/вывода данных");
        }
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
                .serializeNulls()
                .registerTypeAdapter(LocalDateTime.class, new DateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter());
        return gsonBuilder.create();
    }
}
