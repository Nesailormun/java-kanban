package server.handlers;

import com.sun.net.httpserver.HttpExchange;
import service.TaskManager;

public class HistoryHandler extends BaseHttpHandler {

    public HistoryHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange httpExchange){

        manager.getPrioritizedTasks();
    }
}
