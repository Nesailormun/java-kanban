package server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import service.TaskManager;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler {

    public HistoryHandler(TaskManager manager, Gson gson) {
        super(manager, gson);
    }

    @Override
    public void handle(HttpExchange httpExchange) {
        String[] pathParts = httpExchange.getRequestURI().getPath().split("/");
        String method = httpExchange.getRequestMethod();
        try {
            if (!method.equals("GET")) {
                sendBadRequest(httpExchange);
                return;
            }
            if (!(pathParts.length == 2)) {
                sendBadRequest(httpExchange);
                return;
            }
            String response = gson.toJson(manager.getHistory());
            sendText(httpExchange, response);
        } catch (IOException e) {
            sendServerError(httpExchange);
        }
    }
}
