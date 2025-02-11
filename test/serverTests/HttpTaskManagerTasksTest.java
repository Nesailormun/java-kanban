package serverTests;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import enums.TaskStatus;
import model.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
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

public class HttpTaskManagerTasksTest {

    TaskManager manager = Manager.getDefault();
    Gson gson = HttpTaskServer.getGson();
    HttpTaskServer taskServer = new HttpTaskServer(manager, gson);
    HttpClient client = HttpClient.newHttpClient();

    public HttpTaskManagerTasksTest() throws IOException {
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
    public void testAddTask() throws IOException, InterruptedException {

        Task task = new Task("Test", "Testing task",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(5));
        String taskJson = gson.toJson(task);
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        List<Task> tasksFromManager = manager.getAllTasks();
        assertNotNull(tasksFromManager.toString(), "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test", tasksFromManager.getFirst().getName(), "Некорректное имя задачи");

        // тестируем добавление задачи с пересекающимся временным интервалом, должен вернуть код 406
        LocalDateTime start = LocalDateTime.of(2025, 2, 10, 10, 0);
        Duration duration = Duration.ofMinutes(30);
        manager.createTask(new Task("TASK2", "SOMETASK", TaskStatus.DONE, start, duration));
        manager.createTask(new Task("TASK3", "SOMETASK", TaskStatus.DONE,
                start.plusMinutes(30), duration));
        Task task1 = new Task("Wrong task", "ERRROORR", start, duration);
        String taskJson1 = gson.toJson(task1);
        URI url1 = URI.create("http://localhost:8080/tasks");
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(url1)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson1))
                .build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response1.statusCode());
    }

    @Test
    public void testAddNull() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks");
        String nullTask = gson.toJson(null);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(nullTask))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testUpdateTask() throws IOException, InterruptedException {

        LocalDateTime start = LocalDateTime.of(2025, 2, 7, 10, 0);
        Duration duration = Duration.ofMinutes(30);
        manager.createTask(new Task("TASK1", "SOMETASK", TaskStatus.DONE, start, duration));
        manager.createTask(new Task("TASK2", "SOMETASK", TaskStatus.DONE,
                start.plusMinutes(30), duration));

        //обновляем существующий Таск новым корректным таском
        Task task = new Task(2, "NEWTASK 2", "Testing task 2",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(30));
        String taskJson = gson.toJson(task);
        URI url = URI.create("http://localhost:8080/tasks/2");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        assertEquals(task, manager.getTaskById(task.getId()), "Некорректно обновляются задачи");

        //тест на обновление таска, которого нет в базе, должен вернуть 404
        Task task1 = new Task(3, "NEWTASK 3", "Testing task 3",
                TaskStatus.NEW, start.plusMinutes(90), duration);
        String taskJson1 = gson.toJson(task1);
        URI url1 = URI.create("http://localhost:8080/tasks/3");
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(url1)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson1))
                .build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(2, manager.getAllTasks().size(), "Некорректно обновляются задачи");
        assertEquals(404, response1.statusCode());

        //тест обновления на таск с пересекающимся временным интервалом, Должен вернуть код 406
        Task task2 = new Task(2, "WRONGTASK", "Testing task 4",
                TaskStatus.NEW, start, duration);
        String taskJson2 = gson.toJson(task2);
        URI url2 = URI.create("http://localhost:8080/tasks/2");
        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(url2)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson2))
                .build();
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response2.statusCode());
    }

    @Test
    public void testDeleteTask() throws IOException, InterruptedException {
        LocalDateTime start = LocalDateTime.of(2025, 2, 7, 10, 0);
        Duration duration = Duration.ofMinutes(30);
        manager.createTask(new Task("TASK1", "SOMETASK", TaskStatus.DONE, start, duration));
        manager.createTask(new Task("TASK2", "SOMETASK", TaskStatus.DONE,
                start.plusMinutes(30), duration));
        URI url = URI.create("http://localhost:8080/tasks/2");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals(1, manager.getAllTasks().size(), "Задача не удалилась");

        //тест на удаление несуществующей задачи, должен вернуть 404
        URI url1 = URI.create("http://localhost:8080/tasks/34");
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(url1)
                .DELETE()
                .build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response1.statusCode());

    }

    @Test
    public void testGetTaskById() throws IOException, InterruptedException {
        LocalDateTime start = LocalDateTime.of(2025, 2, 10, 10, 0);
        Duration duration = Duration.ofMinutes(30);
        manager.createTask(new Task("TASK1", "SOMETASK", TaskStatus.DONE, start, duration));
        manager.createTask(new Task("TASK2", "SOMETASK", TaskStatus.DONE,
                start.plusMinutes(30), duration));
        URI url = URI.create("http://localhost:8080/tasks/2");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Task task = gson.fromJson(response.body(), Task.class);
        assertEquals(manager.getTaskById(2), task, "Возвращен некорректный таск");

        // Таска с id 345 нет. Должен вернуться код состояния 404 (Not Found)
        URI url1 = URI.create("http://localhost:8080/tasks/345");
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(url1)
                .GET()
                .build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response1.statusCode());
    }

    @Test
    public void testWrongRequests() throws IOException, InterruptedException {

        URI url = URI.create("http://localhost:8080/tasks/фывфы");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());
        assertEquals("Bad Request", response.body());

        URI url1 = URI.create("http://localhost:8080/tasks/OOOOLOL");
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(url1)
                .GET()
                .build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response1.statusCode());
        assertEquals("Bad Request", response1.body());

        String taskJson = gson.toJson(new Task("TEST", "TEST"));
        URI url2 = URI.create("http://localhost:8080/tasks/OOOOLOL");
        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(url2)
                .PUT(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response2.statusCode());
        assertEquals("Bad Request", response2.body());

        URI url3 = URI.create("http://localhost:8080/tasks/OOOOLOL");
        HttpRequest request3 = HttpRequest.newBuilder()
                .uri(url3)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> response3 = client.send(request3, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response3.statusCode());
        assertEquals("Bad Request", response3.body());

    }
}
