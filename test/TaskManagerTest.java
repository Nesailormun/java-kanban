import module.Epic;
import module.Subtask;
import module.Task;
import module.TaskStatus;
import service.TaskManager;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {

    protected T manager;

    abstract void initiateManager();

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
    void deleteAllTasks(){
        LocalDateTime start = LocalDateTime.of(2025, 1, 28, 10, 0);
        Duration duration = Duration.ofMinutes(30);
        Task task1 = manager.createTask(new Task("TASK1", "SOMETHINGTODO1", TaskStatus.NEW,
                start, duration));
        Task task2 = manager.createTask(new Task("TASK2", "SOMETHINGTODO2", TaskStatus.NEW,
                start.plusMinutes(30), duration));
        manager.deleteAllTasks();
        assertTrue(manager.getAllTasks().isEmpty());
    }

    void getAllTasks(){
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
    void createEpic(){
        Epic epic1 = manager.createEpic(new Epic("EPIC1", "SOMEOFEPIC1"));
        Epic epic2 = manager.createEpic(new Epic("EPIC2", "SOMEOFEPIC2"));
        assertNotNull(epic1);
        assertNotEquals(epic1, epic2);
        assertEquals(2, manager.getAllEpics().size());
    }

    @Test
    void updateEpic(){
        Epic epic1 = manager.createEpic(new Epic("EPIC1", "SOMEOFEPIC1"));
        manager.updateEpic(new Epic(1, "NEWEPIC1", "SOMENEWEPIC1"));
        assertEquals("NEWEPIC1", manager.getEpicById(epic1.getId()).getName());
        assertEquals("SOMENEWEPIC1", manager.getEpicById(epic1.getId()).getDescription());
    }

    @Test
    void getEpicById(){
        Epic epic1 = manager.createEpic(new Epic("EPIC1", "SOMEOFEPIC1"));
        assertEquals(manager.getEpicById(epic1.getId()), epic1);
        assertNull(manager.getEpicById(24));
    }

    @Test
    void getAllEpics(){
        Epic epic1 = manager.createEpic(new Epic("EPIC1", "SOMEOFEPIC1"));
        assertEquals(1, manager.getAllEpics().size());
    }

    @Test
    void removeEpic(){

    }

    void deleteAllEpics();


    List<Subtask> getEpicsSubtasks(Epic epic);

    // Beginning of module.Subtask methods:

    Subtask createSubtask(Subtask subtask);

    void updateSubtask(Subtask subtask);

    Subtask getSubtaskById(int id);

    List<Subtask> getAllSubtasks();

    void removeSubtask(int id);

    void deleteAllSubtasks();
}
