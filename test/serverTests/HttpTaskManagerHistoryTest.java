package serverTests;

import adapters.TasksListTypeToken;
import com.google.gson.Gson;
import enums.TaskStatus;
import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.HttpTaskServer;
import service.Manager;
import service.TaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskManagerHistoryTest {

    TaskManager manager = Manager.getDefault();
    Gson gson = HttpTaskServer.getGson();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    HttpClient client = HttpClient.newHttpClient();

    public HttpTaskManagerHistoryTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        manager.deleteAllTasks();
        manager.deleteAllSubtasks();
        manager.deleteAllEpics();
        HttpTaskServer.start();
    }

    @AfterEach
    public void shutDown() {
        HttpTaskServer.stop();
        client.close();
    }

    @Test
    public void getHistoryTest() throws IOException, InterruptedException {
        LocalDateTime start = LocalDateTime.of(2025, 2, 7, 10, 0);
        Duration duration = Duration.ofMinutes(30);
        Task task1 = manager.createTask(new Task("TASK1", "SOMETASK", TaskStatus.DONE, start, duration));
        Task task2 = manager.createTask(new Task("TASK2", "SOMETASK", TaskStatus.DONE));
        Epic epic1 = manager.createEpic(new Epic("EPIC1", "SOMETODO"));
        Subtask subtask1 = manager.createSubtask(new Subtask("SUBTASK1", "SOMETODO",
                TaskStatus.IN_PROGRESS, epic1.getId(), start.plusMinutes(90), duration));
        Subtask subtask2 = manager.createSubtask(new Subtask("SUBTASK2", "SOMETODO",
                TaskStatus.IN_PROGRESS, epic1.getId(), start.plusMinutes(120), duration));
        //список истории пуст
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        List<Task> returnedHistory = gson.fromJson(response.body(), new TasksListTypeToken().getType());
        assertEquals(manager.getHistory().size(), returnedHistory.size(), "Передано некорректное " +
                "количество задач в истории, должна быть пустой");

        //Заполняем историю вызовами тасков, сабтасков, эпиков
        manager.getTaskById(task1.getId());
        manager.getTaskById(task2.getId());
        manager.getSubtaskById(subtask1.getId());
        manager.getSubtaskById(subtask2.getId());
        manager.getEpicById(epic1.getId());
        //Получаем новые данные
        HttpResponse<String> response1 = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response1.statusCode());
        List<Task> returnedHistory1 = gson.fromJson(response1.body(), new TasksListTypeToken().getType());
        assertEquals(manager.getHistory().size(), returnedHistory1.size(), "Передано некорректное " +
                "количество задач в истории, должно быть 5");
        assertEquals(manager.getHistory().getFirst(), returnedHistory1.getFirst(), "Неверный порядок в истории");
    }

    @Test
    public void testWrongRequests() throws IOException, InterruptedException {
        //передаем неверный эндроинт (некорректный путь)
        URI url = URI.create("http://localhost:8080/history/123");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());

        //передаем неверный эндроинт (неверный метод)
        URI url1 = URI.create("http://localhost:8080/history");
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(url1)
                .DELETE()
                .build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response1.statusCode());
    }

}


