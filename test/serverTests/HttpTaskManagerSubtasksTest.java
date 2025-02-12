package serverTests;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import enums.TaskStatus;
import model.Epic;
import model.Subtask;
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

public class HttpTaskManagerSubtasksTest {
    TaskManager manager = Manager.getDefault();
    Gson gson = HttpTaskServer.getGson();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    HttpClient client = HttpClient.newHttpClient();

    public HttpTaskManagerSubtasksTest() throws IOException {
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
    public void testAddSubtask() throws IOException, InterruptedException {

        manager.createEpic(new Epic("epic1", "SomeEpic1"));
        Subtask subtask = new Subtask("Test", "Testing subtask",
                TaskStatus.NEW, 1, LocalDateTime.now(), Duration.ofMinutes(5));
        String taskJson = gson.toJson(subtask);
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        List<Subtask> subtasksFromManager = manager.getAllSubtasks();
        assertNotNull(subtasksFromManager.toString(), "Задачи не возвращаются");
        assertEquals(1, subtasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test", subtasksFromManager.getFirst().getName(), "Некорректное имя задачи");

        //тестируем добавление сабтаска с пересекающимся временным интервалом, должен быть код 406
        Subtask subtask1 = new Subtask("WROOONNG", "SOMETHING WROOONNG", TaskStatus.DONE, 1,
                subtask.getStartTime(), Duration.ofMinutes(15));
        String taskJson1 = gson.toJson(subtask1);
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson1))
                .build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response1.statusCode());

