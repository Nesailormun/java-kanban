package server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import service.TaskManager;

import java.io.IOException;

public class PrioritizedHandler extends BaseHttpHandler {

    public PrioritizedHandler(TaskManager manager, Gson gson) {
        super(manager, gson);
    }

    @Override
    public void handle(HttpExchange httpExchange) {
        String[] pathParts = httpExchange.getRequestURI().getPath().split("/");
        String method = httpExchange.getRequestMethod();
        try {
            if ((!method.equals("GET")) || !(pathParts.length == 2)) {
                sendBadRequest(httpExchange);
                return;
            }
            String response = gson.toJson(manager.getPrioritizedTasks());
            sendText(httpExchange, response);
        } catch (IOException e) {
            sendServerError(httpExchange);
        }
    }
}
