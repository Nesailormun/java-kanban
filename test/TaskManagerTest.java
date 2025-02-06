import model.Epic;
import model.Subtask;
import model.Task;
import enums.TaskStatus;
import service.TaskManager;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {

    protected T manager;

    @Test
    void getHistory() {
        LocalDateTime start = LocalDateTime.of(2025, 1, 28, 10, 0);
        Duration duration = Duration.ofMinutes(30);
        Task task1 = manager.createTask(new Task("TASK1", "SOMETHINGTODO1", TaskStatus.NEW,
                start, duration));
        Task task2 = manager.createTask(new Task("TASK2", "SOMETHINGTODO2", TaskStatus.NEW,
                start.plusMinutes(30), duration));
        manager.getTaskById(task1.getId());
        manager.getTaskById(task2.getId());
        assertEquals(2, manager.getHistory().size(), "Неккоректное сохранение в истории");
    }


    // Beginning of module.Task Tests:
    @Test
    void getTaskById() {
        LocalDateTime start = LocalDateTime.of(2025, 1, 28, 10, 0);
        Duration duration = Duration.ofMinutes(30);
        Task task1 = manager.createTask(new Task("TASK1", "SOMETHINGTODO1", TaskStatus.NEW,
                start, duration));
        Task task2 = manager.createTask(null);
        Task task3 = manager.getTaskById(task1.getId());
        assertEquals(task1, task3, "Возвращение неверного таска");
        assertNull(task2, "таск2 должен быть равен null");
    }

    @Test
    void createTask() {
        LocalDateTime start = LocalDateTime.of(2025, 1, 28, 10, 0);
        Duration duration = Duration.ofMinutes(30);
        Task task1 = manager.createTask(new Task("TASK1", "SOMETHINGTODO1", TaskStatus.NEW,
                start, duration));
        Task task2 = manager.createTask(new Task("TASK2", "SOMETHINGTODO2", TaskStatus.NEW,
                start.plusMinutes(30), duration));
        Task task3 = manager.createTask(new Task("TASK3", "SOMETHINGTODO3", TaskStatus.NEW,
                start.plusMinutes(30), duration));
        assertEquals(2, manager.getAllTasks().size(), "Ошибка создания тасков");
    }

    @Test
    void updateTask() {
        LocalDateTime start = LocalDateTime.of(2025, 1, 28, 10, 0);
        Duration duration = Duration.ofMinutes(30);
        Task task1 = manager.createTask(new Task("TASK1", "SOMETHINGTODO1", TaskStatus.NEW,
                start, duration));
        Task newTask1 = manager.createTask(new Task(1, "NEWTASK1", "SOMETHINGTODO1", TaskStatus.DONE,
                start.plusMinutes(30), duration));
        manager.updateTask(newTask1);
        assertEquals(newTask1, manager.getTaskById(task1.getId()), "Таск не обновился");
    }

    @Test
    void removeTask() {
        LocalDateTime start = LocalDateTime.of(2025, 1, 28, 10, 0);
        Duration duration = Duration.ofMinutes(30);
        Task task1 = manager.createTask(new Task("TASK1", "SOMETHINGTODO1", TaskStatus.NEW,
                start, duration));
        Task task2 = manager.createTask(new Task("TASK2", "SOMETHINGTODO2", TaskStatus.NEW,
                start.plusMinutes(30), duration));
        manager.removeTask(task1.getId());
        assertEquals(1, manager.getAllTasks().size());
        manager.removeTask(task1.getId());
        assertEquals(1, manager.getAllTasks().size());
        manager.removeTask(task2.getId());
        assertTrue(manager.getAllTasks().isEmpty());
    }

    @Test
    void deleteAllTasks() {
        LocalDateTime start = LocalDateTime.of(2025, 1, 28, 10, 0);
        Duration duration = Duration.ofMinutes(30);
        Task task1 = manager.createTask(new Task("TASK1", "SOMETHINGTODO1", TaskStatus.NEW,
                start, duration));
        Task task2 = manager.createTask(new Task("TASK2", "SOMETHINGTODO2", TaskStatus.NEW,
                start.plusMinutes(30), duration));
        manager.deleteAllTasks();
        assertTrue(manager.getAllTasks().isEmpty());
    }

    void getAllTasks() {
        LocalDateTime start = LocalDateTime.of(2025, 1, 28, 10, 0);
        Duration duration = Duration.ofMinutes(30);
        Task task1 = manager.createTask(new Task("TASK1", "SOMETHINGTODO1", TaskStatus.NEW,
                start, duration));
        Task task2 = manager.createTask(new Task("TASK2", "SOMETHINGTODO2", TaskStatus.NEW,
                start.plusMinutes(30), duration));
        assertEquals(2, manager.getAllTasks().size());
        manager.deleteAllTasks();
        assertTrue(manager.getAllTasks().isEmpty());
    }

    // Beginning of module.Epic Tests:
    @Test
    void createEpic() {
        Epic epic1 = manager.createEpic(new Epic("EPIC1", "SOMEOFEPIC1"));
        Epic epic2 = manager.createEpic(new Epic("EPIC2", "SOMEOFEPIC2"));
        assertNotNull(epic1);
        assertNotEquals(epic1, epic2);
        assertEquals(2, manager.getAllEpics().size());
    }

    @Test
    void updateEpic() {
        Epic epic1 = manager.createEpic(new Epic("EPIC1", "SOMEOFEPIC1"));
        manager.updateEpic(new Epic(1, "NEWEPIC1", "SOMENEWEPIC1"));
        assertEquals("NEWEPIC1", manager.getEpicById(epic1.getId()).getName());
        assertEquals("SOMENEWEPIC1", manager.getEpicById(epic1.getId()).getDescription());
    }

    @Test
    void getEpicById() {
        Epic epic1 = manager.createEpic(new Epic("EPIC1", "SOMEOFEPIC1"));
        assertEquals(manager.getEpicById(epic1.getId()), epic1);
        assertNull(manager.getEpicById(24));
    }

    @Test
    void getAllEpics() {
        Epic epic1 = manager.createEpic(new Epic("EPIC1", "SOMEOFEPIC1"));
        assertEquals(1, manager.getAllEpics().size());
    }

    @Test
    void removeEpic() {
        Epic epic1 = manager.createEpic(new Epic("EPIC1", "SOMEOFEPIC1"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Subtask1", "SomeSubtask1", epic1.getId(),
                LocalDateTime.now(), Duration.ofMinutes(20)));
        manager.removeEpic(epic1.getId());
        assertEquals(0, manager.getAllEpics().size());
        assertEquals(0, manager.getAllSubtasks().size());
    }

    @Test
    void deleteAllEpics() {
        Epic epic1 = manager.createEpic(new Epic("EPIC1", "SOMEOFEPIC1"));
        Epic epic2 = manager.createEpic(new Epic("EPIC2", "SOMEOFEPIC2"));
        manager.deleteAllEpics();
        assertEquals(0, manager.getAllEpics().size());
    }

    @Test
    void getEpicsSubtasks() {
        Epic epic1 = manager.createEpic(new Epic("EPIC1", "SOMEOFEPIC1"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Subtask1", "SomeSubtask1", epic1.getId(),
                LocalDateTime.now(), Duration.ofMinutes(20)));
        Subtask subtask2 = manager.createSubtask(new Subtask("Subtask2", "SomeSubtask2", epic1.getId(),
                subtask1.getStartTime().plusMinutes(20), Duration.ofMinutes(20)));
        Epic epic2 = manager.createEpic(new Epic("EPIC2", "SOMEOFEPIC2"));
        assertEquals(2, manager.getEpicsSubtasks(epic1).size());
        assertEquals(0, manager.getEpicsSubtasks(epic2).size());
    }

    // Beginning of module.Subtask methods:
    @Test
    void createSubtask() {
        Epic epic1 = manager.createEpic(new Epic("EPIC1", "SOMEOFEPIC1"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Subtask1", "SomeSubtask1", 1,
                LocalDateTime.now(), Duration.ofMinutes(20)));
        Subtask subtask2 = manager.createSubtask(new Subtask("Subtask2", "SomeSubtask2", 1,
                subtask1.getStartTime().plusMinutes(20), Duration.ofMinutes(20)));
        Subtask subtask3 = manager.createSubtask(new Subtask("Subtask3", "SomeSubtask3", 1,
                LocalDateTime.now(), Duration.ofMinutes(20)));
        Subtask subtask4 = manager.createSubtask(new Subtask("Subtask4", "SomeSubtask4", 12,
                subtask1.getStartTime().plusMinutes(60), Duration.ofMinutes(20)));
        assertEquals(2, manager.getAllSubtasks().size());
    }

    @Test
    void updateSubtask() {
        Epic epic1 = manager.createEpic(new Epic("EPIC1", "SOMEOFEPIC1"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Subtask1", "SomeSubtask1", 1,
                LocalDateTime.now(), Duration.ofMinutes(20)));
        Subtask subtask2 = manager.createSubtask(new Subtask("Subtask2", "SomeSubtask2", 1,
                subtask1.getStartTime().plusMinutes(20), Duration.ofMinutes(20)));
        Subtask subtask3 = manager.createSubtask(new Subtask("Subtask3", "SomeSubtask3", 1,
                LocalDateTime.now(), Duration.ofMinutes(20)));
        assertEquals(2, manager.getAllSubtasks().size());

        manager.updateSubtask(new Subtask(2, "NEWSUBTASK3", "SOMESUBTASK4", TaskStatus.IN_PROGRESS,
                epic1.getId(), subtask1.getStartTime().plusMinutes(40), subtask1.getDuration()));
        assertEquals(2, manager.getAllSubtasks().size());
        assertEquals(TaskStatus.IN_PROGRESS, epic1.getStatus());
    }

    @Test
    void getSubtaskById() {
        Epic epic1 = manager.createEpic(new Epic("EPIC1", "SOMEOFEPIC1"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Subtask1", "SomeSubtask1", 1,
                LocalDateTime.now(), Duration.ofMinutes(20)));
        Subtask subtask2 = manager.createSubtask(new Subtask("Subtask2", "SomeSubtask2", 1,
                subtask1.getStartTime().plusMinutes(20), Duration.ofMinutes(20)));
        assertEquals(subtask1, manager.getSubtaskById(subtask1.getId()));
        assertNotEquals(subtask2, manager.getSubtaskById(subtask1.getId()));
        assertNull(manager.getSubtaskById(123));
    }

    @Test
    void getAllSubtasks() {
        Epic epic1 = manager.createEpic(new Epic("EPIC1", "SOMEOFEPIC1"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Subtask1", "SomeSubtask1", 1,
                LocalDateTime.now(), Duration.ofMinutes(20)));
        Subtask subtask2 = manager.createSubtask(new Subtask("Subtask2", "SomeSubtask2", 1,
                subtask1.getStartTime().plusMinutes(20), Duration.ofMinutes(20)));
        assertEquals(2, manager.getAllSubtasks().size());
    }

    @Test
    void removeSubtask() {
        Epic epic1 = manager.createEpic(new Epic("EPIC1", "SOMEOFEPIC1"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Subtask1", "SomeSubtask1", 1,
                LocalDateTime.now(), Duration.ofMinutes(20)));
        Subtask subtask2 = manager.createSubtask(new Subtask("Subtask2", "SomeSubtask2", 1,
                subtask1.getStartTime().plusMinutes(20), Duration.ofMinutes(20)));
        manager.removeSubtask(subtask1.getId());
        assertEquals(1, manager.getAllSubtasks().size());
        manager.removeSubtask(subtask1.getId());
        manager.removeSubtask(subtask2.getId());
        assertEquals(0, manager.getAllSubtasks().size());
    }

    @Test
    void deleteAllSubtasks() {
        Epic epic1 = manager.createEpic(new Epic("EPIC1", "SOMEOFEPIC1"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Subtask1", "SomeSubtask1", 1,
                LocalDateTime.now(), Duration.ofMinutes(20)));
        Subtask subtask2 = manager.createSubtask(new Subtask("Subtask2", "SomeSubtask2", 1,
                subtask1.getStartTime().plusMinutes(20), Duration.ofMinutes(20)));
        manager.deleteAllSubtasks();
        assertEquals(0, manager.getEpicsSubtasks(epic1).size());
    }
}
