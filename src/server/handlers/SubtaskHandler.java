package server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import enums.Endpoint;
import exceptions.DateTimeIntersectionException;
import exceptions.NotFoundException;
import exceptions.NullEqualsException;
import model.Subtask;
import service.TaskManager;

import java.io.IOException;
import java.io.InputStream;

public class SubtaskHandler extends BaseHttpHandler {

    public SubtaskHandler(TaskManager manager, Gson gson) {
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
                        handleGetAllSubtasks(exchange);
                        break;
                    } else if (pathParts.length == 3) {
                        handleGetSubtaskById(exchange, getTaskId(pathParts).get());
                        break;
                    }
                    sendBadRequest(exchange);
                    break;
                }
                case POST: {
                    if (pathParts.length == 2) {
                        handleCreateSubtask(exchange);
                        break;
                    } else if (pathParts.length == 3) {
                        handleUpdateSubtask(exchange, getTaskId(pathParts).get());
                        break;
                    }
                    sendBadRequest(exchange);
                    break;
                }
                case DELETE: {
                    if (pathParts.length == 3) {
                        handleDeleteSubtask(exchange, getTaskId(pathParts).get());
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

    private void handleGetSubtaskById(HttpExchange exchange, Integer taskId) {
        try {
            String response = gson.toJson(manager.getSubtaskById(taskId));
            sendText(exchange, response);
        } catch (NotFoundException exception) {
            sendNotFound(exchange, "Сабтаска с идентификатором: " + taskId + " не существует!");
        } catch (IOException e) {
            sendServerError(exchange);
        }
    }

    private void handleGetAllSubtasks(HttpExchange exchange) {
        try {
            String response = gson.toJson(manager.getAllSubtasks());
            sendText(exchange, response);
        } catch (IOException e) {
            sendServerError(exchange);
        }
    }

    private void handleDeleteSubtask(HttpExchange exchange, Integer taskId) {
        try {
            manager.removeSubtask(taskId);
            String response = "Сабтаск с идентификатором: " + taskId + " удален!";
            sendText(exchange, response);
        } catch (IOException e) {
            sendServerError(exchange);
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        }
    }

    private void handleCreateSubtask(HttpExchange exchange) {
        try {
            InputStream inputStream = exchange.getRequestBody();
            String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
            Subtask subtask = gson.fromJson(body, Subtask.class);
            manager.createSubtask(subtask);
            String response = "Сабаск с идентификатором: " + subtask.getId() + " успешно добавлен!";
            sendModified(exchange, response);
        } catch (IOException e) {
            sendServerError(exchange);
        } catch (NullEqualsException e) {
            sendNotFound(exchange, e.getMessage());
        } catch (DateTimeIntersectionException e) {
            sendHasInteractions(exchange, e.getMessage());
        }
    }

    private void handleUpdateSubtask(HttpExchange exchange, Integer taskId) {
        try {
            InputStream inputStream = exchange.getRequestBody();
            String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
            Subtask subtask = gson.fromJson(body, Subtask.class);
            if (!(subtask.getId() == taskId))
                throw new NotFoundException("Exception! Id in request not equals id of subtask!");
            manager.updateSubtask(subtask);
            String response = "Сабтаск с идентификатором: " + subtask.getId() + " успешно обновлен!";
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
