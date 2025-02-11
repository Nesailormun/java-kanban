import static org.junit.jupiter.api.Assertions.*;

import enums.TaskStatus;
import exceptions.NotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import model.*;
import exceptions.ManagerSaveException;
import service.FileBackedTaskManager;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    File tempTestFile;

    @BeforeEach
    void createFileAndInitializeManager() {
        try {
            tempTestFile = File.createTempFile("tempTestFile", ".cvs");
            manager = new FileBackedTaskManager(tempTestFile.toPath());
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка создания временного файла.");
        }
    }

    @Test
    void loadFromAndSaveEmptyFile() {
        FileBackedTaskManager managerLoadedFromFile = FileBackedTaskManager.loadFromFile(tempTestFile);
        assertNotNull(managerLoadedFromFile);
        assertTrue(managerLoadedFromFile.getHistory().isEmpty(), "История должна быть пустой");
    }

    @Test
    void writeInEmptyFile() {
        try {
            LocalDateTime start = LocalDateTime.of(2025, 1, 28, 10, 0);
            Duration duration = Duration.ofMinutes(30);
            Task task1 = manager.createTask(new Task("TASK1", "SOMETHINGTODO1", TaskStatus.NEW,
                    start, duration));
            Task task2 = manager.createTask(new Task("TASK2", "SOMETHINGTODO2",
                    TaskStatus.NEW, start.minusMinutes(30), duration));
            Epic epic1 = manager.createEpic(new Epic("EPIC1", "SOMEOFEPIC1"));
            Epic epic2 = manager.createEpic(new Epic("EPIC2", "SOMEOFEPIC2"));
            Subtask subtask1 = manager.createSubtask(new Subtask("SUBTASK1", "SOMEOFSUBTASK1",
                    epic1.getId(), start.minusMinutes(60), duration));
            Subtask subtask2 = manager.createSubtask(new Subtask("SUBTASK2", "SOMEOFSUBTASK2",
                    epic1.getId(), start.minusMinutes(90), duration));
            Subtask subtask3 = manager.createSubtask(new Subtask("SUBTASK4", "SOMEOFSUBTASK4",
                    epic2.getId(), start.minusMinutes(180), duration));

            assertTrue(manager.getHistory().isEmpty(), "История должна быть пустой");
            assertEquals(task1, manager.getTaskById(task1.getId()), "Таски не равны");
            assertEquals(task2, manager.getTaskById(task2.getId()), "Таски не равны");
            assertEquals(epic1, manager.getEpicById(epic1.getId()), "Эпики не равны");
            assertEquals(subtask1, manager.getSubtaskById(subtask1.getId()), "Сабтаски не равны");
            assertEquals(subtask2, manager.getSubtaskById(subtask2.getId()), "Сабтаски не равны");
            assertEquals(subtask3, manager.getSubtaskById(subtask3.getId()), "Сабтаски не равны");
            assertEquals(6, manager.getHistory().size(), "История просмотров тасков сохранена некорректно");
        } catch (NotFoundException exception) {
            System.out.println(exception.getMessage());
        }
    }

    @Test
    void testTwoDifferentManagersFromSameFile() {

        try {
            LocalDateTime start = LocalDateTime.of(2025, 1, 28, 10, 0);
            Duration duration = Duration.ofMinutes(30);
            Task task1 = manager.createTask(new Task("TASK1", "SOMETHINGTODO1", TaskStatus.NEW,
                    start, duration));
            Task task2 = manager.createTask(new Task("TASK2", "SOMETHINGTODO2",
                    TaskStatus.NEW, start.minusMinutes(30), duration));
            Epic epic1 = manager.createEpic(new Epic("EPIC1", "SOMEOFEPIC1"));
            Subtask subtask1 = manager.createSubtask(new Subtask("SUBTASK1", "SOMEOFSUBTASK1",
                    epic1.getId(), start.minusMinutes(60), duration));
            Subtask subtask2 = manager.createSubtask(new Subtask("SUBTASK2", "SOMEOFSUBTASK2",
                    epic1.getId(), start.minusMinutes(90), duration));

            assertTrue(manager.getPrioritizedTasks()
                    .stream()
                    .map(Task::getId)
                    .allMatch(List.of(epic1.getId(), subtask2.getId(), subtask1.getId(),
                            task2.getId(), task1.getId())::contains), "Неккоректная запись списка задач в порядке" +
                    " приоритета.");

            FileBackedTaskManager newManager = FileBackedTaskManager.loadFromFile(tempTestFile);
            assertTrue(newManager.getHistory().isEmpty(), "История просмотров должна быть пустой");
            assertEquals(manager.getTaskById(task1.getId()), newManager.getTaskById(task1.getId()),
                    "Некорректное сохранение тасков");
            assertEquals(manager.getTaskById(task2.getId()), newManager.getTaskById(task2.getId()),
                    "Некорректное сохранение тасков");
            assertEquals(manager.getEpicById(epic1.getId()), newManager.getEpicById(epic1.getId()),
                    "Некорректное сохранение эпиков");
            assertEquals(manager.getSubtaskById(subtask1.getId()), newManager.getSubtaskById(subtask1.getId()),
                    "Некорректное сохранение сабтасков");
            assertEquals(manager.getSubtaskById(subtask2.getId()), newManager.getSubtaskById(subtask2.getId()),
                    "Некорректное сохранение сабтасков");
            assertEquals(manager.getHistory().size(), newManager.getHistory().size(),
                    "Некорректное сохранение истории просмотров тасков");
            assertEquals(manager.getPrioritizedTasks(), newManager.getPrioritizedTasks());
        } catch (NotFoundException exception) {
            System.out.println(exception.getMessage());
        }
    }

    @Test
    void testThrowingIOException() {
        assertThrows(ManagerSaveException.class, () -> FileBackedTaskManager.loadFromFile(new File("ERROR")));
        assertDoesNotThrow(() -> FileBackedTaskManager.loadFromFile(tempTestFile));
    }

    @AfterEach
    void deleteAllTempFiles() {
        tempTestFile.deleteOnExit();
    }
}
