package server.handlers;

import com.sun.net.httpserver.HttpExchange;

public class EpicHandler extends BaseHttpHandler {

    @Override
    public void handle(HttpExchange httpExchange){

        manager.getPrioritizedTasks();
    }
}
