package service;

import enums.TaskStatus;
import enums.TaskType;
import exceptions.ManagerSaveException;
import model.*;

import java.io.*;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private static final String HEAD = "id,type,name,status,description,localdatetime,duration,epic,";
    private static final String HISTORY_LINE = "HISTORY:";
    private static final String PRIORITIZED_TASKS = "PRIORITIZED_TASKS";

    protected Path path;

    public FileBackedTaskManager(Path path) {
        this.path = path;
    }

    public static void main(String[] args) {
        try {
            File tempTestFile = File.createTempFile("tempTestFile", ".cvs");
            FileBackedTaskManager manager = new FileBackedTaskManager(tempTestFile.toPath());
            LocalDateTime start = LocalDateTime.of(2025, 1, 28, 10, 0);
            Duration duration = Duration.ofMinutes(30);

            Task task1 = manager.createTask(new Task("TASK1", "SOMETHINGTODO1", TaskStatus.NEW, start,
                    duration));
            Task task2 = manager.createTask(new Task("TASK2", "SOMETHINGTODO2", TaskStatus.NEW, start,
                    duration));
            Task task3 = manager.createTask(new Task("TASK3", "SOMETHINGTODO3", TaskStatus.NEW,
                    start.plusMinutes(30), duration));
            Task task4 = manager.createTask(new Task("TASK4", "SOMETHINGTODO4", TaskStatus.NEW,
                    start.plusMinutes(60), duration));
            Task task5 = manager.createTask(new Task("TASK5", "SOMETHINGTODO5", TaskStatus.NEW));


            Epic epic1 = manager.createEpic(new Epic("EPIC1", "SOMEOFEPIC1"));
            Epic epic2 = manager.createEpic(new Epic("EPIC2", "SOMEOFEPIC2"));

            Subtask subtask1 = manager.createSubtask(new Subtask("SUBTASK1", "SOMEOFSUBTASK1",
                    epic1.getId(), start.minusMinutes(180), duration));
            Subtask subtask2 = manager.createSubtask(new Subtask("SUBTASK2", "SOMEOFSUBTASK2",
                    epic1.getId(), start.minusMinutes(120), duration));
            Subtask subtask3 = manager.createSubtask(new Subtask("SUBTASK3", "SOMEOFSUBTASK3",
                    epic1.getId(), start.minusMinutes(60), duration));
            Subtask subtask4 = manager.createSubtask(new Subtask("SUBTASK4", "SOMEOFSUBTASK4",
                    epic2.getId()));
            Subtask subtask5 = manager.createSubtask(new Subtask("SUBTASK5", "SOMEOFSUBTASK5",
                    epic2.getId()));

            System.out.println("-----Получаем список в порядке приоритета-----");
            System.out.println(manager.getPrioritizedTasks());
            System.out.println();

            manager.getTaskById(task1.getId());
            manager.getTaskById(task3.getId());
            manager.getTaskById(task4.getId());
            manager.getTaskById(task5.getId());
            manager.getSubtaskById(subtask1.getId());
            manager.getSubtaskById(subtask2.getId());
            manager.getSubtaskById(subtask3.getId());
            manager.getSubtaskById(subtask4.getId());
            manager.getSubtaskById(subtask5.getId());
            manager.getTaskById(task1.getId());
            manager.getEpicById(epic1.getId());
            manager.getEpicById(epic2.getId());

//            manager.removeEpic(epic1.getId());
            System.out.println("История просмотров тасков manager:");
            System.out.println(manager.getHistory());
            System.out.println();
            manager.removeTask(task1.getId());
            System.out.println("Все таски manager после удаления task1:");
            System.out.println(manager.getAllTasks());
            System.out.println();
            System.out.println("-----Получаем список в порядке приоритета-----");
            System.out.println(manager.getPrioritizedTasks());
            System.out.println();
            System.out.println("-----Обновляем сабтаск5 И вновь выводим список priority-----");
            manager.updateSubtask(new Subtask(subtask5.getId(), "SUBTASK5", "SOMEOFSUBTASK5",
                    TaskStatus.DONE, epic2.getId(), start.minusMinutes(210), duration));
            System.out.println(manager.getPrioritizedTasks());
            System.out.println();

            System.out.println("-----Выводим историю просмотров-----");
            System.out.println(manager.getHistory());
            System.out.println();
            System.out.println("Далее тестируем newManager, загруженный из файла");
            // Восстанавливаем FileBackedTaskManager backedTaskManager из сохраненного файла и выполняем операции с ним

            FileBackedTaskManager backedTaskManager = loadFromFile(tempTestFile);

            System.out.println(backedTaskManager.getHistory());
            System.out.println(backedTaskManager.getAllTasks());
            System.out.println(backedTaskManager.getAllSubtasks());
            System.out.println(backedTaskManager.getAllEpics());
            System.out.println(backedTaskManager.getPrioritizedTasks());

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка создания файла.");
        }
    }

    protected void setNewMaxTaskId(int newId) {
        taskId = newId + 1;
    }

    protected void save() {
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
            fileWriter.write(HISTORY_LINE + "\n");
            for (Task task : historyManager.getHistory()) {
                fileWriter.write(task.toString() + "\n");
            }
            fileWriter.write(PRIORITIZED_TASKS + "\n");
            for (Task task : prioritizedTasks) {
                fileWriter.write(task.toString() + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка записи в файл.");
        }

    }


    protected Task fromString(String value) {

        String[] split = value.split(",");
        TaskType type = TaskType.valueOf(split[1]);
        TaskStatus status = TaskStatus.valueOf(split[3]);
        Task task;

        switch (type) {
            case TASK:
                if (split.length > 5) {
                    task = new Task(Integer.parseInt(split[0]), split[2], split[4], status,
                            LocalDateTime.parse(split[5]), Duration.ofMinutes(Integer.parseInt(split[6])));
                } else {
                    task = new Task(Integer.parseInt(split[0]), split[2], split[4], status);
                }
                break;
            case EPIC:
                if (split.length > 5) {
                    task = new Epic(Integer.parseInt(split[0]), split[2], split[4]);
                    task.setStartTime(LocalDateTime.parse(split[5]));
                    task.setDuration(Duration.ofMinutes(Integer.parseInt(split[6])));
                } else
                    task = new Epic(Integer.parseInt(split[0]), split[2], split[4]);
                task.setStatus(status);
                break;
            case SUBTASK:
                if (split.length > 6) {
                    task = new Subtask(Integer.parseInt(split[0]), split[2], split[4], status,
                            Integer.parseInt(split[7]), LocalDateTime.parse(split[5]),
                            Duration.ofMinutes(Integer.parseInt(split[6])));
                } else
                    task = new Subtask(Integer.parseInt(split[0]), split[2], split[4], status,
                            Integer.parseInt(split[5]));
                break;
            default:
                throw new ManagerSaveException("Неизвестный тип данных.");
        }
        return task;
    }

    protected void readFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(path.toString()))) {
            reader.readLine();
            boolean isNotHistory = true;
            boolean isNotPriority = true;
            int maxId = 1;
            while (reader.ready()) {
                String data = reader.readLine();
                if (data.isEmpty() || data.isBlank()) {
                    break;
                }
                if (!data.equals(HISTORY_LINE) && isNotHistory) {
                    Task task = fromString(data);
                    switch (task.getType()) {
                        case TASK:
                            taskStorage.put(task.getId(), task);
                            maxId = Integer.max(task.getId(), maxId);
                            break;
                        case EPIC:
                            epicStorage.put(task.getId(), (Epic) task);
                            maxId = Integer.max(task.getId(), maxId);
                            break;
                        case SUBTASK:
                            Subtask subtask = (Subtask) task;
                            subtaskStorage.put(task.getId(), subtask);
                            maxId = Integer.max(task.getId(), maxId);
                            epicStorage.get(subtask.getEpicId()).addSubtask(subtask.getId());
                            break;
                        default:
                            throw new ManagerSaveException("Неизвестный тип данных.");
                    }
                }
                if (data.equals(HISTORY_LINE)) {
                    data = reader.readLine();
                    isNotHistory = false;
                }
                if (data.equals(PRIORITIZED_TASKS)) {
                    data = reader.readLine();
                    isNotPriority = false;
                }
                if (!isNotHistory && isNotPriority) {
                    historyManager.add(fromString(data));
                }

                if (!isNotPriority) {
                    if (data == null)
                        break;
                    prioritizedTasks.add(fromString(data));
                }
            }
            setNewMaxTaskId(maxId);
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка чтения данных из файла.");
        }
    }


    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager savedManager = new FileBackedTaskManager(file.toPath());
        savedManager.readFile();
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
