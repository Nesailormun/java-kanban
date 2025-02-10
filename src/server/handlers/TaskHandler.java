package server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import enums.Endpoint;
import exceptions.DateTimeIntersectionException;
import exceptions.NotFoundException;
import exceptions.NullEqualsException;
import model.Task;
import service.TaskManager;

import java.io.IOException;
import java.io.InputStream;

public class TaskHandler extends BaseHttpHandler {

    public TaskHandler(TaskManager manager, Gson gson) {
        super(manager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            Endpoint endpoint = getEndpoint(exchange.getRequestMethod());
            String[] pathParts = exchange.getRequestURI().getPath().split("/");

            if (endpoint.equals(Endpoint.UNKNOWN) || (pathParts.length == 3 && getTaskId(pathParts).isEmpty())) {
                sendBadRequest(exchange);
                return;
            }
            switch (endpoint) {
                case GET: {
                    if (pathParts.length == 2) {
                        handleGetAllTasks(exchange);
                        break;
                    } else if (pathParts.length == 3) {
                        handleGetTaskById(exchange, getTaskId(pathParts).get());
                        break;
                    }
                    sendBadRequest(exchange);
                    break;
                }
                case POST: {
                    if (pathParts.length == 2) {
                        handleCreateTask(exchange);
                        break;
                    } else if (pathParts.length == 3) {
                        handleUpdateTask(exchange, getTaskId(pathParts).get());
                        break;
                    }
                    sendBadRequest(exchange);
                    break;
                }
                case DELETE: {
                    if (pathParts.length == 3) {
                        handleDeleteTask(exchange, getTaskId(pathParts).get());
                        break;
                    }
                    sendBadRequest(exchange);
                    break;
                }
            }
        } catch (IOException exception) {
            sendServerError(exchange);
        }
    }

    private void handleGetTaskById(HttpExchange exchange, Integer taskId) {
        try {
            String response = gson.toJson(manager.getTaskById(taskId));
            sendText(exchange, response);
        } catch (NotFoundException exception) {
            sendNotFound(exchange, "Таска с идентификатором: " + taskId + " не существует!");
        } catch (IOException e) {
            sendServerError(exchange);
        }
    }

    private void handleGetAllTasks(HttpExchange exchange) {
        try {
            String response = gson.toJson(manager.getAllTasks());
            sendText(exchange, response);
        } catch (IOException e) {
            sendServerError(exchange);
        }
    }

    private void handleDeleteTask(HttpExchange exchange, Integer taskId) {
        try {
            manager.removeTask(taskId);
            String response = "Таск с идентификатором: " + taskId + " удален!";
            sendText(exchange, response);
        } catch (IOException e) {
            sendServerError(exchange);
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        }
    }

    private void handleCreateTask(HttpExchange exchange) {
        try {
            InputStream inputStream = exchange.getRequestBody();
            String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
            Task task = gson.fromJson(body, Task.class);
            manager.createTask(task);
            String response = "Таск с идентификатором: " + task.getId() + " успешно добавлен!";
            sendModified(exchange, response);
        } catch (IOException e) {
            sendServerError(exchange);
        } catch (NullEqualsException e) {
            sendNotFound(exchange, e.getMessage());
        } catch (DateTimeIntersectionException e) {
            sendHasInteractions(exchange, e.getMessage());
        }
    }

    private void handleUpdateTask(HttpExchange exchange, Integer taskId) {
        try {
            InputStream inputStream = exchange.getRequestBody();
            String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
            Task task = gson.fromJson(body, Task.class);
            manager.updateTask(task);
            String response = "Таск с идентификатором: " + taskId + " успешно обновлен!";
            sendModified(exchange, response);
        } catch (IOException e) {
            sendServerError(exchange);
        } catch (NullEqualsException | NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        } catch (DateTimeIntersectionException e) {
            sendHasInteractions(exchange, e.getMessage());
        }
    }
}
