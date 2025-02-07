package server.handlers;

import com.sun.net.httpserver.HttpExchange;
import enums.Endpoint;
import exceptions.NotFoundException;
import model.Task;
import service.TaskManager;


import java.io.IOException;
import java.io.InputStream;

public class TaskHandler extends BaseHttpHandler {


    public TaskHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) {
        Endpoint endpoint = getEndpoint(exchange.getRequestMethod());
        String[] pathParts = exchange.getRequestURI().getPath().split("/");

        switch (endpoint) {
            case GET: {
                if (pathParts.length == 2) {
                    handleGetAllTasks(exchange);
                    break;
                }
                else if (pathParts.length == 3 && getTaskId(pathParts).isPresent()) {
                        handleGetTaskById(exchange, getTaskId(pathParts).get());
                        break;
                }
                System.out.println("Такого эндпоинта не существует");
                break;
            }
            case POST: {
                if (pathParts.length == 2) {
                    handleCreateTask(exchange);
                    break;
                } else if (pathParts.length == 3 && getTaskId(pathParts).isPresent()) {
                    handleUpdateTask(exchange);
                    break;
                }
                System.out.println("Такого эндпоинта не существует");
                break;
            }
            case DELETE: {
                if (pathParts.length == 3 && getTaskId(pathParts).isPresent()) {
                    handleDeleteTask(exchange, getTaskId(pathParts).get());
                    break;
                }
                System.out.println("Такого эндпоинта не существует");
                break;
            }
            default:
                System.out.println("Такого эндпоинта не существует");
                break;
        }
    }

    private void handleGetTaskById(HttpExchange exchange, Integer taskId) {
        try {
            String response = gson.toJson(manager.getTaskById(taskId));
            sendText(exchange, response);

        } catch (NotFoundException exception) {
            sendNotFound(exchange, "Таска с идентификатором: " + taskId + " не существует!");
        } catch (IOException e) {
            System.out.println("Ошибка отправки ответа.");
        }
    }

    private void handleGetAllTasks(HttpExchange exchange) {
        try {
            String response = gson.toJson(manager.getAllTasks().toString());
            sendText(exchange, response);
        } catch (IOException e) {
            System.out.println("Ошибка отправки ответа.");
        }
    }

    private void handleDeleteTask(HttpExchange exchange, Integer taskId) {
        try {
            manager.removeTask(taskId);
            String response = "Таск с идентификатором: " + taskId + " удалена!";
            sendText(exchange, response);
        }
        catch (IOException e){
            System.out.println("Ошибка отправки ответа.");
        }
    }

    private void handleCreateTask(HttpExchange exchange) {
        try {
            InputStream inputStream = exchange.getRequestBody();
            String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
            Task task = gson.fromJson(body, Task.class);
            if (manager.createTask(task) == null){
                sendHasInteractions(exchange);
            }
            manager.createTask(task);
            String response = "Таск с идентификатором: " + task.getId() + " успешно добавлен!";
            sendCreated(exchange, response);
        }
        catch (IOException e){
            sendServerError(exchange);
        }
    }

    private void handleUpdateTask(HttpExchange exchange) {
        try {
            InputStream inputStream = exchange.getRequestBody();
            String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
            Task task = gson.fromJson(body, Task.class);
            manager.updateTask(task);
            String response = "Таск с идентификатором: " + task.getId() + " успешно обновлен!";
            sendCreated(exchange, response);
        }
        catch (IOException e){
            sendServerError(exchange);
        }
    }

}
