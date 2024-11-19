import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class InMemoryTaskManagerTest {
    TaskManager taskManager;

    @BeforeEach
    void recreateTaskManager() {
        Manager manager = new Manager();
        taskManager = manager.getDefault();
    }

    @Test
    void assertManagerIsWorkingCorrectly() {
        assertInstanceOf(InMemoryHistoryManager.class, Manager.getDefaultHistory(), "getDefaultHistory" +
                " не создает экземпляр менеджера InMemoryHistoryManager");
        assertInstanceOf(TaskManager.class, taskManager, "Manager не создает проинициализированный экземпляр" +
                " менеджера TaskManager");
    }

    @Test
    void testTasks() {

        Task task1 = taskManager.createTask(new Task("TASK1", "SOMETASK1"));
        taskManager.createTask(new Task("TASK2", "SOMETASK2"));
        taskManager.createTask(new Task("TASK3", "SOMETASK3"));
        int savedId = task1.getId();

        //проверка получения таска по ID + добавления в хранилище
        assertNotNull(taskManager.getTaskById(savedId), "Таск не был добавлен в хранилище");
        //проверка равенства тасков по ID
        assertEquals(task1, taskManager.getTaskById(savedId), "Таски с одинаковым id не равны друг другу" +
                " при добавлении объекта в хранилище значение полей неизменно");

        assertEquals(task1.toString(), taskManager.getTaskById(task1.getId()).toString());

        //проверка обновления таска
        taskManager.updateTask(new Task(1, "NEWTASK1", "SOMENEWTASK1", TaskStatus.DONE));

        assertEquals(TaskStatus.DONE, taskManager.getTaskById(1).getStatus(), "Таск c id=1 не обновился");

        assertNotNull(taskManager.getAllTasks(), "Такси не возвращаются"); // проверка получения тасков

        taskManager.removeTask(savedId); //проверка удаления таска по ID
        assertNull(taskManager.getTaskById(savedId), "Таск1 не удалился");

        taskManager.deleteAllTasks(); // проверка удаления всех тасков

        assertNull(taskManager.getAllTasks(), "Таски не удалились");
    }

    @Test
    void testEpics(){
        Epic epic1 = taskManager.createEpic(new Epic("EPIC1", "SOMEOFEPIC1"));
        Epic epic2 = taskManager.createEpic(new Epic("EPIC1", "SOMEOFEPIC2"));

        int savedId = epic1.getId();

        assertNotNull(taskManager.getEpicById(epic1.getId()), "Эпик не добавлен в хранилище");

        assertEquals(taskManager.getEpicById(savedId), epic1, "Эпики с одинаковым ID не равны друг другу" +
                        " при добавлении объекта в хранилище значение полей неизменно");

        taskManager.createSubtask(new Subtask("SUBTASK1", "SOMEOFSUBTASK1",
                epic1.getId()));
        taskManager.createSubtask(new Subtask("SUBTASK2", "SOMEOFSUBTASK2",
                epic1.getId()));
        taskManager.createSubtask(new Subtask("SUBTASK3", "SOMEOFSUBTASK3",
                epic1.getId()));
        Subtask subtask4 = taskManager.createSubtask(new Subtask("SUBTASK4", "SOMEOFSUBTASK4",
                epic2.getId()));
        Subtask subtask5 = taskManager.createSubtask(new Subtask("SUBTASK5", "SOMEOFSUBTASK5",
                epic2.getId()));

        assertEquals(TaskStatus.NEW, epic2.getStatus(), "Статус эпика2 считается неверно (CORRECT-NEW)");

        assertNotNull(taskManager.getAllEpics(), "Не возвращает список эпиков");

        subtask4.setStatus(TaskStatus.DONE);
        subtask5.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask4);
        taskManager.updateSubtask(subtask5);
        assertEquals(TaskStatus.DONE, epic2.getStatus(), "Статус эпика2 считается неверно (CORRECT-DONE)");

        subtask5.setStatus(TaskStatus.NEW);
        taskManager.updateSubtask(subtask5);
        assertEquals(TaskStatus.IN_PROGRESS, epic2.getStatus(), "Статус эпика2 считается неверно" +
                " (CORRECT-IN_PROGRESS)");

        taskManager.removeSubtask(subtask5.getId());
        assertEquals(TaskStatus.DONE, epic2.getStatus(), "Статус эпика2 считается неверно" +
                " (CORRECT-IN_PROGRESS)");

        assertEquals(epic2.toString(), taskManager.getEpicById(epic2.getId()).toString());

        for (int i : taskManager.getEpicById(epic2.getId()).getSubtasksId()) {
            Subtask testSubtask = taskManager.getSubtaskById(i);
            for (Subtask subtaskInEpic : taskManager.getEpicsSubtasks(epic2)){
                assertEquals(testSubtask, subtaskInEpic, "Эпик2 некорректно обновляет свои сабтаски");
            }
        }

        Epic newEpic2 = taskManager.createEpic(new Epic (2,"NEWEPIC2", "SOME BIG EPIC"));
        taskManager.updateEpic(newEpic2);
        assertNotEquals(epic2, taskManager.getEpicById(2), "Некорректное обновление эпика");

        assertNotNull(taskManager.getEpicById(epic1.getId()), "Эпик не добавлен в хранилище эпиков");

        taskManager.deleteAllSubtasks();
        assertTrue(taskManager.getEpicById(epic1.getId()).getSubtasksId().isEmpty(), "В эпиках не очистились" +
                " сабтаски при их удалении");

        taskManager.deleteAllEpics();
        assertNull(taskManager.getAllSubtasks(), "При удалении всех эпиков не удалились сабтаски");
    }

    @Test
    void testSubtask(){

        Epic epic1 = taskManager.createEpic(new Epic("EPIC1", "SOMEOFEPIC1"));
        Epic epic2 = taskManager.createEpic(new Epic("EPIC1", "SOMEOFEPIC2"));

        taskManager.createSubtask(new Subtask("SUBTASK1", "SOMEOFSUBTASK1",
                TaskStatus.NEW, epic1.getId()));
        Subtask subtask2 = taskManager.createSubtask(new Subtask("SUBTASK2", "SOMEOFSUBTASK2",
                epic1.getId()));
        taskManager.createSubtask(new Subtask("SUBTASK3", "SOMEOFSUBTASK3",
                epic1.getId()));
        taskManager.createSubtask(new Subtask("SUBTASK4", "SOMEOFSUBTASK4",
                epic2.getId()));
        taskManager.createSubtask(new Subtask("SUBTASK5", "SOMEOFSUBTASK5",
                epic2.getId()));
        Subtask subtask6 = taskManager.createSubtask(new Subtask(45,"SomeNewSubtask", "ToDoInSubtask"
                , TaskStatus.NEW, 2));

        assertEquals(subtask2.getEpicId(), epic1.getId(), "Сабтаск не знает свой эпик");

        taskManager.updateSubtask(new Subtask(58, "WRONGSUBTASK", "FAKEEPICID",
                TaskStatus.NEW, 7));
        assertNull(taskManager.getSubtaskById(58), "Сабтаск был добавлен к несуществующему эпику");

        assertEquals(subtask6, taskManager.getSubtaskById(45), "Сабтаски с одинаковым ID не равны");

        assertEquals(subtask6.toString(), taskManager.getSubtaskById(45).toString());
    }


    @Test
    void testHistoryManager() {

        assertNull(taskManager.getHistory(), "История тасков должна быть пуста");
        Task task1 = taskManager.createTask(new Task("TASK1", "SOMETHINGTODO1", TaskStatus.NEW));
        Task task2 = taskManager.createTask(new Task("TASK2", "SOMETHINGTODO2", TaskStatus.NEW));
        Task task3 = taskManager.createTask(new Task("TASK3", "SOMETHINGTODO3", TaskStatus.NEW));
        Task task4 = taskManager.createTask(new Task("TASK4", "SOMETHINGTODO4", TaskStatus.NEW));
        Task task5 = taskManager.createTask(new Task("TASK5", "SOMETHINGTODO5", TaskStatus.NEW));

        Epic epic1 = taskManager.createEpic(new Epic("EPIC1", "SOMEOFEPIC1"));
        Epic epic2 = taskManager.createEpic(new Epic("EPIC1", "SOMEOFEPIC2"));

        Subtask subtask1 = taskManager.createSubtask(new Subtask("SUBTASK1", "SOMEOFSUBTASK1",
                epic1.getId()));
        Subtask subtask2 = taskManager.createSubtask(new Subtask("SUBTASK2", "SOMEOFSUBTASK2",
                epic1.getId()));
        Subtask subtask3 = taskManager.createSubtask(new Subtask("SUBTASK3", "SOMEOFSUBTASK3",
                epic1.getId()));
        Subtask subtask4 = taskManager.createSubtask(new Subtask("SUBTASK4", "SOMEOFSUBTASK4",
                epic2.getId()));
        Subtask subtask5 = taskManager.createSubtask(new Subtask("SUBTASK5", "SOMEOFSUBTASK5",
                epic2.getId()));

        taskManager.getTaskById(task1.getId());
        assertEquals(1, taskManager.getHistory().size(), "В истории должен быть 1 таск");
        taskManager.getTaskById(task2.getId());
        taskManager.getTaskById(task3.getId());
        taskManager.getTaskById(task4.getId());
        taskManager.getTaskById(task5.getId());

        taskManager.getEpicById(epic1.getId());
        taskManager.getEpicById(epic2.getId());

        taskManager.getSubtaskById(subtask1.getId());
        taskManager.getSubtaskById(subtask2.getId());
        taskManager.getSubtaskById(subtask3.getId());
        assertEquals(10, taskManager.getHistory().size(), "В истории больше чем 10 последних тасков");
        taskManager.getSubtaskById(subtask4.getId());
        taskManager.getSubtaskById(subtask5.getId());
        assertEquals(10, taskManager.getHistory().size(), "В истории больше чем 10 последних тасков");

        assertEquals(task3, taskManager.getHistory().getFirst(), "Объект добавленный в HistoryManager не" +
                "равен своей предыдущей версии до добавления");
        assertEquals(subtask5, taskManager.getHistory().getLast(), "Объект добавленный в HistoryManager не" +
                "равен своей предыдущей версии до добавления");

        int id = task3.getId();
        for (Task task : taskManager.getHistory()){
            assertEquals(id, task.getId(), "История тасков сохраняется некорректно");
            id++;
        }
    }
}
