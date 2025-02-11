package serverTests;

import com.google.gson.Gson;
import enums.TaskStatus;
import adapters.TasksListTypeToken;
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

public class HttpTaskManagerPrioritizedTest {

    TaskManager manager = Manager.getDefault();
    Gson gson = HttpTaskServer.getGson();
    HttpTaskServer taskServer = new HttpTaskServer(manager, gson);
    HttpClient client = HttpClient.newHttpClient();

    public HttpTaskManagerPrioritizedTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        HttpTaskServer.start();
    }

    @AfterEach
    public void shutDown() {
        client.close();
        HttpTaskServer.stop();
    }

    @Test
    public void testGetPrioritizedList() throws IOException, InterruptedException {

        LocalDateTime start = LocalDateTime.of(2025, 2, 7, 10, 0);
        Duration duration = Duration.ofMinutes(30);
        manager.createTask(new Task("TASK1", "SOMETASK", TaskStatus.DONE, start, duration));
        manager.createTask(new Task("TASK2", "SOMETASK", TaskStatus.DONE,
                start.plusMinutes(30), duration));
        manager.createTask(new Task("TASK3", "SOMETASK", TaskStatus.DONE,
                start.plusMinutes(60), duration));
        manager.createTask(new Task("TASK4", "SOMETASK", TaskStatus.DONE));

        manager.createEpic(new Epic(5, "EPIC1", "SOMETODO"));
        manager.createEpic(new Epic("EPIC2", "SOMETODO"));

        manager.createSubtask(new Subtask("SUBTASK1", "SOMETODO", TaskStatus.IN_PROGRESS, 5,
                start.plusMinutes(90), duration));
        manager.createSubtask(new Subtask("SUBTASK2", "SOMETODO", TaskStatus.IN_PROGRESS, 5,
                start.plusMinutes(120), duration));
        manager.createSubtask(new Subtask("SUBTASK3", "SOMETODO", TaskStatus.IN_PROGRESS, 6,
                start.plusMinutes(150), duration));

        List<Task> primePrioritizedTasks = manager.getPrioritizedTasks();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> returnedPrioritizedTasks = gson.fromJson(response.body(), new TasksListTypeToken().getType());
        assertEquals(primePrioritizedTasks.size(), returnedPrioritizedTasks.size(), "Передано некорректное " +
                "количество задач в списке приоритетных задач");

        assertEquals(primePrioritizedTasks.getFirst(), returnedPrioritizedTasks.getFirst(), "Возвращается " +
                "список приоритетных задач с неверным порядком");
        //удаляем все задачи и делаем повторный запрос
        manager.deleteAllEpics();
        manager.deleteAllTasks();

        HttpResponse<String> response1 = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response1.statusCode());
        List<Task> emptyReturnedPrioritizedTasks = gson.fromJson(response1.body(), new TasksListTypeToken().getType());
        assertEquals(0, emptyReturnedPrioritizedTasks.size(), "Список приоритетных задач должен быть" +
                " пуст");
    }

    @Test
    public void testWrongRequests() throws IOException, InterruptedException {
        //передаем неверный эндроинт (некорректный путь)
        URI url = URI.create("http://localhost:8080/prioritized/123");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());

        //передаем неверный эндроинт (неверный метод)
        URI url1 = URI.create("http://localhost:8080/prioritized");
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(url1)
                .DELETE()
                .build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response1.statusCode());
    }
}
