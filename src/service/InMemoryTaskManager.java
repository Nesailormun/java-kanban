package service;

import enums.TaskStatus;
import exceptions.DateTimeIntersectionException;
import exceptions.NotFoundException;
import exceptions.NullEqualsException;
import model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;


public class InMemoryTaskManager implements TaskManager {

    protected int taskId = 1;
    protected final Map<Integer, Task> taskStorage = new HashMap<>();
    protected final Map<Integer, Epic> epicStorage = new HashMap<>();
    protected final Map<Integer, Subtask> subtaskStorage = new HashMap<>();
    protected final HistoryManager historyManager = Manager.getDefaultHistory();
    protected final Set<Task> prioritizedTasks = new TreeSet<>((Task task1, Task task2) -> {
        if (task1.getId() == task2.getId())
            return 0;
        if (task1.getStartTime() == null)
            return 1;
        if (task2.getStartTime() == null)
            return -1;
        int timeComparison = task1.getStartTime().compareTo(task2.getStartTime());
        if (timeComparison != 0)
            return timeComparison; // Сравнение по времени
        return Integer.compare(task1.getId(), task2.getId());
    });


    public static void main(String[] args) {

        TaskManager manager = Manager.getDefault();

        Task task1 = manager.createTask(new Task("TASK1NODATETIME", "SOMETHINGTODO1", TaskStatus.NEW));
        Task task2 = manager.createTask(new Task("TASK2NODATETIME", "SOMETHINGTODO2", TaskStatus.NEW));
        Task task3 = manager.createTask(new Task("TASK3", "SOMETHINGTODO3",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(90)));
        Task task4 = manager.createTask(new Task("TASK4", "SOMETHINGTODO4",
                TaskStatus.NEW, LocalDateTime.now().plus(Duration.ofMinutes(90)), Duration.ofMinutes(50)));
        Task task5 = manager.createTask(null);
        Task task6 = manager.createTask(new Task("TASK6", "SOMETHINGTODO6",
                TaskStatus.NEW, LocalDateTime.now().minus(Duration.ofMinutes(51)), Duration.ofMinutes(50)));

        System.out.println(manager.getPrioritizedTasks());
        System.out.println(manager.getAllTasks());
        System.out.println();
        manager.updateTask(new Task(task3.getId(), "TASK7", "SOMETHINGTODO7",
                TaskStatus.NEW, LocalDateTime.now().minus(Duration.ofMinutes(141)), Duration.ofMinutes(50)));
        manager.updateTask(new Task("WRONGUPDATETASK", "WRONG"));

        System.out.println(manager.getPrioritizedTasks());
        System.out.println(manager.getAllTasks());
        manager.removeTask(task6.getId());
        System.out.println();
        System.out.println(manager.getPrioritizedTasks());
        System.out.println(manager.getAllTasks());

        manager.deleteAllTasks();
        System.out.println();
        System.out.println(manager.getPrioritizedTasks());
        System.out.println(manager.getAllTasks());
        Task task24 = manager.createTask(new Task("TASK3", "SOMETHINGTODO3",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(90)));
        System.out.println(manager.getPrioritizedTasks());
        System.out.println(manager.getAllTasks());

        System.out.println();
        System.out.println("------EPIC TEST-------");
        Epic epic1 = manager.createEpic(new Epic("EPIC1", "DEPIC1"));
        Epic epic2 = manager.createEpic(new Epic("EPIC2", "DEPIC2"));
        Subtask subtask1 = manager.createSubtask(new Subtask("SUBTASK1", "DSUBTASK1", epic1.getId(),
                LocalDateTime.of(2025, 1, 27, 10, 0), Duration.ofMinutes(10)));
        Subtask subtask2 = manager.createSubtask(new Subtask("SUBTASK2", "DSUBTASK2", epic1.getId(),
                subtask1.getStartTime().plusMinutes(10), Duration.ofMinutes(20)));
        Subtask subtask3 = manager.createSubtask(new Subtask("SUBTASK3", "DSUBTASK3", epic1.getId()));
        Subtask subtask4 = manager.createSubtask(new Subtask("SUBTASK4", "DSUBTASK4", epic2.getId(),
                subtask1.getStartTime().minusMinutes(20), Duration.ofMinutes(15)));

        System.out.println("-------ВЫВОД ПРИОРИТЕТНЫХ ЗАДАЧ----------");
        System.out.println(manager.getPrioritizedTasks());

        System.out.println("--ПРОВЕРКА ОБНОВЛЕНИЯ САБТАСКА И СООТВЕТСТВУЮЩЕГО ЭПИКА---");
        manager.updateSubtask(new Subtask(subtask2.getId(), "NEWSUBTASK2", "DNEWSUBTASK2",
                TaskStatus.IN_PROGRESS, epic1.getId(), subtask1.getStartTime().plusMinutes(30), subtask2.getDuration()));
        manager.updateSubtask(new Subtask(subtask4.getId(), "NEWSUBTASK4", "DNEWSUBTASK4",
                TaskStatus.DONE, epic2.getId(), subtask1.getStartTime().minusMinutes(60), subtask4.getDuration()));
        manager.updateSubtask(new Subtask(subtask3.getId(), "NEWSUBTASK3", "DNEWSUBTASK3",
                TaskStatus.NEW, epic1.getId(), subtask1.getStartTime().plusMinutes(60), Duration.ofMinutes(20)));


        System.out.println(manager.getAllSubtasks());
        System.out.println(manager.getAllEpics());
        System.out.println(manager.getPrioritizedTasks());
        System.out.println();
        System.out.println("------ПРОВЕРКА УДАЛЕНИЯ САБТАСКОВ Subtask4 и Subtask1---------");

        manager.removeSubtask(subtask4.getId());
        manager.removeSubtask(subtask1.getId());
        System.out.println(manager.getPrioritizedTasks());

        System.out.println();
        System.out.println("------ПРОВЕРКА УДАЛЕНИЯ ВСЕХ САБТАСКОВ---------");

        manager.deleteAllSubtasks();
        System.out.println(manager.getPrioritizedTasks());

        System.out.println();
        System.out.println("------ПРОВЕРКА УДАЛЕНИЯ ВСЕХ ЭПИКОВ---------");
        manager.deleteAllEpics();
        System.out.println(manager.getPrioritizedTasks());
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    // Beginning of module.Task methods:

    @Override
    public Task getTaskById(int id) throws NotFoundException {
        if (taskStorage.containsKey(id)) {
            historyManager.add(taskStorage.get(id));
            return taskStorage.get(id);
        }
        throw new NotFoundException("Exception! Not founded task, incorrect id!");
    }

    @Override
    public Task createTask(Task task) {
        if (task == null) {
            throw new NullEqualsException("Exception! Task should not be null!");
        }
        if (!isValid(task))
            throw new DateTimeIntersectionException("Exception! Time intervals interaction!");
        if (task.getId() == 0) {
            task.setId(taskId);
        }
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.NEW);
        }
        taskStorage.put(task.getId(), task);
        prioritizedTasks.add(task);
        taskId++;
        return task;
    }

    @Override
    public void updateTask(Task task) {
        if (task == null) {
            throw new NullEqualsException("Exception! Task should not be null!");
        }
        if (!isValid(task))
            throw new DateTimeIntersectionException("Exception! Time intervals intersection!");
        if (!(taskStorage.containsKey(task.getId())))
            throw new NotFoundException("Exception! Not founded task, incorrect id!");
        prioritizedTasks.remove(taskStorage.get(task.getId()));
        prioritizedTasks.add(task);
        taskStorage.put(task.getId(), task);

    }

    @Override
    public void removeTask(int id) {
        if (!taskStorage.containsKey(id))
            throw new NotFoundException("Exception! Not founded task, incorrect id!");
        prioritizedTasks.remove(taskStorage.get(id));
        taskStorage.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void deleteAllTasks() {
        if (taskStorage.isEmpty())
            return;
        taskStorage.keySet()
                .forEach(id -> {
                    historyManager.remove(id);
                    prioritizedTasks.remove(taskStorage.get(id));
                });
        taskStorage.clear();
    }

    @Override
    public List<Task> getAllTasks() {
        if (!taskStorage.isEmpty()) {
            return new ArrayList<>(taskStorage.values());
        }
        return new ArrayList<>();
    }

// Beginning of module.Epic methods:

    @Override
    public Epic createEpic(Epic epic) {
        if (epic == null) {
            throw new NullEqualsException("Exception! Epic should not be null!");
        }
        if (epic.getId() == 0) {
            epic.setId(taskId);
        }
        epic.setStatus(calculateEpicStatus(epic));
        epicStorage.put(epic.getId(), epic);
        taskId++;
        return epic;
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic == null) {
            throw new NullEqualsException("Exception! Epic should not be null!");
        }
        if (!(epicStorage.containsKey(epic.getId())))
            throw new NotFoundException("Exception! Not founded epic, incorrect id!");
        epicStorage.get(epic.getId()).setName(epic.getName());
        epicStorage.get(epic.getId()).setDescription(epic.getDescription());

    }

    @Override
    public Epic getEpicById(int id) throws NotFoundException {
        if (epicStorage.containsKey(id)) {
            historyManager.add(epicStorage.get(id));
            return epicStorage.get(id);
        }
        throw new NotFoundException("Exception! Not founded epic, incorrect id!");
    }

    @Override
    public List<Epic> getAllEpics() {
        if (!epicStorage.isEmpty()) {
            return new ArrayList<>(epicStorage.values());
        }
        return new ArrayList<>();
    }

    @Override
    public void removeEpic(int id) {
        if (!epicStorage.containsKey(id))
            throw new NotFoundException("Exception! Not founded epic, incorrect id!");
        for (int subtaskId : epicStorage.get(id).getSubtasksId()) {
            prioritizedTasks.remove(subtaskStorage.get(subtaskId));
            subtaskStorage.remove(subtaskId);
            historyManager.remove(subtaskId);
        }
        epicStorage.remove(id);
        historyManager.remove(id);

    }

    @Override
    public void deleteAllEpics() {
        if (!epicStorage.isEmpty()) {
            historyManager.getHistory().stream()
                    .filter(task -> task instanceof Epic || task instanceof Subtask)
                    .forEach(task -> historyManager.remove(task.getId()));
            prioritizedTasks.removeIf(task -> task instanceof Subtask);
            epicStorage.clear();
            subtaskStorage.clear();
        }
    }

    private TaskStatus calculateEpicStatus(Epic epic) {
        ArrayList<Integer> listOfSubtaskIDs = epic.getSubtasksId();
        if (listOfSubtaskIDs.isEmpty()) {
            return TaskStatus.NEW;
        }
        int numOfDoneSubtasks = 0;
        int numOfNewSubtask = 0;
        for (int subtaskId : listOfSubtaskIDs) {
            if (TaskStatus.DONE.equals(subtaskStorage.get(subtaskId).getStatus())) {
                numOfDoneSubtasks++;
            } else if (TaskStatus.NEW.equals(subtaskStorage.get(subtaskId).getStatus())) {
                numOfNewSubtask++;
            }
        }
        if (numOfDoneSubtasks == listOfSubtaskIDs.size()) {
            return TaskStatus.DONE;
        }
        if (numOfNewSubtask == listOfSubtaskIDs.size()) {
            return TaskStatus.NEW;
        }
        return TaskStatus.IN_PROGRESS;
    }

    @Override
    public List<Subtask> getEpicsSubtasks(Epic epic) {
        if (epic == null)
            throw new NullEqualsException("Exception! Epic should not be null!");
        if (epic.getSubtasksId().isEmpty())
            return new ArrayList<>();

        return epic.getSubtasksId()
                .stream()
                .map(subtaskStorage::get)
                .toList();
    }

