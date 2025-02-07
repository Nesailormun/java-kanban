package server.handlers;

import com.sun.net.httpserver.HttpExchange;
import service.TaskManager;

import java.io.OutputStream;

public class PrioritizedHandler extends BaseHttpHandler {

    public PrioritizedHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange httpExchange) {

    }
}
