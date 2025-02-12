package serverTests;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import enums.TaskStatus;
import model.Epic;
import model.Subtask;
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

class SubtasksListTypeToken extends TypeToken<List<Subtask>> {
}

public class HttpTaskManagerEpicsTest {

    TaskManager manager = Manager.getDefault();
    Gson gson = HttpTaskServer.getGson();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    HttpClient client = HttpClient.newHttpClient();

    public HttpTaskManagerEpicsTest() throws IOException {
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
    public void testAddEpic() throws IOException, InterruptedException {

        Epic epic = new Epic("Test", "Testing epic");
        String taskJson = gson.toJson(epic);
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        List<Epic> epicsFromManager = manager.getAllEpics();
        assertNotNull(epicsFromManager.toString(), "Эпики не возвращаются");
        assertEquals(1, epicsFromManager.size(), "Некорректное количество эпиков");
        assertEquals("Test", epicsFromManager.getFirst().getName(), "Некорректное имя эпика");
        assertEquals(0, manager.getEpicsSubtasks(epicsFromManager.getFirst()).size(),
                "Некорректное количество сабтасков у эпика");
    }

    @Test
    public void testAddNull() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/epics");
        String nullEpic = gson.toJson(null);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(nullEpic))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testDeleteEpic() throws IOException, InterruptedException {
        LocalDateTime start = LocalDateTime.of(2025, 2, 7, 10, 0);
        Duration duration = Duration.ofMinutes(30);
        manager.createEpic(new Epic("EPIC1", "SOMEOFEPIC1"));
        manager.createSubtask(new Subtask("SUBTASK1", "SOMETASK", TaskStatus.DONE, 1,
                start, duration));
        manager.createSubtask(new Subtask("SUBTASK2", "SOMETASK", TaskStatus.DONE, 1,
                start.plusMinutes(30), duration));
        URI url = URI.create("http://localhost:8080/epics/1");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals(0, manager.getAllEpics().size(), "Эпик удалился некорректно");
        assertEquals(0, manager.getAllSubtasks().size(), "Эпик удалился некорректно" +
                " (сабтаски остались)");

        //тест на удаление несуществующей задачи, должен вернуть 404
        URI url1 = URI.create("http://localhost:8080/epics/34");
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(url1)
                .DELETE()
                .build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response1.statusCode());
    }

    @Test
    public void testGetEpicById() throws IOException, InterruptedException {
        LocalDateTime start = LocalDateTime.of(2025, 2, 10, 10, 0);
        Duration duration = Duration.ofMinutes(30);
        manager.createEpic(new Epic("EPIC1", "SOMEOFEPIC1"));
        manager.createSubtask(new Subtask("SUBTASK1", "SOMETASK", TaskStatus.DONE, 1,
                start, duration));
        manager.createSubtask(new Subtask("SUBTASK2", "SOMETASK", TaskStatus.DONE, 1,
                start.plusMinutes(30), duration));

        URI url = URI.create("http://localhost:8080/epics/1");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        Epic epic = gson.fromJson(response.body(), Epic.class);
        assertEquals(manager.getEpicById(1), epic, "Возвращен некорректный эпик");

        // Эпика с id 345 нет. Должен вернуться код состояния 404 (Not Found)
        URI url1 = URI.create("http://localhost:8080/epic/345");
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(url1)
                .GET()
                .build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response1.statusCode());
    }

    @Test
    public void testGetEpicsSubtasks() throws IOException, InterruptedException {

        LocalDateTime start = LocalDateTime.of(2025, 2, 10, 10, 0);
        Duration duration = Duration.ofMinutes(30);
        Epic epicPrime = manager.createEpic(new Epic("EPIC1", "SOMEOFEPIC1"));
        manager.createSubtask(new Subtask("SUBTASK1", "SOMETASK", TaskStatus.DONE, 1,
                start, duration));
        manager.createSubtask(new Subtask("SUBTASK2", "SOMETASK", TaskStatus.DONE, 1,
                start.plusMinutes(30), duration));

        List<Subtask> primeSubtasksOfEpic = manager.getEpicsSubtasks(epicPrime);
        URI url = URI.create("http://localhost:8080/epics/1/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        List<Subtask> returnedSubtasksOfEpic = gson.fromJson(response.body(), new SubtasksListTypeToken().getType());
        assertEquals(primeSubtasksOfEpic.size(), returnedSubtasksOfEpic.size(), "Передано некорректное " +
                "количество сабтасков у эпика");
        boolean isListsOfSubtasksEquals = returnedSubtasksOfEpic.containsAll(primeSubtasksOfEpic);
        assertTrue(isListsOfSubtasksEquals, "Списки сабтасков не совпадают");

        //тестируем получение сабтасков несуществующего эпика, должен вернуть 404
        URI url1 = URI.create("http://localhost:8080/epics/2/subtasks");
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(url1)
                .GET()
                .build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response1.statusCode());
    }

    @Test
    public void testUpdateEpic() throws IOException, InterruptedException {
        LocalDateTime start = LocalDateTime.of(2025, 2, 7, 10, 0);
        Duration duration = Duration.ofMinutes(30);
        Epic epic1 = manager.createEpic(new Epic("EPIC1", "SOMEOFEPIC1"));
        manager.createSubtask(new Subtask("SUBTASK1", "SOMETASK", TaskStatus.DONE, 1,
                start, duration));
        manager.createSubtask(new Subtask("SUBTASK2", "SOMETASK", TaskStatus.DONE, 1,
                start.plusMinutes(30), duration));
        Epic newEpic1 = new Epic(1, "NEWEPIC1", "SOMETHINGNEW");
        String epicJson = gson.toJson(newEpic1, Epic.class);
        //обновляем существующий Эпик новым корректным эпиком
        URI url = URI.create("http://localhost:8080/epics/1");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        assertEquals(newEpic1.getName(), manager.getEpicById(newEpic1.getId()).getName(),
                "Некорректно обновляются Эпики");

        //тест на обновление эпика, которого нет в базе, должен вернуть 404
        Epic wrongEpic = new Epic(33, "WRONGEPIC", "SOMETHING");
        String wrongEpicJson = gson.toJson(wrongEpic);
        URI url1 = URI.create("http://localhost:8080/epics/33");
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(url1)
                .POST(HttpRequest.BodyPublishers.ofString(wrongEpicJson))
                .build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(1, manager.getAllEpics().size(), "Некорректно обновляются эпики");
        assertEquals(404, response1.statusCode());
    }

    @Test
    public void testWrongRequests() throws IOException, InterruptedException {
        //передаем неверный Id для удаления задачи
        URI url = URI.create("http://localhost:8080/epics/фывфы");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());
        assertEquals("Bad Request", response.body());
        //передаем неверный Id для получения задачи
        URI url1 = URI.create("http://localhost:8080/epics/OOOOLOL");
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(url1)
                .GET()
                .build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response1.statusCode());
        assertEquals("Bad Request", response1.body());
        //передаем неверный метод и айди в запросе
        String epicJson = gson.toJson(new Epic("TEST", "TEST"));
        URI url2 = URI.create("http://localhost:8080/epics/OOOOLOL");
        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(url2)
                .PUT(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response2.statusCode());
        assertEquals("Bad Request", response2.body());
        //передаем неверный эндпоинт (корректный метод, неверный айди, неверный путь)
        URI url3 = URI.create("http://localhost:8080/epics/OOOOLOL");
        HttpRequest request3 = HttpRequest.newBuilder()
                .uri(url3)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        HttpResponse<String> response3 = client.send(request3, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response3.statusCode());
        assertEquals("Bad Request", response3.body());
        //передаем неверный айди эпика при запросе на получения его сабтасков
        URI url4 = URI.create("http://localhost:8080/epics/щщдщ/subtasks");
        HttpRequest request4 = HttpRequest.newBuilder()
                .uri(url4)
                .GET()
                .build();
        HttpResponse<String> response4 = client.send(request4, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response4.statusCode());
        assertEquals("Bad Request", response4.body());

        //в консоле будет вывод (не 5 итераций выводы поскольку в одной из проверок указан неверный метод запроса):
//        Неверный формат идентификатора
//        Неверный формат идентификатора
//        Неверный формат идентификатора
//        Неверный формат идентификатора
    }
}