// Beginning of module.Subtask methods:

    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (subtask == null) {
            throw new NullEqualsException("Exception! Subtask should not be null!");
        }
        int idOfEpic = subtask.getEpicId();
        if (!epicStorage.containsKey(idOfEpic)) {
            throw new NotFoundException("Exception! Not founded epicId of subtask!");
        }
        if (!isValid(subtask)) {
            throw new DateTimeIntersectionException("Exception! Time intervals intersection!");
        }
        if (subtask.getId() == 0) {
            subtask.setId(taskId);
        }
        if (subtask.getStatus() == null) {
            subtask.setStatus(TaskStatus.NEW);
        }
        prioritizedTasks.add(subtask);
        Epic epic = epicStorage.get(idOfEpic);
        epic.addSubtask(subtask.getId());
        subtaskStorage.put(subtask.getId(), subtask);
        calculateEpicDateTime(epic);
        epic.setStatus(calculateEpicStatus(epic));
        taskId++;
        return subtask;
    }


    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null) {
            throw new NullEqualsException("Exception! Subtask should not be null!");
        }
        if (!isValid(subtask))
            throw new DateTimeIntersectionException("Exception! Time intervals intersection!");
        if (!subtaskStorage.containsKey(subtask.getId())) {
            throw new NotFoundException("Exception! Not founded subtask, incorrect id!");
        }
        int subtaskId = subtask.getId();
        Subtask oldSubtask = subtaskStorage.get(subtaskId);
        int subtaskEpicId = subtask.getEpicId();
        if (!epicStorage.containsKey(subtaskEpicId)) {
            throw new NotFoundException("Exception! Not founded epicId of subtask!");
        }
        Epic epic = epicStorage.get(subtaskEpicId);
        if (!epic.getSubtasksId().contains(subtask.getId())) {
            return;
        }
        prioritizedTasks.remove(oldSubtask);
        subtaskStorage.put(subtaskId, subtask);
        prioritizedTasks.add(subtask);
        calculateEpicDateTime(epic);
        epic.setStatus(calculateEpicStatus(epic));
    }

    @Override
    public Subtask getSubtaskById(int id) throws NotFoundException {
        if (subtaskStorage.containsKey(id)) {
            historyManager.add(subtaskStorage.get(id));
            return subtaskStorage.get(id);
        }
        throw new NotFoundException("Exception! Not founded subtask, incorrect id!");
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        if (!subtaskStorage.isEmpty()) {
            return new ArrayList<>(subtaskStorage.values());
        }
        return new ArrayList<>();
    }

    @Override
    public void removeSubtask(int id) {
        if (!subtaskStorage.containsKey(id))
            throw new NotFoundException("Exception! Not founded subtask, incorrect id!");
        int relatedEpicId = subtaskStorage.get(id).getEpicId();
        Epic relatedEpic = epicStorage.get(relatedEpicId);
        prioritizedTasks.remove(subtaskStorage.get(id));
        relatedEpic.deleteSubtask(id);
        subtaskStorage.remove(id);
        calculateEpicDateTime(relatedEpic);
        relatedEpic.setStatus(calculateEpicStatus(relatedEpic));
        historyManager.remove(id);
    }


    @Override
    public void deleteAllSubtasks() {
        if (subtaskStorage.isEmpty())
            return;
        for (Integer id : subtaskStorage.keySet()) {
            prioritizedTasks.remove(subtaskStorage.get(id));
            historyManager.remove(id);
        }
        subtaskStorage.clear();
        for (Epic epic : epicStorage.values()) {
            epic.clearSubtasksList();
            calculateEpicDateTime(epic);
            epic.setStatus(calculateEpicStatus(epic));
        }
    }


    protected void calculateEpicDateTime(Epic epic) {
        epic.setDuration(Duration.ofMinutes(0));
        epic.setStartTime(null);
        epic.setEndTime(null);
        if (epic.getSubtasksId().isEmpty()) {
            return;
        }
        LocalDateTime subtaskStartTime;
        LocalDateTime subtaskEndTime;
        Duration subtaskDuration;
        for (Integer id : epic.getSubtasksId()) {
            if (!(subtaskStorage.get(id).getStartTime() == null)) {
                subtaskStartTime = subtaskStorage.get(id).getStartTime();
                subtaskDuration = subtaskStorage.get(id).getDuration();
                subtaskEndTime = subtaskStorage.get(id).getEndTime();
                if (epic.getSubtasksId().size() == 1) {
                    epic.setStartTime(subtaskStartTime);
                    epic.setDuration(subtaskDuration);
                    epic.setEndTime(subtaskEndTime);
                    return;
                }
                if (epic.getStartTime() == null) {
                    epic.setStartTime(subtaskStartTime);
                    epic.setEndTime(subtaskEndTime);
                }
                epic.setDuration(epic.getDuration().plus(subtaskDuration));
                if (subtaskStartTime.isBefore(epic.getStartTime())) {
                    epic.setStartTime(subtaskStartTime);
                }
                if (subtaskEndTime.isAfter(epic.getEndTime())) {
                    epic.setEndTime(subtaskEndTime);
                }
            }
        }
    }

    public List<Task> getPrioritizedTasks() {
        if (prioritizedTasks.isEmpty())
            return new ArrayList<>();
        return new ArrayList<>(prioritizedTasks);
    }

    protected boolean isValid(Task task) {
        if (prioritizedTasks.isEmpty())
            return true;
        if (task.getStartTime() == null)
            return true;

        return prioritizedTasks
                .stream()
                .filter(priorityTask -> priorityTask.getId() != task.getId())
                .filter(priorityTask -> priorityTask.getStartTime() != null)
                .filter(priorityTask -> priorityTask.getStartTime().isEqual(task.getStartTime())
                        || (priorityTask.getStartTime().isBefore(task.getStartTime())
                        && priorityTask.getEndTime().isAfter(task.getStartTime()))
                        || (priorityTask.getStartTime().isAfter(task.getStartTime())
                        && priorityTask.getStartTime().isBefore(task.getEndTime())))
                .findFirst()
                .isEmpty();

    }
}



