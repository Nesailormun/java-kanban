package service;

import exceptions.ManagerSaveException;
import module.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class FileBackedTaskManager extends InMemoryTaskManager {

    private static final String HEAD = "id,type,name,status,description,epic";
    private static final String SEPARATE_LINE = "HISTORY:";

    private Path path;


    public FileBackedTaskManager(Path path) {
        this.path = path;
    }

    public void save() {
        try (FileWriter fileWriter = new FileWriter(path.toString())) {
            fileWriter.write(HEAD + "\n");

            for (Integer key : taskStorage.keySet()) {
                fileWriter.write(taskStorage.get(key).toString() + "\n");
            }
            for (Integer key : epicStorage.keySet()) {
                fileWriter.write(epicStorage.get(key).toString() + "\n");
            }
            for (Integer key : subtaskStorage.keySet()) {
                fileWriter.write(subtaskStorage.get(key).toString() + "\n");
            }
            fileWriter.write(SEPARATE_LINE + "\n");
            for (Task task : historyManager.getHistory()) {
                fileWriter.write(task.toString() + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка записи в файл.");
        }

    }

    public static void main(String[] args) {

        Path path = Paths.get("C:\\kirito\\projects\\testFile.txt");

//        FileBackedTaskManager manager = new FileBackedTaskManager(path);
//        Task task1 = manager.createTask(new Task("TASK1", "SOMETHINGTODO1", TaskStatus.NEW));
//        Task task2 = manager.createTask(new Task("TASK2", "SOMETHINGTODO2", TaskStatus.NEW));
//        Task task3 = manager.createTask(new Task("TASK3", "SOMETHINGTODO3", TaskStatus.NEW));
//        Task task4 = manager.createTask(new Task("TASK4", "SOMETHINGTODO4", TaskStatus.NEW));
//        Task task5 = manager.createTask(new Task("TASK5", "SOMETHINGTODO5", TaskStatus.NEW));
//
//        Epic epic1 = manager.createEpic(new Epic("EPIC1", "SOMEOFEPIC1"));
//        Epic epic2 = manager.createEpic(new Epic("EPIC1", "SOMEOFEPIC2"));
//
//        Subtask subtask1 = manager.createSubtask(new Subtask("SUBTASK1", "SOMEOFSUBTASK1", epic1.getId()));
//        Subtask subtask2 = manager.createSubtask(new Subtask("SUBTASK2", "SOMEOFSUBTASK2", epic1.getId()));
//        Subtask subtask3 = manager.createSubtask(new Subtask("SUBTASK3", "SOMEOFSUBTASK3", epic1.getId()));
//        Subtask subtask4 = manager.createSubtask(new Subtask("SUBTASK4", "SOMEOFSUBTASK4", epic2.getId()));
//        Subtask subtask5 = manager.createSubtask(new Subtask("SUBTASK5", "SOMEOFSUBTASK5", epic2.getId()));
//
//
//        manager.getTaskById(task1.getId());
//        manager.getTaskById(task2.getId());
//        manager.getTaskById(task3.getId());
//        manager.getTaskById(task4.getId());
//        manager.getTaskById(task5.getId());
//
//        System.out.println(manager.getHistory());

        FileBackedTaskManager newManager = loadFromFile(path.toFile());

        System.out.println(newManager.getAllTasks());


    }

    public Task fromString(String value) {
        String[] split = value.split(",");
        TaskType type = TaskType.valueOf(split[1]);
        TaskStatus status = TaskStatus.valueOf(split[3]);
        Task task;

        switch (type) {
            case TASK:
                task = new Task(Integer.parseInt(split[0]), split[2], split[4], status);
                break;
            case SUBTASK:
                task = new Subtask(Integer.parseInt(split[0]), split[2], split[4], status, Integer.parseInt(split[5]));
                break;
            case EPIC:
                task = new Epic(Integer.parseInt(split[0]), split[2], split[4]);
                task.setStatus(status);
                break;
            default:
                throw new ManagerSaveException("Неизвестный тип данных.");
        }
        return task;
    }

    public void read() {
        try (BufferedReader reader = new BufferedReader(new FileReader(path.toString()))) {
            reader.readLine();
            while (reader.ready()) {
                String data = reader.readLine();
                if (!data.equals(SEPARATE_LINE)) {
                    if (data == null) {
                        break;
                    }
                    if (data.isEmpty()) {
                        break;
                    }
                    Task task = fromString(data);
                    switch (task.getType()) {
                        case TASK:
                            taskStorage.put(task.getId(), task);
                            break;
                        case EPIC:
                            epicStorage.put(task.getId(), (Epic) task);
                            break;
                        case SUBTASK:
                            Subtask subtask = (Subtask) task;
                            subtaskStorage.put(task.getId(),  subtask);
                            epicStorage.get(subtask.getEpicId()).addSubtask(subtask.getId());
                            break;
                    }
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка чтения данных из файла.");
        }

    }


    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager savedManager = new FileBackedTaskManager(file.toPath());
        savedManager.read();
        return savedManager;
    }

    @Override
    public Task createTask(Task task) {
        Task newTask = super.createTask(task);
        save();
        return newTask;
    }


    @Override
    public Epic createEpic(Epic epic) {

        Epic newEpic = super.createEpic(epic);
        save();
        return newEpic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask newSubtask = super.createSubtask(subtask);
        save();
        return newSubtask;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = super.getTaskById(id);
        save();
        return task;
    }

    @Override
    public void removeTask(int id) {
        super.removeTask(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = super.getEpicById(id);
        save();
        return epic;
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void removeEpic(int id) {
        super.removeEpic(id);
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = super.getSubtaskById(id);
        save();
        return subtask;
    }

    @Override
    public void removeSubtask(int id) {
        super.removeSubtask(id);
        save();

    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }
}
