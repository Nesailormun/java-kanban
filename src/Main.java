
public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");

        // Task Tests:

        TaskManager taskManager = new TaskManager();

        Task task1 = taskManager.createTask(new Task ("TASK1", "SOME DESCRIPTION OF TASK1"));
        Task task8 = taskManager.createTask(new Task (8, "TASK8", "SOME DESCRIPTION OF TASK8", TaskStatus.NEW));
        System.out.println();
        System.out.println(taskManager.getAllTasks());
        taskManager.updateTask(new Task (task8.getId(), "NEW TASK8", "NEW DESCRIPTION OF TASK8", TaskStatus.DONE));
        System.out.println("Обновили таск8");
        System.out.println(taskManager.getAllTasks());
        System.out.println("Удалили такс1");
        taskManager.removeTask(task1.getId());

        System.out.println(taskManager.getAllTasks());

        System.out.println(taskManager.getTaskById(8));

        taskManager.getTaskById(task8.getId()).setStatus(TaskStatus.IN_PROGRESS);
        System.out.println(taskManager.getAllTasks());
        taskManager.deleteAllTasks();
        System.out.println(taskManager.getAllTasks());
        System.out.println();

        System.out.println("______________________________________________________________");
        System.out.println("EPIC TIME");



        // Epic + Subtasks Tests:

        Epic epic1 = taskManager.createEpic(new Epic( "Эпичный бум", "1 subtask"));
        Epic epic2 = taskManager.createEpic(new Epic ( "Эпичный бум #2", "2 subtasks"));

        Subtask subtask1 = taskManager.createSubtask(new Subtask( "SomeSubtask1", "ABC1", TaskStatus.NEW, epic1.getId()));
        Subtask subtask2 = taskManager.createSubtask(new Subtask("SomeSubtask2", "ABC2", TaskStatus.NEW, epic2.getId()));
        Subtask subtask3 = taskManager.createSubtask(new Subtask("SomeSubtask3", "ABC3", TaskStatus.NEW, epic2.getId()));
        Subtask subtask4 = taskManager.createSubtask(new Subtask("SomeSubtask4", "ABC4", TaskStatus.NEW, epic2.getId()));

        System.out.println(taskManager.getAllSubtasks());

        System.out.println("Все эпики:");
        System.out.println(taskManager.getAllEpics());
        System.out.println();
        System.out.println("Сабтаски первого эпика:");
        System.out.println(taskManager.getEpicsSubtasks(taskManager.getEpicById(epic1.getId())));
        System.out.println();
        System.out.println("Сабтаски второго эпика:");
        System.out.println(taskManager.getEpicsSubtasks(taskManager.getEpicById(epic2.getId())));
        System.out.println("Меняем статус второго эпика:");

        taskManager.updateSubtask(new Subtask(subtask2.getId(), "NEW", "NEW ONE", TaskStatus.DONE, epic2.getId()));
        taskManager.updateSubtask(new Subtask(subtask3.getId(), "NEW", "NEW ONE", TaskStatus.DONE, epic2.getId()));
        taskManager.updateSubtask(new Subtask(subtask4.getId(), "NEW", "NEW ONE", TaskStatus.DONE, epic2.getId()));

        System.out.println("Второй эпик после изменения статуса");
        System.out.println(taskManager.getEpicById(epic2.getId()));

        System.out.println("Поменяли статус второго сабтаска!!!");
        taskManager.updateSubtask(new Subtask(subtask2.getId(), "NEW SUBTASK2", "NEW DESCRIPTION OF SUBTASK2", TaskStatus.NEW, epic2.getId()));
        System.out.println(taskManager.getSubtaskById(subtask2.getId()));



        System.out.println(taskManager.getEpicById(epic2.getId()));

        taskManager.removeSubtask(subtask2.getId());
        System.out.println(taskManager.getEpicsSubtasks(epic2));


        System.out.println(taskManager.getEpicsSubtasks(epic1));
        System.out.println("ВЫВОДИМ САБТАСКИ 1");
        System.out.println(taskManager.getAllSubtasks());
        taskManager.removeEpic(epic1.getId());

        System.out.println("ВЫВОДИМ САБТАСКИ 2");
        System.out.println(taskManager.getAllSubtasks());

        System.out.println("NEW EPIC2");
        taskManager.updateEpic(new Epic(epic2.getId(), "NEW EPIC 2", "NEWEST EPIC 2"));
        System.out.println(taskManager.getAllEpics());

        taskManager.deleteAllSubtasks();

        System.out.println(taskManager.getAllEpics());

        System.out.println(taskManager.getAllSubtasks());

        System.out.println(taskManager.getAllEpics());

        System.out.println("Удаляем все таски");
        taskManager.deleteAllTasks();
        System.out.println(taskManager.getAllTasks());

        taskManager.createSubtask(new Subtask("SUBTASK9", "SOMETHING", epic2.getId()));
        System.out.println(taskManager.getAllSubtasks());
        System.out.println(taskManager.getAllEpics());
        System.out.println("Удаляем все эпики");
        taskManager.deleteAllEpics();
        System.out.println(taskManager.getAllSubtasks());
        System.out.println(taskManager.getAllEpics());


    }
}
