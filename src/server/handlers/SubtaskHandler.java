package server.handlers;

import com.sun.net.httpserver.HttpExchange;

public class SubtaskHandler extends BaseHttpHandler {

    @Override
    public void handle(HttpExchange httpExchange){

        manager.getPrioritizedTasks();
    }
}