        // тестируем добавление сабтаска с неверным эпик айди, должен вернуть 404
        Subtask subtask2 = new Subtask("WROOONNG", "SOMETHING WROOONNG", TaskStatus.NEW, 34,
                subtask.getStartTime().plusMinutes(30), Duration.ofMinutes(15));
        String taskJson2 = gson.toJson(subtask2);
        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson2))
                .build();
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response2.statusCode());
    }

    @Test
    public void testAddNull() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/subtasks");
        String nullSubtask = gson.toJson(null);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(nullSubtask))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testUpdateSubtask() throws IOException, InterruptedException {

        LocalDateTime start = LocalDateTime.of(2025, 2, 7, 10, 0);
        Duration duration = Duration.ofMinutes(30);
        Epic epic1 = manager.createEpic(new Epic("epic1", "SomeEpic1"));
        manager.createSubtask(new Subtask("SUBTASK1", "SOMETASK", TaskStatus.DONE, 1,
                start, duration));
        manager.createSubtask(new Subtask("SUBTASK2", "SOMETASK", TaskStatus.DONE, 1,
                start.plusMinutes(30), duration));

        //обновляем существующий Сабтаск новым корректным сабтаском
        Subtask subtask = new Subtask(2, "NEWSUBTASK 1", "WOWOWOW",
                TaskStatus.IN_PROGRESS, 1, LocalDateTime.now(), Duration.ofMinutes(30));
        String subtaskJson = gson.toJson(subtask);
        URI url = URI.create("http://localhost:8080/subtasks/2");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        assertEquals(subtask, manager.getSubtaskById(subtask.getId()), "Некорректно обновляются задачи");
        assertEquals(2, manager.getAllSubtasks().size(), "Некорректно обновляются задачи");
        assertEquals(TaskStatus.IN_PROGRESS, epic1.getStatus(), "Некорректно обновляются задачи(статус Эпика)");

        //тест на обновление сабтаска, которого нет в базе, должен вернуть 404
        Subtask subtask1 = new Subtask(4, "NEWSUBTASK 2", "SUBTAAAAASSKKK",
                TaskStatus.NEW, 1, start.plusMinutes(90), duration);
        String taskJson1 = gson.toJson(subtask1);
        URI url1 = URI.create("http://localhost:8080/subtasks/4");
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(url1)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson1))
                .build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response1.statusCode());

        //тест обновления на сабтаск с пересекающимся временным интервалом, Должен вернуть код 406
        Subtask subtask2 = new Subtask(3, "NEWSUBTASK 2", "SUBTAAAAASSKKK",
                TaskStatus.NEW, 1, subtask.getStartTime(), duration);
        String taskJson2 = gson.toJson(subtask2);
        URI url2 = URI.create("http://localhost:8080/subtasks/3");
        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(url2)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson2))
                .build();
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response2.statusCode());

        //тест обновления сабтаска на сабтаск с неверным эпик айди, вернет 404
        Subtask subtask3 = new Subtask(3, "NEWSUBTASK 2", "SUBTAAAAASSKKK",
                TaskStatus.NEW, 2, subtask.getStartTime().plusMinutes(30), duration);
        String taskJson3 = gson.toJson(subtask3);
        URI url3 = URI.create("http://localhost:8080/subtasks/3");
        HttpRequest request3 = HttpRequest.newBuilder()
                .uri(url3)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson3))
                .build();
        HttpResponse<String> response3 = client.send(request3, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response3.statusCode());
    }

    @Test
    public void testDeleteSubtask() throws IOException, InterruptedException {
        LocalDateTime start = LocalDateTime.of(2025, 2, 7, 10, 0);
        Duration duration = Duration.ofMinutes(30);
        Epic epic1 = manager.createEpic(new Epic("epic1", "SomeEpic1"));
        manager.createSubtask(new Subtask("SUBTASK1", "SOMETASK", TaskStatus.DONE, 1,
                start, duration));
        manager.createSubtask(new Subtask("SUBTASK2", "SOMETASK", TaskStatus.NEW, 1,
                start.plusMinutes(30), duration));

        URI url = URI.create("http://localhost:8080/subtasks/2");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Ошибка удаления сабтаска");
        assertEquals(TaskStatus.NEW, epic1.getStatus(), "Ошибка удаления сабтаска (не поменялся статус эпика)");

        //тест на удаление несуществующей задачи, должен вернуть 404
        URI url1 = URI.create("http://localhost:8080/subtasks/34");
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(url1)
                .DELETE()
                .build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response1.statusCode());

    }

    @Test
    public void testGetTaskById() throws IOException, InterruptedException {
        LocalDateTime start = LocalDateTime.of(2025, 2, 7, 10, 0);
        Duration duration = Duration.ofMinutes(30);
        Epic epic1 = manager.createEpic(new Epic("epic1", "SomeEpic1"));
        manager.createSubtask(new Subtask("SUBTASK1", "SOMETASK", TaskStatus.DONE, 1,
                start, duration));
        manager.createSubtask(new Subtask("SUBTASK2", "SOMETASK", TaskStatus.NEW, 1,
                start.plusMinutes(30), duration));

        URI url = URI.create("http://localhost:8080/subtasks/2");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Subtask subtask = gson.fromJson(response.body(), Subtask.class);
        assertEquals(manager.getSubtaskById(2), subtask, "Возвращен некорректный сабтаск");

        // Сабтаска с id 345 нет. Должен вернуться код состояния 404 (Not Found)
        URI url1 = URI.create("http://localhost:8080/subtasks/345");
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(url1)
                .GET()
                .build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response1.statusCode());
    }

    @Test
    public void testWrongRequests() throws IOException, InterruptedException {

        URI url = URI.create("http://localhost:8080/subtasks/фывфы");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());
        assertEquals("Bad Request", response.body());

        URI url1 = URI.create("http://localhost:8080/subtasks/OOOOLOL");
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(url1)
                .GET()
                .build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response1.statusCode());
        assertEquals("Bad Request", response1.body());

        String subtaskJson = gson.toJson(new Subtask("TEST", "TEST", TaskStatus.DONE, 1));
        URI url2 = URI.create("http://localhost:8080/tasks/OOOOLOL");
        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(url2)
                .PUT(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response2.statusCode());
        assertEquals("Bad Request", response2.body());

        URI url3 = URI.create("http://localhost:8080/tasks/OOOOLOL");
        HttpRequest request3 = HttpRequest.newBuilder()
                .uri(url3)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();
        HttpResponse<String> response3 = client.send(request3, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response3.statusCode());
        assertEquals("Bad Request", response3.body());

    }
}

