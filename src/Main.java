import java.sql.SQLOutput;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");

        // Task Tests:

        TaskManager taskManager = new TaskManager();

        Task task1 = taskManager.createTask(new Task ("Уделить время девушке", "Сегодня купаемся вместе!"));
        Task task5 = taskManager.createTask(new Task (8,"Уделить время девушке", "Сегодня купаемся вместе!", TaskStatus.DONE));
        System.out.println(taskManager.getAllTasks());
        taskManager.updateTask(new Task (8, "Marry Her", "wedding", TaskStatus.DONE));

        System.out.println(taskManager.getAllTasks());
        taskManager.removeTask(1);

        System.out.println(taskManager.getAllTasks());

        System.out.println(taskManager.getTaskById(8));

        taskManager.getTaskById(8).setStatus(TaskStatus.IN_PROGRESS);
        System.out.println(taskManager.getAllTasks());
        taskManager.deleteAllTasks();
        System.out.println(taskManager.getAllTasks());
        taskManager.deleteAllTasks();
        System.out.println("______________________________________________________________");
        System.out.println("EPIC TIME");



        // Epic + Subtasks Tests:

        Epic epic1 = taskManager.createEpic(new Epic (1, "Эпичный бум", "1 subtask"));
        Epic epic2 = taskManager.createEpic(new Epic (2, "Эпичный бум #2", "2 subtasks"));
        Subtask subtask1 = taskManager.createSubtask(new Subtask(1, "SomeSubtask1", "ABC1", TaskStatus.NEW, 1));
        Subtask subtask2 = taskManager.createSubtask(new Subtask(2, "SomeSubtask2", "ABC2", TaskStatus.NEW, 2));
        Subtask subtask3 = taskManager.createSubtask(new Subtask(3, "SomeSubtask3", "ABC3", TaskStatus.NEW, 2));
        Subtask subtask4 = taskManager.createSubtask(new Subtask(4, "SomeSubtask4", "ABC4", TaskStatus.NEW, 2));

        System.out.println(taskManager.getAllSubtasks());

        System.out.println("Все эпики:");
        System.out.println(taskManager.getAllEpics());
        System.out.println();
        System.out.println("Сабтаски первого эпика:");
        System.out.println(taskManager.getEpicsSubtasks(taskManager.getEpicById(1)));
        System.out.println();
        System.out.println("Сабтаски второго эпика:");
        System.out.println(taskManager.getEpicsSubtasks(taskManager.getEpicById(2)));
        System.out.println("Меняем статус второго эпика:");
        taskManager.getSubtaskById(2).setStatus(TaskStatus.DONE);
        taskManager.getSubtaskById(3).setStatus(TaskStatus.DONE);
        taskManager.getSubtaskById(4).setStatus(TaskStatus.DONE);
        System.out.println("Второй эпик после изменения статуса");
        System.out.println(taskManager.getEpicById(2));

        System.out.println("Поменяли статус второго сабтаска!!!");
        taskManager.updateSubtask(new Subtask(2, "Mohito party", "By some alcohol", TaskStatus.NEW, 2));
        System.out.println(taskManager.getSubtaskById(2));

        System.out.println(taskManager.getEpicById(2));

        System.out.println(taskManager.getEpicsSubtasks(new Epic (1, "john", "some descr")));

        taskManager.removeSubtask(2);

        taskManager.removeEpic(1);

        System.out.println("NEW EPIC2");
        taskManager.updateEpic(new Epic(2, "New epic 2", "AMAZING"));

        System.out.println(taskManager.getAllEpics());

        taskManager.deleteAllTasks();
        taskManager.deleteAllSubtasks();

        System.out.println(taskManager.getAllEpics());
        taskManager.deleteAllEpics();

        System.out.println(taskManager.getAllEpics());


    }
}
