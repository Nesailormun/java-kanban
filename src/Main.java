import module.Epic;
import module.Subtask;
import module.Task;
import module.TaskStatus;
import service.Manager;
import service.TaskManager;

public class Main {
    public static void main(String[] args) {

        Manager manager = new Manager();
        TaskManager taskManager = manager.getDefault();

        Task task1 = taskManager.createTask(new Task("TASK1", "SOMETHINGTODO1", TaskStatus.NEW));
        Task task2 = taskManager.createTask(new Task("TASK2", "SOMETHINGTODO2", TaskStatus.NEW));
        Task task3 = taskManager.createTask(new Task("TASK3", "SOMETHINGTODO3", TaskStatus.NEW));
        Task task4 = taskManager.createTask(new Task("TASK4", "SOMETHINGTODO4", TaskStatus.NEW));
        Task task5 = taskManager.createTask(new Task("TASK5", "SOMETHINGTODO5", TaskStatus.NEW));

        Epic epic1 = taskManager.createEpic(new Epic("EPIC1", "SOMEOFEPIC1"));
        Epic epic2 = taskManager.createEpic(new Epic("EPIC1", "SOMEOFEPIC2"));

        Subtask subtask1 = taskManager.createSubtask(new Subtask("SUBTASK1", "SOMEOFSUBTASK1", epic1.getId()));
        Subtask subtask2 = taskManager.createSubtask(new Subtask("SUBTASK2", "SOMEOFSUBTASK2", epic1.getId()));
        Subtask subtask3 = taskManager.createSubtask(new Subtask("SUBTASK3", "SOMEOFSUBTASK3", epic1.getId()));
        Subtask subtask4 = taskManager.createSubtask(new Subtask("SUBTASK4", "SOMEOFSUBTASK4", epic2.getId()));
        Subtask subtask5 = taskManager.createSubtask(new Subtask("SUBTASK5", "SOMEOFSUBTASK5", epic2.getId()));

        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task2.getId());
        taskManager.getTaskById(task3.getId());
        taskManager.getTaskById(task4.getId());
        taskManager.getTaskById(task5.getId());

        taskManager.getEpicById(epic1.getId());
        taskManager.getEpicById(epic2.getId());

        taskManager.getSubtaskById(subtask1.getId());
        taskManager.getSubtaskById(subtask2.getId());
        taskManager.getSubtaskById(subtask3.getId());
        taskManager.getSubtaskById(subtask4.getId());
        taskManager.getSubtaskById(subtask5.getId());

        System.out.println(taskManager.getHistory());

    }
}

