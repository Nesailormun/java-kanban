package service;

import module.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {

    protected int taskId = 1;
    protected final Map<Integer, Task> taskStorage = new HashMap<>();
    protected final Map<Integer, Epic> epicStorage = new HashMap<>();
    protected final Map<Integer, Subtask> subtaskStorage = new HashMap<>();
    protected final HistoryManager historyManager = Manager.getDefaultHistory();

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
        }
    }

    @Override
    public void removeTask(int id) {
        if (taskStorage.containsKey(id)) {
            taskStorage.remove(id);
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteAllTasks() {
        if (!taskStorage.isEmpty()) {
            for (Integer id : taskStorage.keySet()) {
                historyManager.remove(id);
            }
            taskStorage.clear();
        }
    }

    @Override
    public List<Task> getAllTasks() {
        if (!taskStorage.isEmpty()) {
            return new ArrayList<>(taskStorage.values());
        }
        return null;
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
        return null;
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
        return null;
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
}


