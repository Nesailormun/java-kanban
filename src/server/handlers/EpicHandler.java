package server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import enums.Endpoint;
import exceptions.NotFoundException;
import exceptions.NullEqualsException;
import model.Epic;
import service.TaskManager;

import java.io.IOException;
import java.io.InputStream;

public class EpicHandler extends BaseHttpHandler {

    public EpicHandler(TaskManager manager, Gson gson) {
        super(manager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            Endpoint endpoint = getEndpoint(exchange.getRequestMethod());
            String[] pathParts = exchange.getRequestURI().getPath().split("/");

            if (endpoint.equals(Endpoint.UNKNOWN) || (pathParts.length >= 3 && getTaskId(pathParts).isEmpty())) {
                sendBadRequest(exchange);
                return;
            }
            switch (endpoint) {
                case GET: {
                    if (pathParts.length == 2) {
                        handleGetAllEpics(exchange);
                        break;
                    } else if (pathParts.length == 3) {
                        handleGetEpicById(exchange, getTaskId(pathParts).get());
                        break;
                    } else if (pathParts.length == 4 && pathParts[pathParts.length - 1].equals("subtasks")) {
                        handleGetEpicsSubtasks(exchange, getTaskId(pathParts).get());
                        break;
                    }
                    sendBadRequest(exchange);
                    break;
                }
                case POST: {
                    if (pathParts.length == 2) {
                        handleCreateEpic(exchange);
                        break;
                    } else if (pathParts.length == 3) {
                        handleUpdateEpic(exchange, getTaskId(pathParts).get());
                        break;
                    }
                    sendBadRequest(exchange);
                    break;
                }
                case DELETE: {
                    if (pathParts.length == 3) {
                        handleDeleteEpic(exchange, getTaskId(pathParts).get());
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

    private void handleGetEpicById(HttpExchange exchange, Integer taskId) {
        try {
            String response = gson.toJson(manager.getEpicById(taskId));
            sendText(exchange, response);
        } catch (NotFoundException exception) {
            sendNotFound(exchange, "Эпика с идентификатором: " + taskId + " не существует!");
        } catch (IOException e) {
            sendServerError(exchange);
        }
    }

    private void handleGetAllEpics(HttpExchange exchange) {
        try {
            String response = gson.toJson(manager.getAllEpics());
            sendText(exchange, response);
        } catch (IOException e) {
            sendServerError(exchange);
        }
    }

    private void handleGetEpicsSubtasks(HttpExchange exchange, Integer epicId) {
        try {
            String response = gson.toJson(manager.getEpicsSubtasks(manager.getEpicById(epicId)));
            sendText(exchange, response);
        } catch (IOException e) {
            sendServerError(exchange);
        } catch (NotFoundException | NullEqualsException e) {
            sendNotFound(exchange, e.getMessage());
        }
    }

    private void handleDeleteEpic(HttpExchange exchange, Integer taskId) {
        try {
            manager.removeEpic(taskId);
            String response = "Эпик с идентификатором: " + taskId + " удален!";
            sendText(exchange, response);
        } catch (IOException e) {
            sendServerError(exchange);
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        }
    }

    private void handleCreateEpic(HttpExchange exchange) {
        try {
            InputStream inputStream = exchange.getRequestBody();
            String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
            Epic epic = gson.fromJson(body, Epic.class);
            manager.createEpic(epic);
            String response = "Эпик с идентификатором: " + epic.getId() + " успешно добавлен!";
            sendModified(exchange, response);
        } catch (IOException e) {
            sendServerError(exchange);
        } catch (NullEqualsException e) {
            sendNotFound(exchange, e.getMessage());
        }
    }

    private void handleUpdateEpic(HttpExchange exchange, Integer taskId) {
        try {
            InputStream inputStream = exchange.getRequestBody();
            String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
            Epic epic = gson.fromJson(body, Epic.class);
            manager.updateEpic(epic);
            String response = "Эпик с идентификатором: " + taskId + " успешно обновлен!";
            sendModified(exchange, response);
        } catch (IOException e) {
            sendServerError(exchange);
        } catch (NullEqualsException | NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        }
    }
}
