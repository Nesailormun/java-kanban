import static org.junit.jupiter.api.Assertions.*;

import enums.TaskStatus;
import exceptions.DateTimeIntersectionException;
import exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.*;
import service.*;

import java.time.Duration;
import java.time.LocalDateTime;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {


    @BeforeEach
    void recreateTaskManager() {
        manager = new InMemoryTaskManager();
    }

    @Test
    void assertManagerIsWorkingCorrectly() {
        assertInstanceOf(InMemoryTaskManager.class, Manager.getDefault());
        assertInstanceOf(InMemoryHistoryManager.class, Manager.getDefaultHistory(), "getDefaultHistory" +
                " не создает экземпляр менеджера service.InMemoryHistoryManager");
        assertInstanceOf(TaskManager.class, manager, "service.Manager не создает проинициализированный экземпляр" +
                " менеджера service.TaskManager");
    }

    @Test
    void testTasks() {
        try {
            Task task1 = manager.createTask(new Task("TASK1", "SOMETASK1"));
            manager.createTask(new Task("TASK2", "SOMETASK2"));
            manager.createTask(new Task("TASK3", "SOMETASK3"));
            int savedId = task1.getId();

            //проверка получения таска по ID + добавления в хранилище
            assertNotNull(manager.getTaskById(savedId), "Таск не был добавлен в хранилище");
            //проверка равенства тасков по ID
            assertEquals(task1, manager.getTaskById(savedId), "Таски с одинаковым id не равны друг другу" +
                    " при добавлении объекта в хранилище значение полей неизменно");

            assertEquals(task1.toString(), manager.getTaskById(task1.getId()).toString());

            //проверка обновления таска
            manager.updateTask(new Task(1, "NEWTASK1", "SOMENEWTASK1", TaskStatus.DONE));

            assertEquals(TaskStatus.DONE, manager.getTaskById(1).getStatus(), "Таск c id=1 не обновился");

            assertNotNull(manager.getAllTasks(), "Такси не возвращаются"); // проверка получения тасков

            manager.removeTask(savedId); //проверка удаления таска по ID
            assertNull(manager.getTaskById(savedId), "Таск1 не удалился");

            manager.deleteAllTasks(); // проверка удаления всех тасков

            assertEquals(0, manager.getAllTasks().size(), "Таски не удалились");
        } catch (NotFoundException exception) {
            System.out.println(exception.getMessage());
        }
    }

    @Test
    void testEpics() {
        try {
            Epic epic1 = manager.createEpic(new Epic("EPIC1", "SOMEOFEPIC1"));
            Epic epic2 = manager.createEpic(new Epic("EPIC1", "SOMEOFEPIC2"));

            int savedId = epic1.getId();

            assertNotNull(manager.getEpicById(epic1.getId()), "Эпик не добавлен в хранилище");

            assertEquals(manager.getEpicById(savedId), epic1, "Эпики с одинаковым ID не равны друг другу" +
                    " при добавлении объекта в хранилище значение полей неизменно");

            manager.createSubtask(new Subtask("SUBTASK1", "SOMEOFSUBTASK1",
                    epic1.getId()));
            manager.createSubtask(new Subtask("SUBTASK2", "SOMEOFSUBTASK2",
                    epic1.getId()));
            manager.createSubtask(new Subtask("SUBTASK3", "SOMEOFSUBTASK3",
                    epic1.getId()));
            Subtask subtask4 = manager.createSubtask(new Subtask("SUBTASK4", "SOMEOFSUBTASK4",
                    epic2.getId()));
            Subtask subtask5 = manager.createSubtask(new Subtask("SUBTASK5", "SOMEOFSUBTASK5",
                    epic2.getId()));

            assertEquals(TaskStatus.NEW, epic2.getStatus(), "Статус эпика2 считается неверно (CORRECT-NEW)");

            assertNotNull(manager.getAllEpics(), "Не возвращает список эпиков");

            subtask4.setStatus(TaskStatus.DONE);
            subtask5.setStatus(TaskStatus.DONE);
            manager.updateSubtask(subtask4);
            manager.updateSubtask(subtask5);
            assertEquals(TaskStatus.DONE, epic2.getStatus(), "Статус эпика2 считается неверно (CORRECT-DONE)");

            subtask5.setStatus(TaskStatus.NEW);
            manager.updateSubtask(subtask5);
            assertEquals(TaskStatus.IN_PROGRESS, epic2.getStatus(), "Статус эпика2 считается неверно" +
                    " (CORRECT-IN_PROGRESS)");

            manager.removeSubtask(subtask5.getId());
            assertEquals(TaskStatus.DONE, epic2.getStatus(), "Статус эпика2 считается неверно" +
                    " (CORRECT-IN_PROGRESS)");

            assertEquals(epic2.toString(), manager.getEpicById(epic2.getId()).toString());

            for (int i : manager.getEpicById(epic2.getId()).getSubtasksId()) {
                Subtask testSubtask = manager.getSubtaskById(i);
                for (Subtask subtaskInEpic : manager.getEpicsSubtasks(epic2)) {
                    assertEquals(testSubtask, subtaskInEpic, "Эпик2 некорректно обновляет свои сабтаски");
                }
            }

            Epic newEpic2 = manager.createEpic(new Epic(2, "NEWEPIC2", "SOME BIG EPIC"));
            manager.updateEpic(newEpic2);
            assertNotEquals(epic2, manager.getEpicById(2), "Некорректное обновление эпика");

            assertNotNull(manager.getEpicById(epic1.getId()), "Эпик не добавлен в хранилище эпиков");

            manager.deleteAllSubtasks();
            assertTrue(manager.getEpicById(epic1.getId()).getSubtasksId().isEmpty(), "В эпиках не очистились" +
                    " сабтаски при их удалении");

            manager.deleteAllEpics();
            assertEquals(0, manager.getAllSubtasks().size(), "При удалении всех эпиков не удалились сабтаски");
        } catch (NotFoundException exception) {
            System.out.println(exception.getMessage());
        }
    }

    @Test
    void testSubtask() {
        Epic epic1 = manager.createEpic(new Epic("EPIC1", "SOMEOFEPIC1"));
        Epic epic2 = manager.createEpic(new Epic("EPIC1", "SOMEOFEPIC2"));

        manager.createSubtask(new Subtask("SUBTASK1", "SOMEOFSUBTASK1",
                TaskStatus.NEW, epic1.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("SUBTASK2", "SOMEOFSUBTASK2",
                epic1.getId()));
        manager.createSubtask(new Subtask("SUBTASK3", "SOMEOFSUBTASK3",
                epic1.getId()));
        manager.createSubtask(new Subtask("SUBTASK4", "SOMEOFSUBTASK4",
                epic2.getId()));
        manager.createSubtask(new Subtask("SUBTASK5", "SOMEOFSUBTASK5",
                epic2.getId()));
        Subtask subtask6 = manager.createSubtask(new Subtask(45, "SomeNewSubtask", "ToDoInSubtask",
                TaskStatus.NEW, 2));
        assertEquals(subtask2.getEpicId(), epic1.getId(), "Сабтаск не знает свой эпик");
        assertThrows(NotFoundException.class, () -> manager.updateSubtask(new Subtask(58, "WRONGSUBTASK",
                "FAKEEPICID", TaskStatus.NEW, 7)));
        assertEquals(subtask6, manager.getSubtaskById(45), "Сабтаски с одинаковым ID не равны");
        assertEquals(subtask6.toString(), manager.getSubtaskById(45).toString());
    }


    @Test
    void testHistoryManager() {
        assertTrue(manager.getHistory().isEmpty(), "История тасков должна быть пуста");
        Task task1 = manager.createTask(new Task("TASK1", "SOMETHINGTODO1", TaskStatus.NEW));
        Task task2 = manager.createTask(new Task("TASK2", "SOMETHINGTODO2", TaskStatus.NEW));
        Task task3 = manager.createTask(new Task("TASK3", "SOMETHINGTODO3", TaskStatus.NEW));
        Task task4 = manager.createTask(new Task("TASK4", "SOMETHINGTODO4", TaskStatus.NEW));
        Task task5 = manager.createTask(new Task("TASK5", "SOMETHINGTODO5", TaskStatus.NEW));

        Epic epic1 = manager.createEpic(new Epic("EPIC1", "SOMEOFEPIC1"));
        Epic epic2 = manager.createEpic(new Epic("EPIC1", "SOMEOFEPIC2"));

        Subtask subtask1 = manager.createSubtask(new Subtask("SUBTASK1", "SOMEOFSUBTASK1",
                epic1.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("SUBTASK2", "SOMEOFSUBTASK2",
                epic1.getId()));
        Subtask subtask3 = manager.createSubtask(new Subtask("SUBTASK3", "SOMEOFSUBTASK3",
                epic1.getId()));
        Subtask subtask4 = manager.createSubtask(new Subtask("SUBTASK4", "SOMEOFSUBTASK4",
                epic2.getId()));
        Subtask subtask5 = manager.createSubtask(new Subtask("SUBTASK5", "SOMEOFSUBTASK5",
                epic2.getId()));

        manager.getTaskById(task1.getId());
        assertEquals(1, manager.getHistory().size(), "В истории должен быть 1 таск");
        manager.getTaskById(task2.getId());
        manager.getTaskById(task3.getId());
        manager.getTaskById(task4.getId());
        manager.getTaskById(task5.getId());

        manager.getEpicById(epic1.getId());
        manager.getEpicById(epic2.getId());

        manager.getSubtaskById(subtask1.getId());
        manager.getSubtaskById(subtask2.getId());
        manager.getSubtaskById(subtask3.getId());
        assertEquals(10, manager.getHistory().size(), "Некорректное количество тасков в истории");
        manager.getSubtaskById(subtask4.getId());
        manager.getSubtaskById(subtask5.getId());
        assertEquals(12, manager.getHistory().size(), "В истории не 12 последних тасков");

        manager.getTaskById(task1.getId());
        assertEquals(task2, manager.getHistory().getFirst(), "Объект после повторного обращения не" +
                "переместился в конец истории");
        manager.getTaskById(task2.getId());
        assertEquals(12, manager.getHistory().size(), "История обновляется некорректно");

        assertEquals(task2, manager.getHistory().getLast(), "Объект после повторного обращения не" +
                "переместился в конец истории");

        manager.deleteAllEpics();
        assertEquals(5, manager.getHistory().size(), "Не удалились Эпики и их Сабтаски из истории");

        Epic epic3 = manager.createEpic(new Epic("Epic3", "EPIC3DESCRIPTION"));
        Subtask subtask6 = manager.createSubtask(new Subtask("SUBTASK6",
                "SUBTASK6DESCRIPTION", 13));
        manager.getEpicById(epic3.getId());
        manager.getSubtaskById(subtask6.getId());
        manager.deleteAllSubtasks();
        assertEquals(6, manager.getHistory().size(), "Удаленные Сабтаски не удалились из истории");

        manager.deleteAllTasks();
        assertEquals(1, manager.getHistory().size(), "Удаленные Таски не удалились из истории");

        manager.removeEpic(epic3.getId());
        assertEquals(0, manager.getHistory().size(), "Удаленный Эпик по id не удалился из истории");

        manager.createTask(task1);
        manager.createTask(task2);
        Epic epic4 = manager.createEpic(new Epic("EPIC4", "SOMEEPIC4"));
        Subtask subtask7 = manager.createSubtask(new Subtask("Subtask7", "Somesubtask7",
                epic4.getId()));
        Subtask subtask8 = manager.createSubtask(new Subtask("Subtask8", "Somesubtask8",
                epic4.getId()));

        manager.getEpicById(epic4.getId());
        manager.getTaskById(task1.getId());
        manager.getTaskById(task2.getId());
        manager.getSubtaskById(subtask7.getId());
        manager.getSubtaskById(subtask8.getId());
        manager.getSubtaskById(subtask7.getId());

        manager.removeSubtask(subtask7.getId());

        assertEquals(4, manager.getHistory().size(), "Некорректно удаляется Сабтаск из истории");

        manager.removeTask(task2.getId());
        assertEquals(3, manager.getHistory().size(), "Некорректно удаляется Таск из истории");

        manager.removeEpic(epic4.getId());
        assertEquals(1, manager.getHistory().size(), "Удаленный Эпик по id не удалился из истории");
    }


    @Test
    void testTimeValidation() {
        LocalDateTime start = LocalDateTime.of(2025, 1, 30, 10, 0);
        Duration duration = Duration.ofMinutes(30);
        Task task1 = manager.createTask(new Task("TASK1", "SOMETHINGTODO1", TaskStatus.NEW, start,
                duration));
        Task task2 = manager.createTask(new Task("TASK2", "SOMETHINGTODO2", TaskStatus.NEW,
                start.plusMinutes(30), duration));
        assertEquals(2, manager.getAllTasks().size(), "Некорректная проверка временных интервалов," +
                " временные интервалы не пересекаются");
        assertThrows(DateTimeIntersectionException.class, () -> manager.createTask(new Task("TASK3",
                "SOMETHINGTODO3", TaskStatus.NEW, start.plusMinutes(30), duration)));
        assertEquals(2, manager.getAllTasks().size(), "Некорректная проверка временных интервалов," +
                " временные интервалы пересекаются");
        assertThrows(DateTimeIntersectionException.class, () -> manager.createTask(new Task("TASK3",
                "SOMETHINGTODO3", TaskStatus.NEW, start.minusMinutes(30),
                duration.plusMinutes(30))));
        assertEquals(2, manager.getAllTasks().size(), "Некорректная проверка временных интервалов," +
                " временные интервалы пересекаются");
        assertThrows(DateTimeIntersectionException.class, () -> manager.createTask(new Task("TASK4",
                "SOMETHINGTODO4", TaskStatus.NEW, start.minusMinutes(30),
                duration.plusMinutes(10))));
        assertEquals(2, manager.getAllTasks().size(), "Некорректная проверка временных интервалов," +
                " временные интервалы пересекаются");
        Task task5 = manager.createTask(new Task("TASK4", "SOMETHINGTODO4", TaskStatus.NEW,
                start.minusMinutes(30), duration));
        assertEquals(3, manager.getAllTasks().size(), "Некорректная проверка временных интервалов," +
                " временные интервалы не пересекаются");
    }

    @Test
    void testPrioritizedTasks() {
        LocalDateTime start = LocalDateTime.of(2025, 1, 30, 10, 0);
        Duration duration30Min = Duration.ofMinutes(30);
        Task task1 = manager.createTask(new Task("TASK1", "SOMETHINGTODO1", TaskStatus.NEW, start,
                duration30Min));
        Task task2 = manager.createTask(new Task("TASK2", "SOMETHINGTODO2", TaskStatus.NEW,
                start.plusMinutes(30), duration30Min));
        Task task3 = manager.createTask(new Task("TASK3", "SOMETHINGTODO2", TaskStatus.NEW,
                start.minusMinutes(30), duration30Min.minusMinutes(10)));

        assertEquals(task3, manager.getPrioritizedTasks().getFirst(), "Неверный порядок " +
                "в приоритетном списке тасков (correct: task3, task1, task2)");
        assertEquals(task2, manager.getPrioritizedTasks().getLast(), "Неверный порядок " +
                "в приоритетном списке тасков (correct: task3, task1, task2)");

        manager.updateTask(new Task(task2.getId(), "NEWTASK2", "NEWSOMETHINGTODO2",
                TaskStatus.IN_PROGRESS, start.minusMinutes(10), duration30Min.minusMinutes(20)));
        assertEquals(task3, manager.getPrioritizedTasks().getFirst(), "Неверный порядок " +
                "в приоритетном списке тасков (correct: task3, task2, task1)");
        assertEquals(task1, manager.getPrioritizedTasks().getLast(), "Неверный порядок " +
                "в приоритетном списке тасков (correct: task3, task2, task1)");


        Epic epic1 = manager.createEpic(new Epic("EPIC1", "SOMEEPIC1"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Subtask1", "SomthingToDo1",
                TaskStatus.NEW, epic1.getId(), start.plusMinutes(60), duration30Min));
        Subtask subtask2 = manager.createSubtask(new Subtask("Subtask1", "SomthingToDo1",
                TaskStatus.DONE, epic1.getId(), start.plusMinutes(90), duration30Min));

        assertEquals(task3, manager.getPrioritizedTasks().getFirst(), "Неверный порядок " +
                "в приоритетном списке тасков после добавления epic1, subtask1, subtask2" +
                " (correct: task3, task2, task1, subtask1, subtask2)");
        assertEquals(subtask2, manager.getPrioritizedTasks().getLast(), "Неверный порядок " +
                "в приоритетном списке тасков после добавления epic1, subtask1, subtask2" +
                " (correct: task3, task2, task1, subtask1, subtask2)");
        assertEquals(5, manager.getPrioritizedTasks().size(), "Ошибка добавления тасков в" +
                " prioritizedTasks");

        Subtask newSubtask1 = new Subtask(subtask1.getId(), "NEWSUBTASK1", "SOMTHINGGOOD",
                TaskStatus.DONE, epic1.getId(), start.minusMinutes(180), duration30Min);
        manager.updateSubtask(newSubtask1);

        assertEquals(newSubtask1, manager.getPrioritizedTasks().getFirst(), "Неверный порядок " +
                "в приоритетном списке Тасков после изменения subtask1 (correct:  subtask1, task3, task2," +
                " task1, subtask2)");
        assertEquals(subtask2, manager.getPrioritizedTasks().getLast(), "Неверный порядок " +
                "в приоритетном списке Тасков после изменения subtask1 (correct:  subtask1, task3, task2," +
                " task1, subtask2)");

        manager.removeSubtask(subtask2.getId());
        assertEquals(newSubtask1, manager.getPrioritizedTasks().getFirst(), "Неверный порядок " +
                "в приоритетном списке Тасков после изменения subtask1 (correct: subtask1, task3, task2," +
                " task1)");
        assertEquals(task1, manager.getPrioritizedTasks().getLast(), "Неверный порядок " +
                "в приоритетном списке Тасков после изменения subtask1 (correct: subtask1, task3, task2," +
                " task1)");
        assertEquals(4, manager.getPrioritizedTasks().size(), "Ошибка удаления subtask2 из списка");

        manager.deleteAllEpics();
        assertEquals(task3, manager.getPrioritizedTasks().getFirst(), "Неверный порядок " +
                "в приоритетном списке тасков после удаления эпика (correct: task3, task2, task1)");
        assertEquals(task1, manager.getPrioritizedTasks().getLast(), "Неверный порядок " +
                "в приоритетном списке тасков после удаления эпика (correct: task3, task2, task1)");
        assertEquals(3, manager.getPrioritizedTasks().size(), "Ошибка удаления тасков в" +
                " prioritizedTasks");
    }
}
