package server.handlers;

import com.sun.net.httpserver.HttpExchange;
import service.TaskManager;

public class SubtaskHandler extends BaseHttpHandler {

    public SubtaskHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange httpExchange){

        manager.getPrioritizedTasks();
    }
}
