package server.handlers;

import adapters.DateTimeAdapter;
import adapters.DurationAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import enums.Endpoint;
import service.TaskManager;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

public class BaseHttpHandler implements HttpHandler {

    protected final TaskManager manager;
    protected final Gson gson;
    protected static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    public BaseHttpHandler(TaskManager manager) {
        this.manager = manager;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .registerTypeAdapter(LocalDateTime.class, new DateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
    }


    @Override
    public void handle(HttpExchange exchange) {

    }

    protected void sendText(HttpExchange h, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        try (OutputStream os = h.getResponseBody()) {
            h.sendResponseHeaders(200, resp.length);
            os.write(resp);
        }
    }

    protected void sendCreated(HttpExchange h, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(201, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendNotFound(HttpExchange h, String text) {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        try (OutputStream os = h.getResponseBody()) {
            h.sendResponseHeaders(404, resp.length);
            os.write(resp);
        } catch (IOException e) {
            System.out.println("Ошибка при отправке ответа: 404(NotFound)");
        }
    }


    protected void sendHasInteractions(HttpExchange h) throws IOException {
        byte[] resp = "Задача пересекается с существующими!".getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(406, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendServerError(HttpExchange h) {
        byte[] resp = "Ошибка обработки запроса".getBytes(StandardCharsets.UTF_8);
        try (OutputStream os = h.getResponseBody()) {
            h.sendResponseHeaders(500, resp.length);
            os.write(resp);
        } catch (IOException e) {
            System.out.println("Ошибка при отправке ответа: 500(Server Error)");
        }
    }

    protected Endpoint getEndpoint(String requestMethod) {
        switch (requestMethod) {
            case "GET":
                return Endpoint.GET;
            case "POST":
                return Endpoint.POST;
            case "DELETE":
                return Endpoint.DELETE;
            default:
                return Endpoint.UNKNOWN;
        }
    }

        protected Optional<Integer> getTaskId (String[] pathParts){
            try {
                return Optional.of(Integer.parseInt(pathParts[2]));
            } catch (NumberFormatException exception) {
                System.out.println("Неверный формат идентификатора");
                return Optional.empty();
            }
        }
    }