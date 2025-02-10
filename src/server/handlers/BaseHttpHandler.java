package server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import enums.Endpoint;
import service.TaskManager;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class BaseHttpHandler implements HttpHandler {

    protected final TaskManager manager;
    protected final Gson gson;
    protected static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    public BaseHttpHandler(TaskManager manager,Gson gson) {
        this.manager = manager;
        this.gson = gson;
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

    protected void sendBadRequest(HttpExchange h) throws IOException {
        byte[] resp = "Bad Request".getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        try (OutputStream os = h.getResponseBody()) {
            h.sendResponseHeaders(400, resp.length);
            os.write(resp);
        }
    }

    protected void sendModified(HttpExchange h, String text) throws IOException {
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


    protected void sendHasInteractions(HttpExchange h, String text)  {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        try (OutputStream os = h.getResponseBody()) {
            h.sendResponseHeaders(406, resp.length);
            os.write(resp);
        } catch (IOException e) {
            System.out.println("Ошибка при отправке ответа: 406(Not Acceptable)");
        }
    }

    protected void sendServerError(HttpExchange h) {
        byte[] resp = "Server Error".getBytes(StandardCharsets.UTF_8);
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