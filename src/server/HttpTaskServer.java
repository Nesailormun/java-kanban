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

    public static void main(String[] args) throws IOException, InterruptedException {

        HttpTaskServer httpTaskServer = new HttpTaskServer(Manager.getDefault(), HttpTaskServer.getGson());
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
