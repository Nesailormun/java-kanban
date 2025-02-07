package server.handlers;

import com.sun.net.httpserver.HttpExchange;
import service.TaskManager;

public class EpicHandler extends BaseHttpHandler {

    public EpicHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange httpExchange){

        manager.getPrioritizedTasks();
    }


}
