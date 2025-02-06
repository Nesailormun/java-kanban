package server.handlers;

import com.sun.net.httpserver.HttpExchange;
import enums.Endpoint;

import java.util.Optional;

public class TaskHandler extends BaseHttpHandler {

    @Override
    public void handle(HttpExchange exchange){

        Endpoint endpoint = getEndpoint(exchange.getRequestMethod());
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        Optional<Integer> taskId = getTaskId(exchange);

        switch (endpoint){
            case GET:
                if(pathParts.length == 2)
                    handleGetAllTasks(exchange);
                else if (pathParts.length == 3 && taskId.isPresent())
                    handleGetTaskById(exchange);
            case POST:
                if (pathParts.length == 2)
                    handleCreateTask(exchange);
                else if (getTaskId(exchange).isPresent()) {
                    handleUpdateTask(exchange);
                }
            case DELETE:
                if (pathParts.length == 3 && taskId.isPresent())
                    handleDeleteTask(exchange);
            default:

        }

    }

    private void handleGetTaskById(HttpExchange exchange) {

    }

    private void handleGetAllTasks(HttpExchange exchange){

    }

    private  void handleDeleteTask(HttpExchange exchange){

    }

    private void handleCreateTask(HttpExchange exchange){

    }

    private void handleUpdateTask(HttpExchange exchange){

    }



}
