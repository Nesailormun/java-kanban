package server;

import com.sun.net.httpserver.HttpServer;
import server.handlers.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class HttpTaskServer {

    private static final int PORT = 8080;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private static HttpServer httpServer;


    public static void main(String[] args) throws IOException {

        httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);

        httpServer.createContext("/tasks", new TaskHandler());
        httpServer.createContext("/subtasks", new SubtaskHandler());
        httpServer.createContext("/epics", new EpicHandler());
        httpServer.createContext("/history", new HistoryHandler());
        httpServer.createContext("/prioritized", new PrioritizedHandler());

        start();

        System.out.println("HTTP-сервер запущен на " + PORT + " порту!");

    }

    public static void start(){
        httpServer.start();
    }

    public static void stop(){
        httpServer.stop(0);
    }
}
