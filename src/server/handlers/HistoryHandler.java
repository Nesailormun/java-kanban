package server.handlers;

import com.sun.net.httpserver.HttpExchange;

public class HistoryHandler extends BaseHttpHandler {

    @Override
    public void handle(HttpExchange httpExchange){

        manager.getPrioritizedTasks();
    }
}
