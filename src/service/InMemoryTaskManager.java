package service;

import module.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
        if (task1.getStartTime().isBefore(task2.getStartTime())) {
            return -1;
        } else if (task1.getStartTime().isAfter(task2.getStartTime())) {
            return 1;
        } else {
            return task1.getId() - task2.getId();
        }
    });


    public static void main(String[] args) {
        InMemoryTaskManager manager = new InMemoryTaskManager();

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
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    // Beginning of module.Task methods:

    @Override
    public Task getTaskById(int id) {
        if (taskStorage.containsKey(id)) {
            historyManager.add(taskStorage.get(id));
            return taskStorage.get(id);
        }
        return null;
    }

    @Override
    public Task createTask(Task task) {
        if (task == null) {
            return null;
        }
        if (task.getId() == 0) {
            task.setId(taskId);
        }
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.NEW);
        }
        taskStorage.put(task.getId(), task);
        if (validateDateTime(task))
            prioritizedTasks.add(task);
        taskId++;
        return task;
    }

    @Override
    public void updateTask(Task task) {
        if (task == null) {
            return;
        }
        if (taskStorage.containsKey(task.getId())) {
            taskStorage.put(task.getId(), task);
            if (validateDateTime(task)) {
                prioritizedTasks.remove(taskStorage.get(task.getId()));
                prioritizedTasks.add(task);
            }
        }
    }

    @Override
    public void removeTask(int id) {
        if (taskStorage.containsKey(id)) {
            prioritizedTasks.remove(taskStorage.get(id));
            taskStorage.remove(id);
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteAllTasks() {
        if (!taskStorage.isEmpty()) {
            for (Integer id : taskStorage.keySet()) {
                historyManager.remove(id);
                prioritizedTasks.remove(taskStorage.get(id));
            }
            taskStorage.clear();
        }
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
            return null;
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
            return;
        }
        if (epicStorage.containsKey(epic.getId())) {
            epicStorage.get(epic.getId()).setName(epic.getName());
            epicStorage.get(epic.getId()).setDescription(epic.getDescription());
        }
    }

    @Override
    public Epic getEpicById(int id) {
        if (epicStorage.containsKey(id)) {
            historyManager.add(epicStorage.get(id));
            return epicStorage.get(id);
        }
        return null;
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
        if (epicStorage.containsKey(id)) {
            for (int subtaskId : epicStorage.get(id).getSubtasksId()) {
                subtaskStorage.remove(subtaskId);
                historyManager.remove(subtaskId);
            }
            epicStorage.remove(id);
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteAllEpics() {
        if (!epicStorage.isEmpty()) {
            for (Task task : historyManager.getHistory()) {
                if (task instanceof Epic || task instanceof Subtask) {
                    historyManager.remove(task.getId());
                }
            }
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
        ArrayList<Subtask> subtasks = new ArrayList<>();
        if (epic == null) {
            return null;
        }
        if (!epic.getSubtasksId().isEmpty()) {
            for (int id : epic.getSubtasksId()) {
                subtasks.add(subtaskStorage.get(id));
            }
            return subtasks;
        }
        return null;
    }

    // Beginning of module.Subtask methods:

    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (subtask == null) {
            return null;
        }
        int idOfEpic = subtask.getEpicId();
        if (!epicStorage.containsKey(idOfEpic)) {
            return null;
        }
        if (subtask.getId() == 0) {
            subtask.setId(taskId);
        }
        if (subtask.getStatus() == null) {
            subtask.setStatus(TaskStatus.NEW);
        }
        Epic epic = epicStorage.get(idOfEpic);
        epic.addSubtask(subtask.getId());
        if (epic.getStartTime() == null && subtask.getStartTime() != null)
            epic.setStartTime(subtask.getStartTime());
        if (epic.getStartTime() != null && subtask.getStartTime() != null
                && subtask.getStartTime().isBefore(epic.getStartTime()))
            epic.setStartTime(subtask.getStartTime());
        subtaskStorage.put(subtask.getId(), subtask);
        epic.setStatus(calculateEpicStatus(epic));
        taskId++;
        return subtask;
    }


    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null) {
            return;
        }
        if (!subtaskStorage.containsKey(subtask.getId())) {
            return;
        }
        int subtaskEpicId = subtask.getEpicId();
        if (!epicStorage.containsKey(subtaskEpicId)) {
            return;
        }
        Epic epic = epicStorage.get(subtaskEpicId);
        ArrayList<Integer> epicSubtaskList = epic.getSubtasksId();
        if (!epicSubtaskList.contains(subtask.getId())) {
            return;
        }
        subtaskStorage.put(subtask.getId(), subtask);
        epic.setStatus(calculateEpicStatus(epic));
    }

    @Override
    public Subtask getSubtaskById(int id) {
        if (subtaskStorage.containsKey(id)) {
            historyManager.add(subtaskStorage.get(id));
            return subtaskStorage.get(id);
        }
        return null;
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
        if (subtaskStorage.containsKey(id)) {
            int relatedEpicId = subtaskStorage.get(id).getEpicId();
            Epic relatedEpic = epicStorage.get(relatedEpicId);
            relatedEpic.deleteSubtask(id);
            relatedEpic.setStatus(calculateEpicStatus(relatedEpic));
            subtaskStorage.remove(id);
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteAllSubtasks() {
        if (!subtaskStorage.isEmpty()) {
            for (Integer id : subtaskStorage.keySet()) {
                historyManager.remove(id);
            }
            subtaskStorage.clear();
            for (Epic epic : epicStorage.values()) {
                epic.clearSubtasksList();
                epic.setStatus(calculateEpicStatus(epic));
            }
        }
    }

//    Comparator<Task> comparator = (task1, task2) -> {
//        if (task1.getId() == task2.getId())
//            return 0;
//        if (task1.getStartTime() == null)
//            return 1;
//        if (task2.getStartTime() == null)
//            return -1;
//        if (task1.getStartTime().isBefore(task2.getStartTime()))
//            return -1;
//        else
//            return 1;
//    };

    public void calculateEpicDateTime(Epic epic) {
        if (epic.getSubtasksId().isEmpty()) {
            epic.setStartTime(null);
            epic.setDuration(null);
            epic.setEndTime(null);
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

//    public List<Task> getPrioritizedTasks(){
//        return prioritizedTasks
//                .stream()
//                .filter(task -> task.getStartTime() != null)
//                .collect(Collectors.toList());
//    }

    public List<Task> getPrioritizedTasks() {
        if (prioritizedTasks.isEmpty())
            return new ArrayList<>();
        return new ArrayList<>(prioritizedTasks);
    }

    protected boolean validateDateTime(Task task) {
        if (prioritizedTasks.isEmpty())
            return true;
        if (task.getStartTime() == null)
            return true;
        return prioritizedTasks
                .stream()
                .filter(priorityTask -> priorityTask.getStartTime() != null)
                .filter(priorityTask -> (priorityTask.getStartTime().isEqual(task.getStartTime())
                                || (priorityTask.getStartTime().isBefore(task.getStartTime())
                                && priorityTask.getEndTime().isAfter(task.getStartTime()))
                                || (priorityTask.getStartTime().isAfter(task.getStartTime())
                                && priorityTask.getStartTime().isBefore(task.getEndTime()))))
                .findFirst()
                .isEmpty();

    }
}


